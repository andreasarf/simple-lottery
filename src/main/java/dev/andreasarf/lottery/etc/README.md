# Other Approaches

This package contains other approaches to solve the problem that is still working-in-progress, failed, and/or not feasible.

## Approaches
1. Main2:
   - Using basic/standard Java library: `FileChannel`.
   - It works by dividing the file into chunks and then read the chunks.
   - It is not working as expected because it read more data than the expected.
   - On M1 Macbook Air with 8-core CPU & 8GB RAM, it processes 10M of row in ~1.3 seconds.
