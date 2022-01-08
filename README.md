# Cron scheduler

## README

Solution Description:

- The scheduler is providing only one method which is adding a task.
- Internally, the scheduler is implemented using pool of threads where the default number of threads is 10.
- Each job is added to a time-priority queue, and there's a separate thread to check the top of that queue.
- if the thread found a job that should be executed soon (100 ms), then the thread would submit the job to the thread pool.
- Once the job is executed successfully, it would return to the queue(in the current implementation, we don't allow to have a job that run multiple times in parallel).
- There's another thread that monitors the executing jobs regularly(every second), for now it's just printing a warning.

Decisions:
- Using FixedThreadPool to avoid the overhead of creating and deleting threads, and we don't want to have a thread for each job(waste of resources).
- The thread that checks for the queue job is not running regularly, it gets blocked if the queue is empty, or the next job needs to run after some time, 
  and it wakes up if a new job is added to save resources, it's a background thread, and it's not desired to do busy-wait for a background thread.

API:
```java
Scheduler scheduler = new Scheduler();
scheduler.addTask(id, Duration.ofSeconds(Freq_time), Duration.ofSeconds(Expected_time), () -> {
    // job implementation.
});
```
- Test case
```java
scheduler.addTask("1", Duration.ofSeconds(1), Duration.ofSeconds(1), () -> {
    System.out.println("1: Starting working");
    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    System.out.println("1: I finished!!");
});

scheduler.addTask("2", Duration.ofSeconds(2), Duration.ofSeconds(2), () -> {
    System.out.println("2: Starting working");
    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    System.out.println("2: I finished!!");
});
```
- Console output:
```
1::2022-01-08 03:28:37::Started with 34 ms delay. 
1: Starting working
2::2022-01-08 03:28:37::Started with 33 ms delay. 
2: Starting working
1: I finished!!
1::2022-01-08 03:28:38::Ended, actual 0 seconds, expected 1 seconds
1::2022-01-08 03:28:38::Started with 12 ms delay. 
1: Starting working
2: I finished!!
2::2022-01-08 03:28:38::Ended, actual 1 seconds, expected 2 seconds
1: I finished!!
1::2022-01-08 03:28:39::Ended, actual 0 seconds, expected 1 seconds
1::2022-01-08 03:28:39::Started with 5 ms delay. 
1: Starting working
2::2022-01-08 03:28:39::Started with 4 ms delay. 
2: Starting working
1: I finished!!
1::2022-01-08 03:28:40::Ended, actual 0 seconds, expected 1 seconds
2: I finished!!
2::2022-01-08 03:28:40::Ended, actual 1 seconds, expected 2 seconds
1::2022-01-08 03:28:40::Started with 9 ms delay. 
1: Starting working
1: I finished!!
1::2022-01-08 03:28:41::Ended, actual 0 seconds, expected 1 seconds
1::2022-01-08 03:28:41::Started with 6 ms delay. 
1: Starting working
2::2022-01-08 03:28:41::Started with 5 ms delay. 
2: Starting working
1: I finished!!
1::2022-01-08 03:28:42::Ended, actual 0 seconds, expected 1 seconds
1::2022-01-08 03:28:42::Started with 5 ms delay. 
1: Starting working
2: I finished!!
2::2022-01-08 03:28:42::Ended, actual 1 seconds, expected 2 seconds
1: I finished!!
1::2022-01-08 03:28:43::Ended, actual 0 seconds, expected 1 seconds
```

- Monitoring Example:
```java
scheduler.addTask("1", Duration.ofSeconds(1), Duration.ofSeconds(1), () -> {
    System.out.println("1: Starting working");
    try {
        Thread.sleep(50000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    System.out.println("1: I finished!!");
});

scheduler.addTask("2", Duration.ofSeconds(2), Duration.ofSeconds(2), () -> {
    System.out.println("2: Starting working");
    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    System.out.println("2: I finished!!");
});
```
Console output:
```
2::2022-01-08 03:30:19::Started with 38 ms delay. 
1::2022-01-08 03:30:19::Started with 39 ms delay. 
2: Starting working
1: Starting working
2: I finished!!
2::2022-01-08 03:30:20::Ended, actual 1 seconds, expected 2 seconds
2::2022-01-08 03:30:21::Started with 10 ms delay. 
2: Starting working
Jan 08, 2022 3:30:21 PM Monitoring.Monitor run
WARNING: Scheduling.Task 1 has exceeded the expected execution time
2: I finished!!
2::2022-01-08 03:30:22::Ended, actual 1 seconds, expected 2 seconds
2::2022-01-08 03:30:23::Started with 11 ms delay. 
2: Starting working
2: I finished!!
2::2022-01-08 03:30:24::Ended, actual 1 seconds, expected 2 seconds

```


Further Improvements:
- Allowing the same job to be executed multiple times in parallel.
- adding more statistical operations, such as calculating the actual average execution time.
- Allowing to cancel jobs and manipulate existing jobs.
- Adding more tests for the monitoring, and to cover test cases for large number of jobs
