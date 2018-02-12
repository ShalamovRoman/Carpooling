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
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 * @author Dimitrios Michail
 */
public class TreeSingleSourcePathsTest
{

    @Test
    public void test()
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

        Map<Integer, Pair<Double, DefaultWeightedEdge>> map = new HashMap<>();
        map.put(2, Pair.of(-5d, e12_1));
        map.put(3, Pair.of(-10d, e23_3));
        map.put(4, Pair.of(-110d, e34_1));

        TreeSingleSourcePathsImpl<Integer, DefaultWeightedEdge> t1 =
            new TreeSingleSourcePathsImpl<>(g, 1, map);

        assertEquals(1, t1.getSourceVertex().intValue());
        assertEquals(0d, t1.getWeight(1), 1e-9);
        assertTrue(t1.getPath(1).getEdgeList().isEmpty());
        assertEquals(Arrays.asList(g.getEdgeSource(e12_1)), t1.getPath(1).getVertexList());
        assertEquals(-5d, t1.getWeight(2), 1e-9);
        assertEquals(Arrays.asList(e12_1), t1.getPath(2).getEdgeList());
        assertEquals(-10d, t1.getWeight(3), 1e-9);
        assertEquals(Arrays.asList(e12_1, e23_3), t1.getPath(3).getEdgeList());
        assertEquals(-110d, t1.getWeight(4), 1e-9);
        assertEquals(Arrays.asList(e12_1, e23_3, e34_1), t1.getPath(4).getEdgeList());
    }

}
