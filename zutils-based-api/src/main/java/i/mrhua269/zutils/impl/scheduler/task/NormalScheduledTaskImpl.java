package i.mrhua269.zutils.impl.scheduler.task;

import i.mrhua269.zutils.api.scheduler.ScheduledTask;
import i.mrhua269.zutils.api.scheduler.SchedulerService;

public class NormalScheduledTaskImpl implements ScheduledTask {
    private final SchedulerService parentService;
    private Object taskId = null;

    public NormalScheduledTaskImpl(SchedulerService parentService) {
        this.parentService = parentService;
    }

    public void setTaskId(Object taskId){
        this.taskId = taskId;
    }

    @Override
    public void cancel() {
        this.parentService.cancelTask(this.taskId);
    }
}