package i.mrhua269.zutils.impl.scheduler;

import i.mrhua269.zutils.api.scheduler.ScheduledTask;
import i.mrhua269.zutils.api.scheduler.SchedulerService;
import i.mrhua269.zutils.impl.scheduler.task.NormalScheduledTaskImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitSchedulerServiceImpl implements SchedulerService {
    @Override
    public @NotNull ScheduledTask runTaskNow(Runnable task, @Nullable Location location, Plugin plugin) {
        final NormalScheduledTaskImpl wrapped = new NormalScheduledTaskImpl(this);
        wrapped.setTaskId(Bukkit.getScheduler().runTask(plugin,task).getTaskId());
        return wrapped;
    }

    @Override
    public @NotNull ScheduledTask runAsyncTaskNow(Runnable task, Plugin plugin) {
        final NormalScheduledTaskImpl wrapped = new NormalScheduledTaskImpl(this);
        wrapped.setTaskId(Bukkit.getScheduler().runTaskAsynchronously(plugin,task).getTaskId());
        return wrapped;
    }

    @Override
    public @NotNull ScheduledTask runTaskLater(Runnable task, long delay, @Nullable Location location, Plugin plugin) {
        final NormalScheduledTaskImpl wrapped = new NormalScheduledTaskImpl(this);
        wrapped.setTaskId(Bukkit.getScheduler().runTaskLater(plugin,task,delay).getTaskId());
        return wrapped;
    }

    @Override
    public @NotNull ScheduledTask runAsyncTaskLater(Runnable task, long delay, Plugin plugin) {
        final NormalScheduledTaskImpl wrapped = new NormalScheduledTaskImpl(this);
        wrapped.setTaskId(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay).getTaskId());
        return wrapped;
    }

    @Override
    public @NotNull ScheduledTask runTaskTimer(Runnable task, long delay, long period, @Nullable Location location, Plugin plugin) {
        final NormalScheduledTaskImpl wrapped = new NormalScheduledTaskImpl(this);
        wrapped.setTaskId(Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period).getTaskId());
        return wrapped;
    }

    @Override
    public @NotNull ScheduledTask runAsyncTaskTimer(Runnable task, long delay, long period, Plugin plugin) {
        final NormalScheduledTaskImpl wrapped = new NormalScheduledTaskImpl(this);
        wrapped.setTaskId(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period).getTaskId());
        return wrapped;
    }

    @Override
    public void cancelTask(@NotNull Object taskId) {
        Bukkit.getScheduler().cancelTask((Integer) taskId);
    }
}