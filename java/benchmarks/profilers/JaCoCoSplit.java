package org.example.benchmarks.profilers;

import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.SessionInfoStore;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;

public class JaCoCoSplit {

    private static final String JACOCO_MBEAN_NAME = "org.jacoco:type=Runtime";

    public static synchronized void writeExecutionData(String testMethodName, String testClassName,
                                                        String targetSubDir) {
        try {
            MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(JACOCO_MBEAN_NAME);
            // Invoke the dump command with reset (you can set to false to not reset coverage after each dump)
            byte[] executionData = (byte[]) mbsc.invoke(objectName, "getExecutionData", new Object[] { true },
                    new String[] { "boolean" });

            ExecutionDataStore executionDataStore = new ExecutionDataStore();
            SessionInfoStore sessionInfoStore = new SessionInfoStore();

            String destinationDir = targetSubDir + testClassName + "/" + testMethodName + "/";

            File parentExecDir = new File(destinationDir);
            if (!parentExecDir.exists()) {
                parentExecDir.mkdirs();
            }
            File methodExecFile = new File(parentExecDir, "jacoco.exec");

            // Load new data from JMX bytes
            ByteArrayInputStream bis = new ByteArrayInputStream(executionData);
            ExecutionDataReader reader = new ExecutionDataReader(bis);
            reader.setExecutionDataVisitor(executionDataStore);
            reader.setSessionInfoVisitor(sessionInfoStore);
            reader.read();

            // Write merged data back to file atomically
            File tempFile = new File(parentExecDir, "jacoco.exec.tmp");
            FileOutputStream fos = new FileOutputStream(tempFile);
            ExecutionDataWriter writer = new ExecutionDataWriter(fos);
            executionDataStore.accept(writer);
            sessionInfoStore.accept(writer);
            fos.close();

            // Atomic rename to prevent partial reads
            tempFile.renameTo(methodExecFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}