package benchmarks.profilers;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.runner.IterationType;
import java.util.Collection;
import java.util.Collections;

public class JaCoCoProfiler implements InternalProfiler {
    @Override
    public String getDescription() {
        return "My custom listener-like profiler";
    }

    @Override
    public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {
        IterationType type = iterationParams.getType();

        if (type == IterationType.MEASUREMENT) {
            iterationCounter++;
        }
    }

    @Override
    public Collection<? extends Result<?>> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams,
                                                          IterationResult result) {
        System.out.println("Iteration finished for " + benchmarkParams.getBenchmark());

        String benchmarkFqn = benchmarkParams.getBenchmark();

        IterationType type = iterationParams.getType();

        if (type == IterationType.MEASUREMENT && iterationCounter == iterationParams.getCount()) {
            int lastDot = benchmarkFqn.lastIndexOf('.');
            String className = benchmarkFqn.substring(0, lastDot);
            String methodName = benchmarkFqn.substring(lastDot + 1);

            if (!benchmarkParams.getParamsKeys().isEmpty()) {
                StringBuilder params = new StringBuilder();
                params.append("#");
                for (String key : benchmarkParams.getParamsKeys()) {
                    String param = key + "=" + benchmarkParams.getParam(key);
                    params.append(param);
                    params.append("_");
                }

                int lastUnderscore = params.lastIndexOf("_");

                if (lastUnderscore != -1) {
                    String pam = params.substring(0, lastUnderscore);
                    methodName = methodName + pam;
                } else {
                    methodName = methodName + params;
                }

            }

            JaCoCoCoverageMatrix.updateCoverageMatrix(methodName, className, "reports-coverage/jmh/", "build/jacoco/bench.exec", "build/classes/java/main");
        }

        return Collections.emptyList();
    }

    private int iterationCounter = 0;
}

