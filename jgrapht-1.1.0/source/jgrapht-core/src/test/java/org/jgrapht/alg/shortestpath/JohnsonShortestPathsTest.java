/*
 * (C) Copyright 2017-2017, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.shortestpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.function.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 * @author Dimitrios Michail
 */
public class JohnsonShortestPathsTest
{

    private VertexFactory<String> vertexFactory = new VertexFactory<String>()
    {
        private int i = 0;

        @Override
        public String createVertex()
        {
            return "vertex" + i++;
        }
    };

    @Test
    public void testIssue408()
    {
        Graph<Integer, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(graph, Arrays.asList(0, 1, 2, 3, 4, 5, 6));
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);

        graph.addEdge(4, 5);
        graph.addEdge(5, 6);
        graph.addEdge(6, 4);

        JohnsonShortestPaths<Integer, DefaultEdge> sp =
            new JohnsonShortestPaths<>(graph, new IntegerVertexFactory(7));

        assertEquals(2.0, sp.getPathWeight(0, 2), 0.0);
        assertEquals(1.0, sp.getPathWeight(4, 5), 0.0);
        assertTrue(Double.isInfinite(sp.getPathWeight(3, 4)));
    }

    @Test
    public void testWikipediaExample()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex("w");
        g.addVertex("y");
        g.addVertex("x");
        g.addVertex("z");
        g.setEdgeWeight(g.addEdge("w", "z"), 2);
        g.setEdgeWeight(g.addEdge("y", "w"), 4);
        g.setEdgeWeight(g.addEdge("x", "w"), 6);
        g.setEdgeWeight(g.addEdge("x", "y"), 3);
        g.setEdgeWeight(g.addEdge("z", "x"), -7);
        g.setEdgeWeight(g.addEdge("y", "z"), 5);
        g.setEdgeWeight(g.addEdge("z", "y"), -3);

        JohnsonShortestPaths<String, DefaultWeightedEdge> alg =
            new JohnsonShortestPaths<>(g, vertexFactory);
        assertEquals(-1d, alg.getPathWeight("z", "w"), 1e-9);
        assertEquals(-4d, alg.getPathWeight("z", "y"), 1e-9);
        assertEquals(0, alg.getPathWeight("z", "z"), 1e-9);
        assertEquals(-7, alg.getPathWeight("z", "x"), 1e-9);
    }

    @Test
    public void testRandomGraphsCompareWithFloydWarshall()
    {
        final int tests = 20;
        final int n = 50;
        final double p = 0.55;

        Random rng = new Random();

        List<Supplier<Graph<Integer, DefaultWeightedEdge>>> graphs = new ArrayList<>();
        graphs.add(() -> new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class));

        for (Supplier<Graph<Integer, DefaultWeightedEdge>> gSupplier : graphs) {
            GraphGenerator<Integer, DefaultWeightedEdge, Integer> gen =
                new GnpRandomGraphGenerator<>(n, p, rng, false);
            for (int i = 0; i < tests; i++) {
                Graph<Integer, DefaultWeightedEdge> g = gSupplier.get();
                IntegerVertexFactory vertexFactory = new IntegerVertexFactory();
                gen.generateGraph(g, vertexFactory, null);

                // assign weights
                for (DefaultWeightedEdge e : g.edgeSet()) {
                    Integer u = g.getEdgeSource(e);
                    Integer v = g.getEdgeTarget(e);
                    double rWeight;
                    if (u >= v) {
                        rWeight = (n + 1) + 2 * (n + 1) * rng.nextDouble();
                    } else {
                        rWeight = rng.nextDouble();
                        if (rng.nextDouble() < 0.5) {
                            rWeight *= -1;
                        }
                    }
                    g.setEdgeWeight(e, rWeight);
                }

                try {
                    // run Johnson algorithm
                    JohnsonShortestPaths<Integer, DefaultWeightedEdge> fw =
                        new JohnsonShortestPaths<>(g, vertexFactory);

                    // run Floyd-Warshall algorithm
                    FloydWarshallShortestPaths<Integer, DefaultWeightedEdge> fw1 =
                        new FloydWarshallShortestPaths<>(g);

                    for (Integer v : g.vertexSet()) {
                        for (Integer u : g.vertexSet()) {
                            // compare with Dijkstra
                            assertEquals(
                                fw1.getPath(v, u).getWeight(), fw.getPath(v, u).getWeight(), 1e-9);
                        }
                    }
                } catch (RuntimeException e) {
                    // negative weight cycle, skip test
                    assertEquals("Graph contains a negative-weight cycle", e.getMessage());
                }
            }
        }

    }

}
