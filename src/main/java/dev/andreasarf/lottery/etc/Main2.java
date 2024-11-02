package dev.andreasarf.lottery.etc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class Main2 {
    private static final String FILE = "10m-v2.txt";

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();

        System.out.println("READY");

        final Path p = Paths.get(FILE);
        final int numOfWorkers = Runtime.getRuntime().availableProcessors();
        final Set<Integer> set = Set.of(4, 79, 13, 80, 56);

        System.out.println("Number of workers: " + numOfWorkers);

        final long[] cuts = cuts(p, numOfWorkers);

        var results = IntStream.range(0, cuts.length - 1)
                .parallel()
                .mapToObj((i) -> stats(p, cuts[i], cuts[i + 1], set))
                .reduce(new Integer[] {0, 0, 0, 0}, (a, b) -> {
//                    System.out.println(Arrays.toString(a) + " + " + Arrays.toString(b));
                    Integer[] c = new Integer[4];
                    for (int i = 0; i < 4; i++) {
                        c[i] = a[i] + b[i];
                    }
                    return c;
                });

        // print result
        System.out.printf("%d, %d, %d, %d\n",
                results[3], results[2], results[1], results[0]);

        System.out.println("Time: " + (System.currentTimeMillis() - start) + " ms");

        System.out.close();
    }

    private static final int ARRAY_SIZE = 1 << 20;
    private static final int CHUNK_SIZE = 32 << 20;

    private static Integer[] stats(Path p, long start, long end, Set<Integer> set) {
        final Integer[] results = {
                0, // 0 -> 5
                0, // 1 -> 4
                0, // 2 -> 3
                0 // 3 -> 2
        };

        try (final var channel = (FileChannel) Files.newByteChannel(p, EnumSet.of(StandardOpenOption.READ))) {
            channel.position(start);
            var offset = start;
            final byte[] array = new byte[ARRAY_SIZE];
            // good enough for an integer
            byte[] strbuff = new byte[128];
            int strbuffIx = 0;
            final var buffer = ByteBuffer.allocateDirect(CHUNK_SIZE);
            List<Integer> values = new ArrayList<>(5);
//            int counter = 0;
            while (offset < end) {
                final int limit = channel.read(buffer);
                if (limit <= 0)
                    break;
                offset += limit;
                int totalRead = 0;
                buffer.flip();
                while (totalRead < limit) {
                    int read = Math.min(array.length, limit - totalRead);
                    buffer.get(array, 0, read);
                    totalRead += read;

//                    System.out.print(new String(array, "utf8"));

                    for (int i = 0; i < read; i++) {
                        strbuff[strbuffIx++] = array[i]; // copy byte to buffer
                        if (array[i] == ' ' || array[i] == '\n') {
                            var str = new String(strbuff, 0, strbuffIx - 1, "utf8");
                            strbuffIx = 0; // reset index
//                            System.out.println("> " + str);

                            if (str.isBlank()) {
                                values.clear();
                                continue;
                            }

                            int value = Integer.parseInt(str);
                            values.add(value);

                            if (array[i] == '\n') {
//                                counter++;
                                // check result
                                if (values.size() == 5) {
//                                    System.out.println(values);
                                    int result = 0;
                                    for (int val : values) {
                                        if (set.contains(val)) {
                                            result++;
                                        }
                                    }
                                    if (result > 1) {
                                        results[5 - result]++;
                                    }
                                } else {
//                                    System.out.println("Invalid line: " + values);
                                }
                                values.clear();
                            }

                        }
                    }
                }
                buffer.rewind();
            }
//            System.out.println(counter);
            return results;
        }
        catch (IOException err) {
            return null;
        }
    }

    /**
     * Computes good enough partitions which end on a newline
     */
    private static long[] cuts(Path p, int workers) throws IOException {
        var channel = (FileChannel) Files.newByteChannel(p, EnumSet.of(StandardOpenOption.READ));
        final long size = channel.size();

//        System.out.println("File size: " + size);

        if (size < 10_000L) {
            return new long[]{ 0L, size };
        }
        long chunk = size / workers;
        long position = size - chunk;

        long[] cuts = new long[workers + 1];
        cuts[workers] = size;
        // 1024 should cover enough to catch a newline
        var buf = ByteBuffer.allocateDirect(1024);
        byte[] bytes = new byte[1024];

        while (workers-- > 0) {
            var read = channel.read(buf, position);
            buf.flip();
            buf.get(bytes, 0, read);
            var nextNL = position;
            while (read-- > 0) {
                if (bytes[read] == '\n') {
                    nextNL += read;
                    cuts[workers] = nextNL;
                    break;
                }
            }
            position -= chunk;
            buf.rewind();
        }
        cuts[0] = 0L;

        channel.close();

        return cuts;
    }

}
