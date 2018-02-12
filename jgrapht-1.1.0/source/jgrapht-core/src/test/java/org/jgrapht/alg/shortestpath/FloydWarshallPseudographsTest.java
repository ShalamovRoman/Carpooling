/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.function.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 * Test {@link FloydWarshallShortestPaths} on pseudo graphs.
 * 
 * @author Dimitrios Michail
 */
public class FloydWarshallPseudographsTest
{

    @Test
    public void testRandomGraphs()
    {
        final int tests = 20;
        final int n = 50;
        final double p = 0.85;

        Random rng = new Random();

        List<Supplier<Graph<Integer, DefaultWeightedEdge>>> graphs = new ArrayList<>();
        graphs.add(() -> new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class));
        graphs.add(() -> new WeightedPseudograph<>(DefaultWeightedEdge.class));

        for (Supplier<Graph<Integer, DefaultWeightedEdge>> gSupplier : graphs) {
            GraphGenerator<Integer, DefaultWeightedEdge, Integer> gen =
                new GnpRandomGraphGenerator<>(n, p, rng, true);
            for (int i = 0; i < tests; i++) {
                Graph<Integer, DefaultWeightedEdge> g = gSupplier.get();
                gen.generateGraph(g, new IntegerVertexFactory(), null);

                // assign random weights
                for (DefaultWeightedEdge e : g.edgeSet()) {
                    g.setEdgeWeight(e, rng.nextDouble());
                }

                // for each vertex add more edges
                Integer[] allVertices = g.vertexSet().toArray(new Integer[0]);
                for (Integer v : g.vertexSet()) {
                    for (int j = 0; j < n * p; j++) {
                        Integer u = allVertices[rng.nextInt(n)];
                        DefaultWeightedEdge e = new DefaultWeightedEdge();
                        g.addEdge(v, u, e);
                        g.setEdgeWeight(e, rng.nextDouble());
                    }
                }

                // run one Floyd-Warshall
                FloydWarshallShortestPaths<Integer, DefaultWeightedEdge> fw =
                    new FloydWarshallShortestPaths<>(g);

                for (Integer v : g.vertexSet()) {
                    // run Dijkstra
                    SingleSourcePaths<Integer, DefaultWeightedEdge> dTree =
                        new DijkstraShortestPath<>(g).getPaths(v);

                    SingleSourcePaths<Integer, DefaultWeightedEdge> fwTree = fw.getPaths(v);

                    for (Integer u : g.vertexSet()) {
                        // compare with Dijkstra
                        assertEquals(dTree.getWeight(u), fw.getPath(v, u).getWeight(), 1e-9);

                        // Test getPath method w.r.t getPathsTree
                        assertEquals(
                            fwTree.getPath(u).getEdgeList(), fw.getPath(v, u).getEdgeList());
                    }
                }

            }
        }

    }

    @Test
    public void test1()
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
        DefaultWeightedEdge e12_1 = g.addEdge(1, 2);
        g.setEdgeWeight(e12_1, -5.0);
        DefaultWeightedEdge e12_2 = g.addEdge(1, 2);
        g.setEdgeWeight(e12_2, -2.0);
        DefaultWeightedEdge e12_3 = g.addEdge(1, 2);
        g.setEdgeWeight(e12_3, 1.0);
        DefaultWeightedEdge e23_1 = g.addEdge(2, 3);
        g.setEdgeWeight(e23_1, 0d);
        DefaultWeightedEdge e23_2 = g.addEdge(2, 3);
        g.setEdgeWeight(e23_2, -2.0);
        DefaultWeightedEdge e23_3 = g.addEdge(2, 3);
        g.setEdgeWeight(e23_3, -5.0);
        DefaultWeightedEdge e34_1 = g.addEdge(3, 4);
        g.setEdgeWeight(e34_1, -100.0);
        DefaultWeightedEdge e34_2 = g.addEdge(3, 4);
        g.setEdgeWeight(e34_2, 100.0);
        DefaultWeightedEdge e34_3 = g.addEdge(3, 4);
        g.setEdgeWeight(e34_3, 1.0);

        FloydWarshallShortestPaths<Integer, DefaultWeightedEdge> fw =
            new FloydWarshallShortestPaths<>(g);

        SingleSourcePaths<Integer, DefaultWeightedEdge> t1 = fw.getPaths(1);
        assertEquals(0d, t1.getWeight(1), 1e-9);
        assertTrue(t1.getPath(1).getEdgeList().isEmpty());
        assertEquals(Arrays.asList(g.getEdgeSource(e12_1)), t1.getPath(1).getVertexList());
        assertEquals(-5d, t1.getWeight(2), 1e-9);
        assertEquals(Arrays.asList(e12_1), t1.getPath(2).getEdgeList());
        assertEquals(-10d, t1.getWeight(3), 1e-9);
        assertEquals(Arrays.asList(e12_1, e23_3), t1.getPath(3).getEdgeList());
        assertEquals(-110d, t1.getWeight(4), 1e-9);
        assertEquals(Arrays.asList(e12_1, e23_3, e34_1), t1.getPath(4).getEdgeList());

        SingleSourcePaths<Integer, DefaultWeightedEdge> t2 = fw.getPaths(2);
        assertEquals(Double.POSITIVE_INFINITY, t2.getWeight(1), 1e-9);
        assertNull(t2.getPath(1));
        assertEquals(0d, t2.getWeight(2), 1e-9);
        assertTrue(t2.getPath(2).getEdgeList().isEmpty());
        assertEquals(Arrays.asList(g.getEdgeSource(e23_1)), t2.getPath(2).getVertexList());
        assertEquals(-5d, t2.getWeight(3), 1e-9);
        assertEquals(Arrays.asList(e23_3), t2.getPath(3).getEdgeList());
        assertEquals(-105d, t2.getWeight(4), 1e-9);
        assertEquals(Arrays.asList(e23_3, e34_1), t2.getPath(4).getEdgeList());

        SingleSourcePaths<Integer, DefaultWeightedEdge> t3 = fw.getPaths(3);
        assertEquals(Double.POSITIVE_INFINITY, t3.getWeight(1), 1e-9);
        assertNull(t3.getPath(1));
        assertEquals(Double.POSITIVE_INFINITY, t3.getWeight(2), 1e-9);
        assertNull(t3.getPath(2));
        assertEquals(0d, t3.getWeight(3), 1e-9);
        assertTrue(t3.getPath(3).getEdgeList().isEmpty());
        assertEquals(-100d, t3.getWeight(4), 1e-9);
        assertEquals(Arrays.asList(e34_1), t3.getPath(4).getEdgeList());

        SingleSourcePaths<Integer, DefaultWeightedEdge> t4 = fw.getPaths(4);
        assertNull(t4.getPath(1));
        assertNull(t4.getPath(2));
        assertNull(t4.getPath(3));
        assertEquals(0d, t4.getWeight(4), 1e-9);
        assertTrue(t4.getPath(4).getEdgeList().isEmpty());

        // test diameter
        assertEquals(Double.POSITIVE_INFINITY, fw.getDiameter(), 1e-9);
        // test shortest path count
        assertEquals(6, fw.getShortestPathsCount());

        // test first hop
        assertNull(fw.getFirstHop(1, 1));
        assertEquals(2, fw.getFirstHop(1, 2).intValue());
        assertEquals(2, fw.getFirstHop(1, 3).intValue());
        assertEquals(2, fw.getFirstHop(1, 4).intValue());
        assertNull(fw.getFirstHop(2, 1));
        assertNull(fw.getFirstHop(2, 2));
        assertEquals(3, fw.getFirstHop(2, 3).intValue());
        assertEquals(3, fw.getFirstHop(2, 4).intValue());
        assertNull(fw.getFirstHop(3, 1));
        assertNull(fw.getFirstHop(3, 2));
        assertNull(fw.getFirstHop(3, 3));
        assertEquals(4, fw.getFirstHop(3, 4).intValue());
        assertNull(fw.getFirstHop(4, 1));
        assertNull(fw.getFirstHop(4, 2));
        assertNull(fw.getFirstHop(4, 3));
        assertNull(fw.getFirstHop(4, 4));

        // test last hop
        assertNull(fw.getLastHop(1, 1));
        assertEquals(1, fw.getLastHop(1, 2).intValue());
        assertEquals(2, fw.getLastHop(1, 3).intValue());
        assertEquals(3, fw.getLastHop(1, 4).intValue());
        assertNull(fw.getLastHop(2, 1));
        assertNull(fw.getLastHop(2, 2));
        assertEquals(2, fw.getLastHop(2, 3).intValue());
        assertEquals(3, fw.getLastHop(2, 4).intValue());
        assertNull(fw.getLastHop(3, 1));
        assertNull(fw.getLastHop(3, 2));
        assertNull(fw.getLastHop(3, 3));
        assertEquals(3, fw.getLastHop(3, 4).intValue());
        assertNull(fw.getLastHop(4, 1));
        assertNull(fw.getLastHop(4, 2));
        assertNull(fw.getLastHop(4, 3));
        assertNull(fw.getLastHop(4, 4));

    }

    @Test
    public void testGetPathWeight()
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
        DefaultWeightedEdge e12_1 = g.addEdge(1, 2);
        g.setEdgeWeight(e12_1, -5.0);
        DefaultWeightedEdge e12_2 = g.addEdge(1, 2);
        g.setEdgeWeight(e12_2, -2.0);
        DefaultWeightedEdge e12_3 = g.addEdge(1, 2);
        g.setEdgeWeight(e12_3, 1.0);
        DefaultWeightedEdge e23_1 = g.addEdge(2, 3);
        g.setEdgeWeight(e23_1, 0d);
        DefaultWeightedEdge e23_2 = g.addEdge(2, 3);
        g.setEdgeWeight(e23_2, -2.0);
        DefaultWeightedEdge e23_3 = g.addEdge(2, 3);
        g.setEdgeWeight(e23_3, -5.0);
        DefaultWeightedEdge e34_1 = g.addEdge(3, 4);
        g.setEdgeWeight(e34_1, -100.0);
        DefaultWeightedEdge e34_2 = g.addEdge(3, 4);
        g.setEdgeWeight(e34_2, 100.0);
        DefaultWeightedEdge e34_3 = g.addEdge(3, 4);
        g.setEdgeWeight(e34_3, 1.0);

        FloydWarshallShortestPaths<Integer, DefaultWeightedEdge> fw =
            new FloydWarshallShortestPaths<>(g);

        assertEquals(Double.POSITIVE_INFINITY, fw.getPathWeight(2, 1), 1e-9);
        assertEquals(0d, fw.getPathWeight(2, 2), 1e-9);
        assertEquals(-5d, fw.getPathWeight(2, 3), 1e-9);
        assertEquals(-105d, fw.getPathWeight(2, 4), 1e-9);
    }

    @Test
    public void testLoops()
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2));
        DefaultWeightedEdge e12_1 = g.addEdge(1, 2);
        g.setEdgeWeight(e12_1, 5.0);
        DefaultWeightedEdge e21_1 = g.addEdge(2, 1);
        g.setEdgeWeight(e21_1, 15.0);

        g.addEdge(1, 1);
        g.addEdge(1, 1);
        g.addEdge(1, 1);
        g.addEdge(2, 2);
        g.addEdge(2, 2);
        g.addEdge(2, 2);

        FloydWarshallShortestPaths<Integer, DefaultWeightedEdge> fw =
            new FloydWarshallShortestPaths<>(g);

        GraphPath<Integer, DefaultWeightedEdge> p1 = fw.getPath(1, 1);
        assertEquals(1, p1.getStartVertex().intValue());
        assertEquals(1, p1.getEndVertex().intValue());
        assertEquals(0, p1.getLength());
        assertEquals(0d, p1.getWeight(), 1e-9);
        assertTrue(p1.getEdgeList().isEmpty());
        assertEquals(1, p1.getVertexList().size());
        assertEquals(1, p1.getVertexList().get(0).intValue());

        GraphPath<Integer, DefaultWeightedEdge> p2 = fw.getPath(2, 2);
        assertEquals(2, p2.getStartVertex().intValue());
        assertEquals(2, p2.getEndVertex().intValue());
        assertEquals(0, p2.getLength());
        assertEquals(0d, p2.getWeight(), 1e-9);
        assertTrue(p2.getEdgeList().isEmpty());
        assertEquals(1, p2.getVertexList().size());
        assertEquals(2, p2.getVertexList().get(0).intValue());

        assertEquals(5.0, fw.getPath(1, 2).getWeight(), 1e-9);
        assertEquals(15.0, fw.getPath(2, 1).getWeight(), 1e-9);
    }

}
