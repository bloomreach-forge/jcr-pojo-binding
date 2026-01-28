/*
 *  Copyright 2015-2025 Bloomreach (https://www.bloomreach.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.forge.content.pojo.binder.jcr;

import java.util.concurrent.TimeUnit;

import org.onehippo.forge.content.pojo.model.ContentNode;
import org.onehippo.repository.mock.MockNode;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * JMH benchmark for {@link DefaultJcrContentNodeBinder}.
 *
 * Demonstrates linear O(J+C) scaling after optimization.
 * If scaling is linear, doubling node count should roughly double the time.
 * If scaling were quadratic O(C×J), doubling would quadruple the time.
 *
 * Run with: mvn test -Dtest=DefaultJcrContentNodeBinderBenchmark#runBenchmark
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class DefaultJcrContentNodeBinderBenchmark {

    private static final String COMPOUND_TYPE = "hippo:compound";
    private static final String NON_COMPOUND_TYPE = "nt:unstructured";

    @Param({"10", "50", "100", "200", "500"})
    private int nodeCount;

    private DefaultJcrContentNodeBinder binder;
    private MockNode jcrParentNode;
    private ContentNode contentNode;

    @Setup(Level.Invocation)
    public void setup() throws Exception {
        binder = new DefaultJcrContentNodeBinder();
        jcrParentNode = createJcrNodeWithChildren(nodeCount);
        contentNode = createContentNodeWithChildren(nodeCount);
    }

    @Benchmark
    public void benchmarkDefaultMode() throws Exception {
        // Default mode: removeSubNodes + addSubNodes
        binder.setSubNodesMergingOnly(false);
        binder.bind(jcrParentNode, contentNode);
    }

    @Benchmark
    public void benchmarkMergeMode() throws Exception {
        // Merge mode: mergeSubNodes
        binder.setSubNodesMergingOnly(true);
        binder.bind(jcrParentNode, contentNode);
    }

    private MockNode createJcrNodeWithChildren(int count) throws Exception {
        MockNode root = MockNode.root();
        MockNode parent = root.addNode("parent", "nt:unstructured");

        // Add mix of compound and non-compound children
        for (int i = 0; i < count; i++) {
            String name = "child" + i;
            String type = (i % 3 == 0) ? COMPOUND_TYPE : NON_COMPOUND_TYPE;
            MockNode child = parent.addNode(name, type);

            // Add some properties
            child.setProperty("prop1", "value" + i);
            child.setProperty("prop2", i);
        }

        return parent;
    }

    private ContentNode createContentNodeWithChildren(int count) {
        ContentNode parent = new ContentNode("parent", "nt:unstructured");

        // Create matching content children
        for (int i = 0; i < count; i++) {
            String name = "child" + i;
            String type = (i % 3 == 0) ? COMPOUND_TYPE : NON_COMPOUND_TYPE;
            ContentNode child = new ContentNode(name, type);

            // Add some properties
            child.setProperty("prop1", "newvalue" + i);
            child.setProperty("prop2", String.valueOf(i * 2));

            parent.addNode(child);
        }

        return parent;
    }

    /**
     * Run this test method to execute the benchmark.
     * Results show average time per operation at each node count.
     *
     * Linear scaling (O(J+C)): time ratio ≈ node count ratio
     * Quadratic scaling (O(C×J)): time ratio ≈ (node count ratio)²
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DefaultJcrContentNodeBinderBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

    /**
     * JUnit entry point for running benchmark via Maven.
     * Results are saved to ~/Documents/jcr-binder-benchmark-results.json
     */
    @org.junit.Test
    public void runBenchmark() throws RunnerException {
        String outputPath = System.getProperty("user.home") + "/Documents/jcr-binder-benchmark-OPTIMIZED.json";

        Options opt = new OptionsBuilder()
                .include(DefaultJcrContentNodeBinderBenchmark.class.getSimpleName())
                .result(outputPath)
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();

        System.out.println("\n=== Benchmark results saved to: " + outputPath + " ===\n");
    }
}
