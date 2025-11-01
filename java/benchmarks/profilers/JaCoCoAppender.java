package benchmarks.profilers;

import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfoStore;

import java.io.*;

public class JaCoCoAppender {

    public static void appendExecutionData(byte[] newData, File execFile) throws IOException {
        ExecutionDataStore executionDataStore = new ExecutionDataStore();
        SessionInfoStore sessionInfoStore = new SessionInfoStore();

        // 1. Load existing data from file (if it exists)
        if (execFile.exists()) {
            try (FileInputStream fis = new FileInputStream(execFile)) {
                ExecutionDataReader reader = new ExecutionDataReader(fis);
                reader.setExecutionDataVisitor(executionDataStore);
                reader.setSessionInfoVisitor(sessionInfoStore);
                reader.read();
            }
        }

        // 2. Load new data from JMX bytes
        try (ByteArrayInputStream bis = new ByteArrayInputStream(newData)) {
            ExecutionDataReader reader = new ExecutionDataReader(bis);
            reader.setExecutionDataVisitor(executionDataStore);
            reader.setSessionInfoVisitor(sessionInfoStore);
            reader.read();
        }

        // 3. Write merged data back to file
        try (FileOutputStream fos = new FileOutputStream(execFile)) {
            ExecutionDataWriter writer = new ExecutionDataWriter(fos);
            executionDataStore.accept(writer);
            sessionInfoStore.accept(writer);
        }
    }
}
