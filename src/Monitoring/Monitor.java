package Monitoring;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import Scheduling.Task;

public class Monitor implements Runnable{
    private final Set<Task> tasks; // reference to set of tasks to monitor.
    private final Logger logger; // logger to log warnings
    private Set<String> marked; // Set is used to not produce multiple warning for the same task.

    public Monitor(Set<Task> tasks) {
        this.tasks = tasks;
        marked = new HashSet<>();
        this.logger = Logger.getLogger(Monitor.class.getName());
    }

    @Override
    public void run() {
        while(true) {
            synchronized (tasks) {
                HashSet<String> new_marked = new HashSet<>();
                for (Task task : tasks) {
                    // allowing for 2 * expected execution time.
                    boolean exceeded = System.currentTimeMillis() - task.getNextTime().getTime() >= task.getExpectedExecutionTime() * 2000;
                    if (exceeded) {
                        new_marked.add(task.getId());
                        if (!marked.contains(task.getId())) {
                            logger.warning(String.format("Scheduling.Task %s has exceeded the expected execution time", task.getId()));
                        }
                    }
                }
                this.marked = new_marked;
            }
            try {
                // in milliseconds.
                int monitorFreq = 1000;
                Thread.sleep(monitorFreq);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
