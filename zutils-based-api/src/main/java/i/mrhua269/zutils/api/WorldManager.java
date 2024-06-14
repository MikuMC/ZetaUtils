package i.mrhua269.zutils.api;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WorldManager {
    boolean unloadWorld(@NotNull World world, boolean save);

    boolean unloadWorld(@NotNull String name, boolean save);

    @Nullable
    World createWorld(@NotNull WorldCreator creator);
}
