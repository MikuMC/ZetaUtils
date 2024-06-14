package i.mrhua269.zutils.api.teleporter;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface Teleporter {
    @NotNull
    CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, final @NotNull Location loc, final @NotNull PlayerTeleportEvent.TeleportCause cause, @NotNull Plugin plugin);

    @NotNull
    CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, final @NotNull Location loc, @NotNull Plugin plugin);
}
