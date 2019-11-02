package music.symphony.com.materialmusicv2.utils.misc;

import android.os.Handler;
import android.util.ArrayMap;

public class TaskScheduler extends Handler {

    private ArrayMap<Runnable, Runnable> tasks = new ArrayMap<>();

    public void scheduleAtFixedRate(final Runnable task, final long period) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.run();
                postDelayed(this, period);
            }
        };
        tasks.put(task, runnable);
        runnable.run();
    }

    public void stop(Runnable task) {
        Runnable removed = tasks.remove(task);
        if (removed != null) {
            removeCallbacks(removed);
        }
    }
}