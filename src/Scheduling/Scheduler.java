package Scheduling;

import Monitoring.Monitor;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Scheduler {

    // Max number of threads, used to run tasks.
    private static final int THREADS = 10;

    // thread that is responsible for scheduling.
    private final Thread schedulerThread;

    // thread for monitoring.
    private final Thread monitor;

    // Mapping each id to task object
    HashMap<String, Task> tasks;

    // Providing thread pool, to reuse the already created threads.
    ExecutorService executor;

    // queue of tasks, with priority based on how much left to run each task.
    final PriorityQueue<Task> queue;

    // list of actually running task to monitor.
    final HashSet<Task> activeTasks;

    // Semaphore to enforce mutual exclusion on both the queue.
    final Semaphore queueSemaphore;
    final Semaphore sleepSemaphore; // Used to block the schedule thread, once there's no tasks to schedule.

    private final Logger logger; // logger to log warnings

    public Scheduler() {
        this.tasks = new HashMap<>();
        this.activeTasks = new HashSet<>();

        // to block the thread once it tries to acquire it.
        this.sleepSemaphore = new Semaphore(0);
        this.queueSemaphore = new Semaphore(1); // to allow just one thread to access the queue.

        // fixed number of threads created.
        executor = Executors.newFixedThreadPool(THREADS);

        // Priority based on nextTime (in millis).
        queue = new PriorityQueue<>(Comparator.comparingLong(o -> o.getNextTime().getTime()));

        this.logger = Logger.getLogger(Scheduler.class.getName());

        // Running both monitor, and the schedule threads.
        monitor = new Thread(new Monitor(this.activeTasks));
        schedulerThread = new Thread(this::schedule);
        monitor.start();
        schedulerThread.start();
    }

    /**
     * add task to the scheduler.
     * @param id must be unique, otherwise it won't be added.
     * @param frequency how often the task should execute, not allowed to run the task more than once at a time
     * @param expected expected time to run a task
     * @param toRun the task itself
     * @return true if it's added successfully, false otherwise.
     */
    public boolean addTask(String id, Duration frequency, Duration expected, Runnable toRun){
        if (tasks.containsKey(id) || frequency.getSeconds() < expected.getSeconds()) {
            this.logger.warning(String.format("Job %s can't be added, check if the id is used before. or make sure " +
                    "that the expected duration should be smaller than the frequency, executing the same job multiple times not allowed yet", id));
            return false;
        }
        postTask(new Task(expected.getSeconds(), frequency.getSeconds(), id, toRun));
        return true;
    }

    private void postTask(Task task) {

        // callback to update activeTasks, and the queue.
        // also to notify the schedule thread to check for next tasks.
        task.setCallback(() -> {
            try {
                queueSemaphore.acquire();
                this.queue.add(task);
                synchronized (this.activeTasks) {
                    this.activeTasks.remove(task);
                }
                if (this.sleepSemaphore.getQueueLength() > 0) {
                    this.sleepSemaphore.release();
                }
                queueSemaphore.release();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        this.tasks.put(task.getId(), task);
        runTask(task);
    }

    // Schedule thread.
    private void schedule() {
        while (true) {
            try {
                this.queueSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // no job had been added so far, then we will need to release the semaphore, and sleep.
            if (this.queue.isEmpty()) {
                try {
                    this.queueSemaphore.release();
                    this.sleepSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // if it's too early to run the next task, then again release the queue semaphore, but sleep for the remaining time.
            else if (this.queue.peek().getNextTime().after(new Date(System.currentTimeMillis() + 10))) {
                try {
                    assert this.queue.peek() != null;
                    long remainingTime = this.queue.peek().getNextTime().getTime() - System.currentTimeMillis();
                    this.queueSemaphore.release();
                    this.sleepSemaphore.tryAcquire(remainingTime, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // it's time to schedule some tasks!!!!
            else {
                while (!this.queue.isEmpty()) {
                    assert this.queue.peek() != null;
                    if (this.queue.peek().getNextTime().getTime() - System.currentTimeMillis() > 100) break;
                    Task readyToExecute = this.queue.poll();
                    runTask(readyToExecute);
                }
                this.queueSemaphore.release();
            }
        }
    }

    private void runTask(Task task) {
        this.executor.execute(Objects.requireNonNull(task));
        synchronized (this.activeTasks) {
            this.activeTasks.add(task);
            // to notify the scheduler to look if some task has exceeded its expected time.
            this.activeTasks.notify();
        }
    }


    /**
     * @return true if the scheduler is running, it must be false whenever there are no tasks to execute
     */
    public boolean isSchedulerRunning() {
        return this.schedulerThread.getState().equals(Thread.State.RUNNABLE);
    }

    /**
     * @return the current number of blocked tasks waiting for their next time to execute.
     */
    public int getQueueSize() {
        int ans;
        synchronized (this.queue) {
            ans = this.queue.size();
        }
        return ans;
    }

    /**
     * @return the number of currently executing tasks.
     */
    public int getNumberOfRunningTasks() {
        int ans;
        synchronized (this.activeTasks) {
            ans = this.activeTasks.size();
        }
        return ans;
    }
}
