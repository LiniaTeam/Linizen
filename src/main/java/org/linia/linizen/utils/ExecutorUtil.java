package org.linia.linizen.utils;

import org.bukkit.Bukkit;
import org.linia.linizen.Linizen;

import java.util.concurrent.CountDownLatch;

public class ExecutorUtil {

    // We usually use this method to make sure that any exceptions thrown by the runnable are propagated to the caller
    public static void runSyncAndWait(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        RuntimeException[] runtimeException = new RuntimeException[1];

        Bukkit.getScheduler().runTask(Linizen.instance, () -> {
            try {
                runnable.run();
            } catch (RuntimeException e) {
                runtimeException[0] = e;
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e); // Rather propagate the interrupt (and thus prevent further execution) than continue
        }

        if (runtimeException[0] != null) {
            throw runtimeException[0];
        }
    }
}