package dev.andreasarf.lottery.experimental;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.nio.file.StandardOpenOption.READ;

public class ExpMain {

    private static final String FILE = "10m-v2.txt";

    public static void main(String[] args) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();

        // >>> user-defined parameter
        // Adjust some parameters here to get different results
        final boolean printRuntime = true;
        final boolean useVirtualThread = false;
        final int numOfWorkers = args.length > 0 ? Integer.parseInt(args[0])
                : Runtime.getRuntime().availableProcessors();
        final Set<Integer> set = Set.of(4, 79, 13, 80, 56); // Set of organizer's numbers
        // <<< user-defined parameter

        System.out.println("READY");

        try (var channel = FileChannel.open(Path.of(FILE), READ);
             var arena = Arena.ofShared()) {

            System.out.println("Number of workers: " + numOfWorkers);

            // calculate chunk sizes for data distribution
            var data = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size(), arena);
            var chunkSize = data.byteSize() / numOfWorkers;
            var chunks = new long[numOfWorkers + 1];
            chunks[numOfWorkers] = data.byteSize();

            for (int i = 1; i < numOfWorkers; ++i) {
                var chunkPos = i * chunkSize;

                while (data.get(JAVA_BYTE, chunkPos++) != '\n') {
                }

                chunks[i] = chunkPos;
            }

            // process data in parallel / distributing to workers using virtual threads
            if (useVirtualThread) {
                final AtomicInteger[] results = {
                        new AtomicInteger(0), // 0 -> 5
                        new AtomicInteger(0), // 1 -> 4
                        new AtomicInteger(0), // 2 -> 3
                        new AtomicInteger(0) // 3 -> 2
                };
                Thread[] workers = new Thread[numOfWorkers];

                for (int i = 0; i < numOfWorkers; i++) {
                    final long offset = chunks[i];
                    final long limit = chunks[i + 1];
                    workers[i] = Thread.startVirtualThread(() -> {
                        parseData(data, offset, limit, set, results);
                    });
                }

                for (Thread worker : workers) {
                    worker.join();
                }

                System.out.printf("%d, %d, %d, %d\n",
                        results[3].get(), results[2].get(), results[1].get(), results[0].get());

                System.out.println("Time: " + (System.currentTimeMillis() - start) + " ms");

                System.out.close();

                return;
            }

            // process data in parallel / distributing to workers
            var results = IntStream.range(0, numOfWorkers)
                    .parallel()
                    .mapToObj((i) -> parseData(data, chunks[i], chunks[i + 1], set))
                    .reduce(new Integer[]{0, 0, 0, 0}, (a, b) -> {
                        Integer[] c = new Integer[4];
                        for (int i = 0; i < 4; i++) {
                            c[i] = a[i] + b[i];
                        }
                        return c;
                    });

            // print result
            System.out.printf("%d, %d, %d, %d\n",
                    results[3], results[2], results[1], results[0]);

            if (printRuntime) {
                System.out.println("Time: " + (System.currentTimeMillis() - start) + " ms");
            }

            System.out.close();
        }
    }

    /**
     * Using experimental Java API (Arena) to parse data much quicker.
     * It works by reading the data from the memory segment one character at a time.
     */
    private static Integer[] parseData(final MemorySegment data,
                                       long offset, final long limit,
                                       Set<Integer> set) {
        final Integer[] results = {
                0, // 0 -> 5
                0, // 1 -> 4
                0, // 2 -> 3
                0 // 3 -> 2
        };
        var val = 0;
        var values = new ArrayList<Integer>(5);

        while (offset < limit) {
            var b = data.get(JAVA_BYTE, offset++);
            if (b == '\n') {
                values.add(val);
                if (values.size() == 5) { // only take valid lottery numbers
                    int result = 0;
                    for (int v : values) {
                        if (set.contains(v)) {
                            result++;
                        }
                    }
                    if (result > 1) {
                        results[5 - result]++;
                    }
                }
                val = 0;
                values.clear();
            } else if (b == ' ') {
                // add temp value to the list
                values.add(val);
                val = 0;
            } else {
                // construct values
                val = val * 10 + (b - '0');
            }
        }
        return results;
    }

    /**
     * Using experimental Java API (Arena) to parse data much quicker.
     * It works by reading the data from the memory segment one character at a time.
     */
    private static void parseData(final MemorySegment data,
                                  long offset, final long limit,
                                  Set<Integer> set,
                                  AtomicInteger[] results) {
        var val = 0;
        var values = new ArrayList<Integer>(5);

        while (offset < limit) {
            var b = data.get(JAVA_BYTE, offset++);
            if (b == '\n') {
                values.add(val);
                if (values.size() == 5) { // only take valid lottery numbers
                    int result = 0;
                    for (int v : values) {
                        if (set.contains(v)) {
                            result++;
                        }
                    }
                    if (result > 1) {
                        results[5 - result].incrementAndGet();
                    }
                }
                val = 0;
                values.clear();
            } else if (b == ' ') {
                // add temp value to the list
                values.add(val);
                val = 0;
            } else {
                // construct values
                val = val * 10 + (b - '0');
            }
        }
    }
}
