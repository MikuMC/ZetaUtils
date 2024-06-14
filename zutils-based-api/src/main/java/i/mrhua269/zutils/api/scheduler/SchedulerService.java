package i.mrhua269.zutils.api.scheduler;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface SchedulerService {
    @NotNull
    ScheduledTask runTaskNow(Runnable task, @Nullable Location location, Plugin plugin);

    @NotNull
    ScheduledTask runAsyncTaskNow(Runnable task,Plugin plugin);

    @NotNull
    ScheduledTask runTaskLater(Runnable task,long delay,@Nullable Location location,Plugin plugin);

    @NotNull
    ScheduledTask runAsyncTaskLater(Runnable task,long delay,Plugin plugin);

    @NotNull
    ScheduledTask runTaskTimer(Runnable task,long delay,long period,@Nullable Location location,Plugin plugin);

    @NotNull
    ScheduledTask runAsyncTaskTimer(Runnable task,long delay,long period,Plugin plugin);

    void cancelTask(@NotNull Object taskId);
}
