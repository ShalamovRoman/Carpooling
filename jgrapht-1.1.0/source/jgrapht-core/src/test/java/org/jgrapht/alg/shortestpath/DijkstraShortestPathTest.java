/*
 * (C) Copyright 2003-2017, by John V Sichi and Contributors.
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

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.graph.*;

/**
 * .
 *
 * @author John V. Sichi
 */
public class DijkstraShortestPathTest
    extends ShortestPathTestCase
{
    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testConstructor()
    {
        GraphPath<String, DefaultWeightedEdge> path;
        Graph<String, DefaultWeightedEdge> g = create();

        path = new DijkstraShortestPath<String, DefaultWeightedEdge>(g, Double.POSITIVE_INFINITY)
            .getPath(V3, V4);
        assertEquals(
            Arrays.asList(new DefaultWeightedEdge[] { e13, e12, e24 }), path.getEdgeList());
        assertEquals(10.0, path.getWeight(), 0);

        path = new DijkstraShortestPath<String, DefaultWeightedEdge>(g, 7.0).getPath(V3, V4);
        assertNull(path);
    }

    @Override
    protected List<DefaultWeightedEdge> findPathBetween(
        Graph<String, DefaultWeightedEdge> g, String src, String dest)
    {
        return new DijkstraShortestPath<String, DefaultWeightedEdge>(g)
            .getPath(src, dest).getEdgeList();
    }

    public void testShortestPathTree()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(V1, V2, V3, V4, V5));

        DefaultWeightedEdge we12 = g.addEdge(V1, V2);
        DefaultWeightedEdge we24 = g.addEdge(V2, V4);
        DefaultWeightedEdge we13 = g.addEdge(V1, V3);
        DefaultWeightedEdge we32 = g.addEdge(V3, V2);
        DefaultWeightedEdge we34 = g.addEdge(V3, V4);

        g.setEdgeWeight(we12, 3.0);
        g.setEdgeWeight(we24, 1.0);
        g.setEdgeWeight(we13, 1.0);
        g.setEdgeWeight(we32, 1.0);
        g.setEdgeWeight(we34, 3.0);

        SingleSourcePaths<String, DefaultWeightedEdge> pathsTree =
            new DijkstraShortestPath<String, DefaultWeightedEdge>(g).getPaths(V1);
        assertEquals(g, pathsTree.getGraph());
        assertEquals(V1, pathsTree.getSourceVertex());
        assertEquals(0d, pathsTree.getWeight(V1), 1e-9);
        assertEquals(2d, pathsTree.getWeight(V2), 1e-9);
        assertEquals(1d, pathsTree.getWeight(V3), 1e-9);
        assertEquals(3d, pathsTree.getWeight(V4), 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, pathsTree.getWeight(V5), 1e-9);

        GraphPath<String, DefaultWeightedEdge> p11 = pathsTree.getPath(V1);
        assertEquals(V1, p11.getStartVertex());
        assertEquals(V1, p11.getEndVertex());
        assertEquals(0d, p11.getWeight(), 1e-9);
        assertTrue(p11.getEdgeList().isEmpty());

        GraphPath<String, DefaultWeightedEdge> p12 = pathsTree.getPath(V2);
        assertEquals(V1, p12.getStartVertex());
        assertEquals(V2, p12.getEndVertex());
        assertEquals(2d, p12.getWeight(), 1e-9);
        assertEquals(Arrays.asList(we13, we32), p12.getEdgeList());

        GraphPath<String, DefaultWeightedEdge> p13 = pathsTree.getPath(V3);
        assertEquals(V1, p13.getStartVertex());
        assertEquals(V3, p13.getEndVertex());
        assertEquals(1d, p13.getWeight(), 1e-9);
        assertEquals(Arrays.asList(we13), p13.getEdgeList());

        GraphPath<String, DefaultWeightedEdge> p14 = pathsTree.getPath(V4);
        assertEquals(V1, p14.getStartVertex());
        assertEquals(V4, p14.getEndVertex());
        assertEquals(3d, p14.getWeight(), 1e-9);
        assertEquals(Arrays.asList(we13, we32, we24), p14.getEdgeList());

        GraphPath<String, DefaultWeightedEdge> p15 = pathsTree.getPath(V5);
        assertNull(p15);
    }

    public void testGetPathWeight()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(V1, V2, V3, V4, V5));

        DefaultWeightedEdge we12 = g.addEdge(V1, V2);
        DefaultWeightedEdge we24 = g.addEdge(V2, V4);
        DefaultWeightedEdge we13 = g.addEdge(V1, V3);
        DefaultWeightedEdge we32 = g.addEdge(V3, V2);
        DefaultWeightedEdge we34 = g.addEdge(V3, V4);

        g.setEdgeWeight(we12, 3.0);
        g.setEdgeWeight(we24, 1.0);
        g.setEdgeWeight(we13, 1.0);
        g.setEdgeWeight(we32, 1.0);
        g.setEdgeWeight(we34, 3.0);

        assertEquals(
            0d, new DijkstraShortestPath<String, DefaultWeightedEdge>(g).getPathWeight(V1, V1));
        assertEquals(
            2d, new DijkstraShortestPath<String, DefaultWeightedEdge>(g).getPathWeight(V1, V2));
        assertEquals(
            1d, new DijkstraShortestPath<String, DefaultWeightedEdge>(g).getPathWeight(V1, V3));
        assertEquals(
            3d, new DijkstraShortestPath<String, DefaultWeightedEdge>(g).getPathWeight(V1, V4));
        assertEquals(
            Double.POSITIVE_INFINITY,
            new DijkstraShortestPath<String, DefaultWeightedEdge>(g).getPathWeight(V1, V5));
    }

    public void testNonNegativeWeights()
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(V1, V2));

        DefaultWeightedEdge we12 = g.addEdge(V1, V2);
        g.setEdgeWeight(we12, -100.0);

        try {
            new DijkstraShortestPath<String, DefaultWeightedEdge>(g).getPath(V1, V2);
            fail("No!");
        } catch (IllegalArgumentException e) {
        }
    }

}

// End DijkstraShortestPathTest.java
