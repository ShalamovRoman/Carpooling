/*
 * (C) Copyright 2015-2017, by Joris Kinable and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.perf.graph;

import java.util.*;
import java.util.concurrent.*;

import org.jgrapht.alg.*;
import org.jgrapht.alg.flow.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.specifics.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import junit.framework.*;

/**
 * Benchmark class to compare different graph implementations. The benchmark creates a graph, runs
 * various algorithms on the graph and finally destroys (part of) the graph. This is an attempt to
 * simulate common usage of the graph.
 *
 * Note: Currently the tests are performed on a single graph. It would be better to run it on
 * multiple graphs. Not sure how to achieve that through the JMH framework.
 */
public class GraphPerformanceTest
    extends TestCase
{

    public static final int PERF_BENCHMARK_VERTICES_COUNT = 1000;
    public static final int PERF_BENCHMARK_EDGES_COUNT = 100000;
    public static final long SEED = 1446523573696201013l;
    public static final int NR_GRAPHS = 5; // Number of unique graphs on which the tests are
                                           // repeated

    @State(Scope.Benchmark)
    private static abstract class DirectedGraphBenchmarkBase
    {

        private Blackhole blackhole;
        protected GnmRandomGraphGenerator<Integer, DefaultWeightedEdge> rgg;
        private SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph;

        /**
         * Creates a random graph using the Random Graph Generator
         * 
         * @return random graph
         */
        abstract SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> constructGraph();

        @Setup
        public void setup()
        {
            blackhole = new Blackhole();
        }

        /**
         * Benchmark 1: graph construction
         */
        @Benchmark
        public void generateGraphBenchmark()
        {
            for (int i = 0; i < NR_GRAPHS; i++) {
                rgg = new GnmRandomGraphGenerator<>(
                    PERF_BENCHMARK_VERTICES_COUNT, PERF_BENCHMARK_EDGES_COUNT, SEED + i);
                // Create a graph
                graph = constructGraph();

            }
        }

        /**
         * Benchmark 2: Simulate graph usage: Create a graph, perform various algorithms, partially
         * destroy graph
         */
        @Benchmark
        public void graphPerformanceBenchmark()
        {
            for (int i = 0; i < NR_GRAPHS; i++) {
                rgg = new GnmRandomGraphGenerator<>(
                    PERF_BENCHMARK_VERTICES_COUNT, PERF_BENCHMARK_EDGES_COUNT, SEED + i);
                // Create a graph
                graph = constructGraph();

                Integer[] vertices =
                    graph.vertexSet().toArray(new Integer[graph.vertexSet().size()]);
                Integer source = vertices[0];
                Integer sink = vertices[vertices.length - 1];

                // Run various algorithms on the graph
                double length = this.calculateShorestPath(graph, source, sink);
                blackhole.consume(length);

                double maxFlow = this.calculateMaxFlow(graph, source, sink);
                blackhole.consume(maxFlow);

                boolean isStronglyConnected = this.isStronglyConnected(graph);
                blackhole.consume(isStronglyConnected);

                // Destroy some random edges in the graph
                destroyRandomEdges(graph);
            }
        }

        private double calculateShorestPath(
            SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph, Integer source,
            Integer sink)
        {
            DijkstraShortestPath<Integer, DefaultWeightedEdge> shortestPathAlg =
                new DijkstraShortestPath<>(graph);
            return shortestPathAlg.getPath(source, sink).getWeight();
        }

        private double calculateMaxFlow(
            SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph, Integer source,
            Integer sink)
        {
            EdmondsKarpMFImpl<Integer, DefaultWeightedEdge> maximumFlowAlg =
                new EdmondsKarpMFImpl<>(graph);
            return maximumFlowAlg.getMaximumFlow(source, sink).getValue();
        }

        private boolean isStronglyConnected(
            SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph)
        {
            StrongConnectivityAlgorithm<Integer, DefaultWeightedEdge> strongConnectivityAlg =
                new GabowStrongConnectivityInspector<>(graph);
            return strongConnectivityAlg.isStronglyConnected();
        }

        private void destroyRandomEdges(
            SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph)
        {
            int nrVertices = graph.vertexSet().size();
            Random rand = new Random(SEED);
            for (int i = 0; i < PERF_BENCHMARK_EDGES_COUNT / 2; i++) {
                int u = rand.nextInt(nrVertices);
                int v = rand.nextInt(nrVertices);
                graph.removeEdge(u, v);
            }
        }

    }

    /**
     * Graph class which relies on the (legacy) DirectedSpecifics implementation. This class is
     * optimized for low memory usage, but performs edge retrieval operations fairly slow.
     */
    public static class MemoryEfficientDirectedGraphBenchmark
        extends DirectedGraphBenchmarkBase
    {
        @Override
        SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> constructGraph()
        {
            SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
                new MemoryEfficientDirectedWeightedGraph<>(DefaultWeightedEdge.class);
            rgg.generateGraph(graph, new IntegerVertexFactory(1), null);
            return graph;
        }
    }

    /**
     * Graph class which relies on the FastLookupDirectedSpecifics. This class is optimized to
     * perform quick edge retrievals.
     */
    public static class FastLookupDirectedGraphBenchmark
        extends DirectedGraphBenchmarkBase
    {
        @Override
        SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> constructGraph()
        {
            SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
                new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
            rgg.generateGraph(graph, new IntegerVertexFactory(1), null);
            return graph;
        }
    }

    public void testRandomGraphBenchmark()
        throws RunnerException
    {
        Options opt = new OptionsBuilder()
            .include(".*" + MemoryEfficientDirectedGraphBenchmark.class.getSimpleName() + ".*")
            .include(".*" + FastLookupDirectedGraphBenchmark.class.getSimpleName() + ".*")

            .mode(Mode.AverageTime).timeUnit(TimeUnit.MILLISECONDS)
            // .warmupTime(TimeValue.seconds(1))
            .warmupIterations(3)
            // .measurementTime(TimeValue.seconds(1))
            .measurementIterations(5).forks(1).shouldFailOnError(true).shouldDoGC(true).build();

        new Runner(opt).run();
    }

    /**
     * Creates an memory efficient graph implementation.
     * 
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    public static class MemoryEfficientDirectedWeightedGraph<V, E>
        extends SimpleDirectedWeightedGraph<V, E>
    {
        private static final long serialVersionUID = -1826738982402033648L;

        public MemoryEfficientDirectedWeightedGraph(Class<? extends E> edgeClass)
        {
            super(edgeClass);
        }

        @Override
        protected Specifics<V, E> createSpecifics()
        {
            return new DirectedSpecifics<>(this);
        }
    }
}
