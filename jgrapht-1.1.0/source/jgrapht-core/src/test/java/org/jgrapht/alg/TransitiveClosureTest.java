/*
 * (C) Copyright 2007-2017, by Vinayak R Borkar and Contributors.
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
package org.jgrapht.alg;

import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;

import junit.framework.*;

/**
 */
public class TransitiveClosureTest
    extends TestCase
{
    // ~ Methods ----------------------------------------------------------------

    public void testLinearGraph()
    {
        SimpleDirectedGraph<Integer, DefaultEdge> graph =
            new SimpleDirectedGraph<>(DefaultEdge.class);

        int N = 10;
        LinearGraphGenerator<Integer, DefaultEdge> gen = new LinearGraphGenerator<>(N);
        gen.generateGraph(graph, new IntegerVertexFactory(), null);
        TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(graph);

        assertEquals(true, graph.edgeSet().size() == ((N * (N - 1)) / 2));
        for (int i = 0; i < N; ++i) {
            for (int j = i + 1; j < N; ++j) {
                assertEquals(true, graph.getEdge(i, j) != null);
            }
        }
    }

    public void testRingGraph()
    {
        SimpleDirectedGraph<Integer, DefaultEdge> graph =
            new SimpleDirectedGraph<>(DefaultEdge.class);

        int N = 10;
        RingGraphGenerator<Integer, DefaultEdge> gen = new RingGraphGenerator<>(N);
        gen.generateGraph(graph, new IntegerVertexFactory(), null);
        TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(graph);

        assertEquals(true, graph.edgeSet().size() == (N * (N - 1)));
        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < N; ++j) {
                assertEquals(true, (i == j) || (graph.getEdge(i, j) != null));
            }
        }
    }

    public void testNoVerticesDag()
    {
        DirectedAcyclicGraph<Integer, DefaultEdge> graph =
            new DirectedAcyclicGraph<>(DefaultEdge.class);

        TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(graph);

        assertEquals(0, graph.edgeSet().size());
    }

    public void testEmptyDag()
    {
        DirectedAcyclicGraph<Integer, DefaultEdge> graph =
            new DirectedAcyclicGraph<>(DefaultEdge.class);
        int n = 10;
        EmptyGraphGenerator<Integer, DefaultEdge> gen = new EmptyGraphGenerator<>(n);
        gen.generateGraph(graph, new IntegerVertexFactory(), null);

        TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(graph);

        assertEquals(0, graph.edgeSet().size());
    }

    public void testCompleteBipartiteDag()
    {
        DirectedAcyclicGraph<Integer, DefaultEdge> graph =
            new DirectedAcyclicGraph<>(DefaultEdge.class);
        CompleteBipartiteGraphGenerator<Integer, DefaultEdge> gen =
            new CompleteBipartiteGraphGenerator<>(5, 5);
        gen.generateGraph(graph, new IntegerVertexFactory(), null);

        TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(graph);

        assertEquals(25, graph.edgeSet().size());
    }

    public void testLinearGraphForDag()
    {
        DirectedAcyclicGraph<Integer, DefaultEdge> graph =
            new DirectedAcyclicGraph<>(DefaultEdge.class);
        int n = 10;
        LinearGraphGenerator<Integer, DefaultEdge> gen = new LinearGraphGenerator<>(n);
        gen.generateGraph(graph, new IntegerVertexFactory(), null);

        TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(graph);

        assertEquals((n * (n - 1)) / 2, graph.edgeSet().size());
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                assertNotNull(graph.getEdge(i, j));
            }
        }
    }
}

// End TransitiveClosureTest.java
