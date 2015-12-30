/*
 * Author: Jayer
 * Create Date: 2015-01-13 13:24:45
 */
package com.github.stuxuhai.jcron;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;

public interface CronExecutorService extends ExecutorService {
    ScheduledFuture<?> schedule(Runnable task, CronExpression expression);
}
