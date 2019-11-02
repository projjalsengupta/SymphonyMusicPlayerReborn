package music.symphony.com.materialmusicv2.utils.mediasessionutils;

import music.symphony.com.materialmusicv2.utils.misc.TaskScheduler;

public class TaskRepeater {

    private static TaskRepeater taskRepeater;

    private TaskScheduler taskScheduler = null;

    private TaskRepeater() {
        taskScheduler = new TaskScheduler();
    }

    public static TaskRepeater getInstance() {
        if (taskRepeater == null) {
            taskRepeater= new TaskRepeater();
        }
        return taskRepeater;
    }

    public void addTask(Runnable task, long time) {
        if (taskScheduler != null) {
            taskScheduler.scheduleAtFixedRate(task, time);
        }
    }

    public void removeTask(Runnable task) {
        if (taskScheduler !=null) {
            taskScheduler.stop(task);
        }
    }
}
