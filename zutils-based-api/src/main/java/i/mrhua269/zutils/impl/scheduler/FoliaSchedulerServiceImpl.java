package i.mrhua269.zutils.impl.scheduler;

import i.mrhua269.zutils.api.scheduler.ScheduledTask;
import i.mrhua269.zutils.api.scheduler.SchedulerService;
import i.mrhua269.zutils.impl.scheduler.task.NormalScheduledTaskImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class FoliaSchedulerServiceImpl implements SchedulerService {

    @Override
    public @NotNull ScheduledTask runTaskNow(Runnable task, @Nullable Location location, Plugin plugin) {
        final NormalScheduledTaskImpl wrapped = new NormalScheduledTaskImpl(this);
        if (location != null){
            wrapped.setTaskId(Bukkit.getRegionScheduler().run(plugin,location,scheduledTask -> task.run()));
        }else{
            wrapped.setTaskId(Bukkit.getGlobalRegionScheduler().run(plugin,scheduledTask -> task.run()));
        }
        return wrapped;
    }

    @Override
    public @NotNull ScheduledTask runAsyncTaskNow(Runnable task, Plugin plugin) {
        final NormalScheduledTaskImpl wrapped = new NormalScheduledTaskImpl(this);
        wrapped.setTaskId(Bukkit.getAsyncScheduler().runNow(plugin,scheduledTask -> task.run()));
        return wrapped;
    }

    @Override
    public @NotNull ScheduledTask runTaskLater(Runnable task, long delay, @Nullable Location location, Plugin plugin) {
        final NormalScheduledTaskImpl wrapped = new NormalScheduledTaskImpl(this);
        if (location != null){
            wrapped.setTaskId(Bukkit.getRegionScheduler().runDelayed(plugin,location,scheduledTask -> task.run(),delay));
        }else{
            wrapped.setTaskId(Bukkit.getGlobalRegionScheduler().runDelayed(plugin,scheduledTask -> task.run(),delay));
        }
        return wrapped;
    }

    @Override
    public @NotNull ScheduledTask runAsyncTaskLater(Runnable task, long delay, Plugin plugin) {
        final NormalScheduledTaskImpl wrapped = new NormalScheduledTaskImpl(this);
        wrapped.setTaskId(Bukkit.getAsyncScheduler().runDelayed(plugin,scheduledTask -> task.run(),delay * 50, TimeUnit.MILLISECONDS));
        return null;
    }

    @Override
    public @NotNull ScheduledTask runTaskTimer(Runnable task, long delay, long period, @Nullable Location location, Plugin plugin) {
        final NormalScheduledTaskImpl wrapped = new NormalScheduledTaskImpl(this);
        if (location != null){
            wrapped.setTaskId(Bukkit.getRegionScheduler().runAtFixedRate(plugin,location,scheduledTask -> task.run(),delay,period));
        }else {
            wrapped.setTaskId(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,scheduledTask -> task.run(),delay,period));
        }
        return wrapped;
    }

    @Override
    public @NotNull ScheduledTask runAsyncTaskTimer(Runnable task, long delay, long period, Plugin plugin) {
        final NormalScheduledTaskImpl wrapped = new NormalScheduledTaskImpl(this);
        wrapped.setTaskId(Bukkit.getAsyncScheduler().runAtFixedRate(plugin,scheduledTask -> task.run(),delay * 50,period * 50, TimeUnit.MILLISECONDS));
        return wrapped;
    }

    @Override
    public void cancelTask(@NotNull Object taskId) {
        ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) taskId).cancel();
    }
}