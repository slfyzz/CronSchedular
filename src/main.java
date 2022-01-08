import Scheduling.Scheduler;

import java.time.Duration;

public class main {
    public static void main(String[] args) {

        Scheduler scheduler = new Scheduler();

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

    }
}
