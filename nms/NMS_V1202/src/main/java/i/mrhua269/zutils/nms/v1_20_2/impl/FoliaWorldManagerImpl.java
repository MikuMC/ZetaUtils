package i.mrhua269.zutils.nms.v1_20_2.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import i.mrhua269.zutils.api.WorldManager;
import io.papermc.paper.chunk.system.io.RegionFileIOThread;
import io.papermc.paper.chunk.system.scheduling.ChunkHolderManager;
import io.papermc.paper.chunk.system.scheduling.NewChunkHolder;
import io.papermc.paper.threadedregions.RegionizedServer;
import io.papermc.paper.threadedregions.ThreadedRegionizer;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.generator.CraftWorldInfo;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FoliaWorldManagerImpl implements WorldManager {

    //TODO Did we ACTUALLY kill the region?
    private void killAllThreadedRegionsOnce(@NotNull ServerLevel level){
        level.regioniser.computeForAllRegions(region -> {
            //Ugly reflection :(
            try {
                final Class<ThreadedRegionizer.ThreadedRegion> threadedRegionClass = ThreadedRegionizer.ThreadedRegion.class;
                final Method tryKillMethod = threadedRegionClass.getDeclaredMethod("tryKill");
                tryKillMethod.setAccessible(true);
                tryKillMethod.invoke(region);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        });
    }

    private void saveAllChunksNoCheck(ServerLevel level, @NotNull ChunkHolderManager holderManager, final boolean flush, final boolean shutdown, final boolean logProgress, final boolean first, final boolean last) {
        io.papermc.paper.threadedregions.RegionizedServer.ensureGlobalTickThread("Saving all chunks can be done only on global tick thread");

        final List<NewChunkHolder> holders = holderManager.getChunkHolders(); //We don't need current region because all regions are killed right now

        if (first && logProgress) {
            MinecraftServer.LOGGER.info("Saving all chunkholders for world '" + level.getWorld().getName() + "'");
        }

        final DecimalFormat format = new DecimalFormat("#0.00");

        int saved = 0;

        final long start = System.nanoTime();
        long lastLog = start;
        boolean needsFlush = false;
        final int flushInterval = 50;

        int savedChunk = 0;
        int savedEntity = 0;
        int savedPoi = 0;

        for (int i = 0, len = holders.size(); i < len; ++i) {
            final NewChunkHolder holder = holders.get(i);
            if (!RegionizedServer.isGlobalTickThread()) {
                continue;
            }
            try {
                final NewChunkHolder.SaveStat saveStat = holder.save(shutdown, false);
                if (saveStat != null) {
                    ++saved;
                    needsFlush = flush;
                    if (saveStat.savedChunk()) {
                        ++savedChunk;
                    }
                    if (saveStat.savedEntityChunk()) {
                        ++savedEntity;
                    }
                    if (saveStat.savedPoiChunk()) {
                        ++savedPoi;
                    }
                }
            } catch (ThreadDeath killSignal) {
                throw killSignal;
            } catch (final Throwable ex) {
                MinecraftServer.LOGGER.error("Failed to save chunk (" + holder.chunkX + "," + holder.chunkZ + ") in world '" + level.getWorld().getName() + "'", ex);
            }

            if (needsFlush && (saved % flushInterval) == 0) {
                needsFlush = false;
                RegionFileIOThread.partialFlush(flushInterval / 2);
            }

            if (logProgress) {
                final long currTime = System.nanoTime();
                if ((currTime - lastLog) > TimeUnit.SECONDS.toNanos(10L)) {
                    lastLog = currTime;
                    MinecraftServer.LOGGER.info("Saved " + saved + " chunks (" + format.format((double)(i+1)/(double)len * 100.0) + "%) in world '" + level.getWorld().getName() + "'");
                }
            }
        }

        if (last && flush) {
            RegionFileIOThread.flush();
            if (level.paperConfig().chunks.flushRegionsOnSave) {
                try {
                    level.chunkSource.chunkMap.regionFileCache.flush();
                } catch (IOException ex) {
                    MinecraftServer.LOGGER.error("Exception when flushing regions in world {}", level.getWorld().getName(), ex);
                }
            }
        }

        if (logProgress) {
            MinecraftServer.LOGGER.info("Saved " + savedChunk + " block chunks, " + savedEntity + " entity chunks, " + savedPoi + " poi chunks in world '" + level.getWorld().getName() + "' in " + format.format(1.0E-9 * (System.nanoTime() - start)) + "s");
        }
    }

    public void save(@NotNull ServerLevel level, boolean flush, boolean savingDisabled) {
        // Paper start - rewrite chunk system - add close param
        this.save(level, flush, savingDisabled, false);
    }

    public void save(@NotNull ServerLevel level, boolean flush, boolean savingDisabled, boolean close) {
        if (!savingDisabled) {
            org.bukkit.Bukkit.getPluginManager().callEvent(new org.bukkit.event.world.WorldSaveEvent(level.getWorld())); // CraftBukkit

            level.saveLevelData();

            if (!close) this.saveAllChunksNoCheck(level, level.chunkTaskScheduler.chunkHolderManager, flush, false, false,true,true); // Paper - rewrite chunk system
            if (close) this.closeChunkProvider(level, true);

        } else if (close) {
            this.closeChunkProvider(level, false);
        }
    }

    private void closeChunkProvider(@NotNull ServerLevel handle, boolean save){
        handle.chunkTaskScheduler.chunkHolderManager.close(save, true,true, true, false);
    }

    @Override
    public boolean unloadWorld(@NotNull World world, boolean save) {
        io.papermc.paper.threadedregions.RegionizedServer.ensureGlobalTickThread("World unload can be done only on global tick thread");

        if (world == null) {
            return false;
        }

        final CraftServer craftServer = ((CraftServer) Bukkit.getServer());
        final MinecraftServer console = craftServer.getServer();
        ServerLevel handle = ((CraftWorld) world).getHandle();

        if (console.getLevel(handle.dimension()) == null) {
            return false;
        }

        if (handle.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
            return false;
        }

        if (!handle.players().isEmpty()) {
            return false;
        }

        WorldUnloadEvent e = new WorldUnloadEvent(handle.getWorld());
        Bukkit.getPluginManager().callEvent(e);

        if (e.isCancelled()) {
            return false;
        }

        try {
            this.killAllThreadedRegionsOnce(handle);

            if (save) {
                this.save(handle, true, false);
            }

            this.closeChunkProvider(handle, save);
            handle.convertable.close();
        } catch (Exception ex) {
            Bukkit.getLogger().log(java.util.logging.Level.SEVERE, null, ex);
        }

        final Map<String, World> worlds;

        //Ugly reflection :(
        try {
            final Class<CraftServer> craftServerClass = CraftServer.class;
            final Field worldsField = craftServerClass.getDeclaredField("worlds");
            worldsField.setAccessible(true);
            worlds = ((Map<String, World>) worldsField.get(craftServer));
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }

        worlds.remove(world.getName().toLowerCase(java.util.Locale.ENGLISH));
        console.removeLevel(handle);
        return true;
    }


    @Override
    public boolean unloadWorld(@NotNull String name, boolean save) {
        return this.unloadWorld(Bukkit.getWorld(name),save);
    }

    @Override
    public World createWorld(@NotNull WorldCreator creator) {
        io.papermc.paper.threadedregions.RegionizedServer.ensureGlobalTickThread("World create can be done only on global tick thread");
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        DedicatedServer console = craftServer.getServer();

        String name = creator.name();

        String levelName = console.getProperties().levelName;
        if (name.equals(levelName)
                || (console.isNetherEnabled() && name.equals(levelName + "_nether"))
                || (craftServer.getAllowEnd() && name.equals(levelName + "_the_end"))
        ) {
            return null;
        }

        ChunkGenerator generator = creator.generator();
        BiomeProvider biomeProvider = creator.biomeProvider();
        File folder = new File(craftServer.getWorldContainer(), name);
        org.bukkit.World world = craftServer.getWorld(name);

        CraftWorld worldByKey = (CraftWorld) craftServer.getWorld(creator.key());
        if (world != null || worldByKey != null) {
            if (world != worldByKey) {
                return world;
            }
            throw new IllegalArgumentException("Cannot create a world with key " + creator.key() + " and name " + name + " one (or both) already match a world that exists");
        }

        if (folder.exists()) {
            Preconditions.checkArgument(folder.isDirectory(), "File (%s) exists and isn't a folder", name);
        }

        if (generator == null) {
            generator = craftServer.getGenerator(name);
        }

        if (biomeProvider == null) {
            biomeProvider = craftServer.getBiomeProvider(name);
        }

        ResourceKey<LevelStem> actualDimension = switch (creator.environment()) {
            case NORMAL -> LevelStem.OVERWORLD;
            case NETHER -> LevelStem.NETHER;
            case THE_END -> LevelStem.END;
            default -> throw new IllegalArgumentException("Illegal dimension (" + creator.environment() + ")");
        };

        LevelStorageSource.LevelStorageAccess worldSession;
        try {
            worldSession = LevelStorageSource.createDefault(craftServer.getWorldContainer().toPath()).createAccess(name, actualDimension);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        boolean hardcore = creator.hardcore();

        PrimaryLevelData worlddata;
        WorldLoader.DataLoadContext worldloader_a = console.worldLoader;
        net.minecraft.core.Registry<LevelStem> iregistry = worldloader_a.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
        DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, worldloader_a.datapackWorldgen());
        Pair<WorldData, WorldDimensions.Complete> pair = worldSession.getDataTag(dynamicops, worldloader_a.dataConfiguration(), iregistry, worldloader_a.datapackWorldgen().allRegistriesLifecycle());


        if (pair != null) {
            worlddata = (PrimaryLevelData) pair.getFirst();
            iregistry = pair.getSecond().dimensions();
        } else {
            LevelSettings worldsettings;
            WorldOptions worldoptions = new WorldOptions(creator.seed(), creator.generateStructures(), false);
            WorldDimensions worlddimensions;

            DedicatedServerProperties.WorldDimensionData properties = new DedicatedServerProperties.WorldDimensionData(GsonHelper.parse((creator.generatorSettings().isEmpty()) ? "{}" : creator.generatorSettings()), creator.type().name().toLowerCase(Locale.ROOT));

            worldsettings = new LevelSettings(name, GameType.byId(Bukkit.getServer().getDefaultGameMode().getValue()), hardcore, Difficulty.EASY, false, new GameRules(), worldloader_a.dataConfiguration());
            worlddimensions = properties.create(worldloader_a.datapackWorldgen());

            WorldDimensions.Complete worlddimensions_b = worlddimensions.bake(iregistry);
            Lifecycle lifecycle = worlddimensions_b.lifecycle().add(worldloader_a.datapackWorldgen().allRegistriesLifecycle());

            worlddata = new PrimaryLevelData(worldsettings, worldoptions, worlddimensions_b.specialWorldProperty(), lifecycle);
            iregistry = worlddimensions_b.dimensions();
        }
        worlddata.customDimensions = iregistry;
        worlddata.checkName(name);
        worlddata.setModdedInfo(console.getServerModName(), console.getModdedStatus().shouldReportAsModified());

        long j = BiomeManager.obfuscateSeed(creator.seed());
        List<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(worlddata));
        LevelStem worlddimension = iregistry.get(actualDimension);

        WorldInfo worldInfo = new CraftWorldInfo(worlddata, worldSession, creator.environment(), worlddimension.type().value(), worlddimension.generator(), craftServer.getHandle().getServer().registryAccess()); // Paper
        if (biomeProvider == null && generator != null) {
            biomeProvider = generator.getDefaultBiomeProvider(worldInfo);
        }

        if (console.options.has("forceUpgrade")) {
            net.minecraft.server.Main.convertWorldButItWorks(
                    actualDimension, worldSession, DataFixers.getDataFixer(), worlddimension.generator().getTypeNameForDataFixer(), console.options.has("eraseCache")
            );
        }

        ResourceKey<net.minecraft.world.level.Level> worldKey;
        worldKey = ResourceKey.create(Registries.DIMENSION, new net.minecraft.resources.ResourceLocation(creator.key().getNamespace().toLowerCase(java.util.Locale.ENGLISH), creator.key().getKey().toLowerCase(java.util.Locale.ENGLISH))); // Paper

        ServerLevel internal = new ServerLevel(console, console.executor, worldSession, worlddata, worldKey, worlddimension, console.progressListenerFactory.create(11),
                worlddata.isDebugWorld(), j, creator.environment() == org.bukkit.World.Environment.NORMAL ? list : ImmutableList.of(), true, null, creator.environment(), generator, biomeProvider);

        internal.randomSpawnSelection = new ChunkPos(internal.getChunkSource().randomState().sampler().findSpawnPosition());
        int loadRegionRadius = ((32) >> 4);
        for (int currX = -loadRegionRadius; currX <= loadRegionRadius; ++currX) {
            for (int currZ = -loadRegionRadius; currZ <= loadRegionRadius; ++currZ) {
                net.minecraft.world.level.ChunkPos pos = new net.minecraft.world.level.ChunkPos(currX, currZ);
                internal.chunkSource.addTicketAtLevel(
                        TicketType.UNKNOWN, pos, io.papermc.paper.chunk.system.scheduling.ChunkHolderManager.MAX_TICKET_LEVEL, pos
                );
            }
        }

        console.addLevel(internal);

        internal.setSpawnSettings(true, true);

        internal.keepSpawnInMemory = creator.keepSpawnLoaded().toBooleanOrElse(internal.getWorld().getKeepSpawnInMemory()); // Paper

        io.papermc.paper.threadedregions.RegionizedServer.getInstance().addWorld(internal);

        Bukkit.getPluginManager().callEvent(new WorldLoadEvent(internal.getWorld()));

        return internal.getWorld();
    }
}
