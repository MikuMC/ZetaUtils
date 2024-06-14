package i.mrhua269.zutils.impl;

import i.mrhua269.zutils.api.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitWorldManagerImpl implements WorldManager {
    @Override
    public boolean unloadWorld(@NotNull World world, boolean save) {
        return Bukkit.getServer().unloadWorld(world,save);
    }

    @Override
    public boolean unloadWorld(@NotNull String name, boolean save) {
        return Bukkit.getServer().unloadWorld(name,save);
    }

    @Override
    public @Nullable World createWorld(@NotNull WorldCreator creator) {
        return Bukkit.createWorld(creator);
    }
}
