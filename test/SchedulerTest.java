import Scheduling.Scheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;

class SchedulerTest {
    @Test
    public void checkSchedulerStatesWithoutJobs() throws Exception {
        Scheduler scheduler = new Scheduler();
        Thread.sleep(50);
        Assertions.assertFalse(scheduler.isSchedulerRunning());
    }

    @Test
    public void checkSchedulerStatesWithSingleJobs() throws Exception {
        Scheduler scheduler = new Scheduler();
        scheduler.addTask(
                "Blob",
                Duration.ofHours(1),
                Duration.ofSeconds(2),
                () -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );
        Thread.sleep(50);
        Assertions.assertFalse(scheduler.isSchedulerRunning());
    }


    @Test
    public void checkSchedulerRunning() throws Exception {
        int JobNumbers = 10;
        Scheduler scheduler = new Scheduler();

        for (int i = 0; i < JobNumbers; i++) {
            scheduler.addTask(
                    String.valueOf(i),
                    Duration.ofHours(1),
                    Duration.ofSeconds(2),
                    () -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
            );
        }
        Thread.sleep(100);
        Assertions.assertEquals(JobNumbers, scheduler.getNumberOfRunningTasks());
        Assertions.assertEquals(0, scheduler.getQueueSize());
        Assertions.assertFalse(scheduler.isSchedulerRunning());

        Thread.sleep(1000);
        Assertions.assertEquals(0, scheduler.getNumberOfRunningTasks());
        Assertions.assertEquals(JobNumbers, scheduler.getQueueSize());
        Assertions.assertFalse(scheduler.isSchedulerRunning());


    }

    /**
     * That test assume that the scheduler has more than 2 threads available.
     * if the scheduler has just 1 thread to use, then it will fail due to lack of parallelism.
     */
    @Test
    public void testTwoInterleavingJobs() throws Exception{

        Scheduler scheduler = new Scheduler();
        scheduler.addTask(
            "BLOB",
            Duration.ofSeconds(2),
            Duration.ofSeconds(2),
            () -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        );

        // adding some noise to actual execution, the overhead of sync and adding to queues (usually by 30ms).
        Thread.sleep(550);
        Assertions.assertEquals(0, scheduler.getNumberOfRunningTasks());
        Assertions.assertEquals(1, scheduler.getQueueSize());
        Assertions.assertFalse(scheduler.isSchedulerRunning());

        scheduler.addTask(
            "BLOBFaster",
            Duration.ofSeconds(1),
            Duration.ofSeconds(1),
            () -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        );

        Assertions.assertEquals(1, scheduler.getNumberOfRunningTasks());
        Assertions.assertEquals(1, scheduler.getQueueSize());
        Assertions.assertFalse(scheduler.isSchedulerRunning());

        Thread.sleep(550);
        Assertions.assertEquals(0, scheduler.getNumberOfRunningTasks());
        Assertions.assertEquals(2, scheduler.getQueueSize());
        Assertions.assertFalse(scheduler.isSchedulerRunning());

        Thread.sleep(500);
        Assertions.assertEquals(1, scheduler.getNumberOfRunningTasks());
        Assertions.assertEquals(1, scheduler.getQueueSize());
        Assertions.assertFalse(scheduler.isSchedulerRunning());

        Thread.sleep(500);
        Assertions.assertEquals(1, scheduler.getNumberOfRunningTasks());
        Assertions.assertEquals(1, scheduler.getQueueSize());
        Assertions.assertFalse(scheduler.isSchedulerRunning());
    }


    @Test
    public void addTwoJobsWithSameName() throws Exception {
        Scheduler scheduler = new Scheduler();
        Assertions.assertTrue(scheduler.addTask(
                "BLOB",
                Duration.ofSeconds(2),
                Duration.ofSeconds(2),
                () -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        ));
        Assertions.assertFalse(scheduler.addTask(
                "BLOB",
                Duration.ofSeconds(2),
                Duration.ofSeconds(2),
                () -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        ));
        Thread.sleep(100);
        Assertions.assertEquals(scheduler.getNumberOfRunningTasks(), 1);
        Assertions.assertEquals(scheduler.getQueueSize(), 0);

        Thread.sleep(500);
        Assertions.assertEquals(scheduler.getQueueSize(), 1);
        Assertions.assertEquals(scheduler.getNumberOfRunningTasks(), 0);
    }

    @Test
    public void CheckForSmallFrequencyAndLongExecution() throws Exception {
        Scheduler scheduler = new Scheduler();
        Assertions.assertTrue(scheduler.addTask(
                "BLOB",
                Duration.ofSeconds(2),
                Duration.ofSeconds(2),
                () -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        ));

        Thread.sleep(50);
        Assertions.assertEquals(scheduler.getNumberOfRunningTasks(), 1);

        Assertions.assertFalse(scheduler.addTask(
                "BLOBFaster",
                Duration.ofSeconds(1),
                Duration.ofSeconds(500),
                () -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        ));

        Thread.sleep(50);
        Assertions.assertEquals(scheduler.getNumberOfRunningTasks() + scheduler.getQueueSize(), 1);
    }

}