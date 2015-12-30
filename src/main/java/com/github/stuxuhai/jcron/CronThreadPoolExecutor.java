/*
 * Author: Jayer
 * Create Date: 2015-01-13 13:24:45
 */
package com.github.stuxuhai.jcron;

import java.text.ParseException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import com.google.common.base.Throwables;

public class CronThreadPoolExecutor extends ScheduledThreadPoolExecutor implements CronExecutorService {

    public CronThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    public CronThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public CronThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
    }

    public CronThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
    }

    public ScheduledFuture<?> schedule(final Runnable task, final CronExpression expression) {
        if (task == null) {
            throw new NullPointerException();
        }

        this.setCorePoolSize(this.getCorePoolSize() + 1);

        Runnable scheduleTask = new Runnable() {
            public void run() {
                try {
                    DateTime now = new DateTime();
                    DateTime time = expression.getTimeAfter(now);
                    while (time != null) {
                        schedule(task, time.getMillis() - now.getMillis(), TimeUnit.MILLISECONDS);
                        while (now.isBefore(time)) {
                            Thread.sleep(time.getMillis() - now.getMillis());
                            now = new DateTime();
                        }
                        time = expression.getTimeAfter(now);
                    }
                } catch (RejectedExecutionException e) {
                } catch (CancellationException e) {
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ParseException e) {
                    Throwables.propagate(e);
                }
            }
        };

        execute(scheduleTask);

        return null;
    }

}
