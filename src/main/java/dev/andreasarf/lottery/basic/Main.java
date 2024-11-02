package dev.andreasarf.lottery.basic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final String FILE = "10m-v2.txt";

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();

        // >>> user-defined parameter
        // Adjust some parameters here to get different results
        final boolean printRuntime = true;
        final int numOfWorkers = args.length > 0 ? Integer.parseInt(args[0])
                : Runtime.getRuntime().availableProcessors();
        final Set<Integer> set = Set.of(4, 79, 13, 80, 56); // Set of organizer's numbers
        // <<< user-defined parameter

        System.out.println("READY");

        System.out.println("Number of workers: " + numOfWorkers);
        final Thread[] workers = new Thread[numOfWorkers];
        final AtomicInteger[] results = {
                new AtomicInteger(0), // 0 -> 5
                new AtomicInteger(0), // 1 -> 4
                new AtomicInteger(0), // 2 -> 3
                new AtomicInteger(0) // 3 -> 2
        };
        final BlockingQueue<Integer[]> data = new LinkedBlockingQueue<>();

        // read data and distribute them into the queue
        workers[0] = Thread.startVirtualThread(() -> {
            try {
                readData(data);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // begin processing and distribute the works into different virtual threads
        for (int i = 0; i < numOfWorkers; i++) {
            workers[i] = Thread.startVirtualThread(() -> {
                try {
                    checkResult(results, set, data);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        // join and wait the threads to finish
        for (Thread thread : workers) {
            thread.join();
        }

        // print result
        System.out.printf("%d, %d, %d, %d\n",
                results[3].get(), results[2].get(), results[1].get(), results[0].get());

        if (printRuntime) {
            System.out.println("Time: " + (System.currentTimeMillis() - start) + " ms");
        }

        System.out.close();
    }

    private static void readData(final BlockingQueue<Integer[]> data) throws IOException, InterruptedException {
        try (final BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
//                System.out.println("Read: " + line);
                final Integer[] values = Arrays.stream(line.split(" "))
                        .map(Integer::parseInt).toArray(Integer[]::new);
                if (values.length == 5) { // only take valid lottery numbers
                    data.put(values);
                }
            }
        }
    }

    private static void checkResult(final AtomicInteger[] results, final Set<Integer> set,
                                    final BlockingQueue<Integer[]> data) throws InterruptedException {
        Integer[] values = null;
        while ((values = data.poll(5, TimeUnit.MILLISECONDS)) != null) {
//            System.out.println("Check: " + Arrays.toString(values));
            int result = 0;
            for (int val : values) {
                if (set.contains(val)) {
                    result++;
                }
            }
            if (result > 1) {
                results[5 - result].incrementAndGet();
            }
        }
    }

}
