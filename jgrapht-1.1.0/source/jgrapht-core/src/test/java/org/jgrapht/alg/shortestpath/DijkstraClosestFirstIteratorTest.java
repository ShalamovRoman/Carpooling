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
import static org.junit.Assert.assertFalse;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 * @author Dimitrios Michail
 */
public class DijkstraClosestFirstIteratorTest
{

    @Test
    public void testUndirected()
    {
        WeightedPseudograph<String, DefaultWeightedEdge> g =
            new WeightedPseudograph<>(DefaultWeightedEdge.class);

        Graphs.addAllVertices(g, Arrays.asList("1", "2", "3", "4", "5"));
        g.setEdgeWeight(g.addEdge("1", "2"), 2.0);
        g.setEdgeWeight(g.addEdge("1", "3"), 3.0);
        g.setEdgeWeight(g.addEdge("1", "5"), 100.0);
        g.setEdgeWeight(g.addEdge("2", "4"), 5.0);
        g.setEdgeWeight(g.addEdge("3", "4"), 20.0);
        g.setEdgeWeight(g.addEdge("4", "5"), 5.0);

        DijkstraClosestFirstIterator<String, DefaultWeightedEdge> it =
            new DijkstraClosestFirstIterator<>(g, "3");

        assertEquals("3", it.next());
        assertEquals("1", it.next());
        assertEquals("2", it.next());
        assertEquals("4", it.next());
        assertEquals("5", it.next());
        assertFalse(it.hasNext());

        DijkstraClosestFirstIterator<String, DefaultWeightedEdge> it1 =
            new DijkstraClosestFirstIterator<>(g, "1");
        assertEquals("1", it1.next());
        assertEquals("2", it1.next());
        assertEquals("3", it1.next());
        assertEquals("4", it1.next());
        assertEquals("5", it1.next());
        assertFalse(it1.hasNext());

        DijkstraClosestFirstIterator<String, DefaultWeightedEdge> it2 =
            new DijkstraClosestFirstIterator<>(g, "1", 11.0);
        assertEquals("1", it2.next());
        assertEquals("2", it2.next());
        assertEquals("3", it2.next());
        assertEquals("4", it2.next());
        assertFalse(it2.hasNext());

        DijkstraClosestFirstIterator<String, DefaultWeightedEdge> it3 =
            new DijkstraClosestFirstIterator<>(g, "3", 12.0);
        assertEquals("3", it3.next());
        assertEquals("1", it3.next());
        assertEquals("2", it3.next());
        assertEquals("4", it3.next());
        assertFalse(it3.hasNext());
        SingleSourcePaths<String, DefaultWeightedEdge> paths3 = it3.getPaths();
        assertEquals(10.0, paths3.getPath("4").getWeight(), 1e-9);
        assertEquals(5.0, paths3.getPath("2").getWeight(), 1e-9);
        assertEquals(3.0, paths3.getPath("1").getWeight(), 1e-9);
    }

}
