package i.mrhua269.zutils.impl.teleporter;

import i.mrhua269.zutils.api.teleporter.Teleporter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class FoliaTeleporterImpl implements Teleporter {
    @Override
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location loc, PlayerTeleportEvent.@NotNull TeleportCause cause, @NotNull Plugin plugin) {
        final CompletableFuture<Boolean> result = new CompletableFuture<>();

        entity.getScheduler().execute(plugin, () -> {
            entity.teleportAsync(loc, cause).thenAccept(result::complete); //Force run on the scheduler of entity
        },() -> result.complete(Boolean.FALSE),1L);

        return result;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location loc, @NotNull Plugin plugin) {
        final CompletableFuture<Boolean> result = new CompletableFuture<>();

        entity.getScheduler().execute(plugin, () -> {
            entity.teleportAsync(loc).thenAccept(result::complete); //Force run on the scheduler of entity
        },() -> result.complete(Boolean.FALSE),1L);

        return result;
    }
}
