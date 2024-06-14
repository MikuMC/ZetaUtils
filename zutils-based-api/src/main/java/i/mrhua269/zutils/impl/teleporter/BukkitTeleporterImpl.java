package i.mrhua269.zutils.impl.teleporter;

import i.mrhua269.zutils.api.teleporter.Teleporter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class BukkitTeleporterImpl implements Teleporter {
    @Override
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location loc, PlayerTeleportEvent.@NotNull TeleportCause cause, @NotNull Plugin plugin) {
        return entity.teleportAsync(loc, cause); //Call directly
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location loc, @NotNull Plugin plugin) {
        return entity.teleportAsync(loc); //Call directly
    }
}
