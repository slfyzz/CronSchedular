package Scheduling;

import Logging.Logger;

import java.util.Date;

public class Task implements Runnable{

    private final long expectedExecutionTime;
    private final long frequency;
    private final Date nextTime;
    private final String id;
    private final Runnable task;
    private Runnable callback; // if the scheduler needs to perform any additional functionality.

    public Task(long expectedExecutionTime, long frequency, String id, Runnable task) {
        this.expectedExecutionTime = expectedExecutionTime;
        this.frequency = frequency;
        this.id = id;
        this.task = task;
        nextTime = new Date(System.currentTimeMillis());
    }


    @Override
    public void run() {
        // logging the start.
        Logger.startExec(id, this.getNextTime());
        // Calculating the start and the end of the execution for logging.
        long time = System.currentTimeMillis();
        task.run();
        long end = System.currentTimeMillis();

        // updating the next time.
        nextTime.setTime(nextTime.getTime() + frequency * 1000);
        Logger.endExec(id, expectedExecutionTime, (end - time) / 1000);
        this.callback.run();
    }

    public String getId() {
        return id;
    }

    public long getExpectedExecutionTime() {
        return expectedExecutionTime;
    }

    public Date getNextTime() {
        return nextTime;
    }

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }
}
