package music.symphony.com.materialmusicv2.utils.mediasessionutils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MediaSessionTasker {
    private static ExecutorService executorService = null;

    static Future<?> addTask(Runnable runnable) {
        renewExecutorService();
        return executorService.submit(runnable);
    }

    private static void renewExecutorService() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
    }

    public static void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }
}
