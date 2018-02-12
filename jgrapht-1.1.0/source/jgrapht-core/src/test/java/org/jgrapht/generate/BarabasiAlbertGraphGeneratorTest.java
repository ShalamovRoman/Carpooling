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
package org.jgrapht.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 * Tests for {@link BarabasiAlbertGraphGenerator}.
 * 
 * @author Dimitrios Michail
 */
public class BarabasiAlbertGraphGeneratorTest
{
    @Test
    public void testBadParameters()
    {
        try {
            new BarabasiAlbertGraphGenerator<>(0, 10, 100);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new BarabasiAlbertGraphGenerator<>(2, 0, 100);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new BarabasiAlbertGraphGenerator<>(2, 3, 100);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new BarabasiAlbertGraphGenerator<>(3, 2, 2);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testUndirected()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new BarabasiAlbertGraphGenerator<>(3, 2, 10, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);

        assertEquals(10, g.vertexSet().size());
    }

    @Test
    public void testUndirectedWithOneInitialNode()
    {
        final long seed = 7;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new BarabasiAlbertGraphGenerator<>(1, 1, 20, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);

        assertEquals(20, g.vertexSet().size());
    }

    @Test
    public void testDirected()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new BarabasiAlbertGraphGenerator<>(3, 2, 10, seed);
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);

        assertEquals(10, g.vertexSet().size());
    }

    @Test
    public void testDirectedWithOneInitialNode()
    {
        final long seed = 13;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new BarabasiAlbertGraphGenerator<>(1, 1, 20, seed);
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);

        assertEquals(20, g.vertexSet().size());
    }

    @Test
    public void testUndirectedWithGraphWhichAlreadyHasSomeVertices()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new BarabasiAlbertGraphGenerator<>(3, 2, 10, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(1000);
        gen.generateGraph(g, new IntegerVertexFactory(), null);

        assertEquals(11, g.vertexSet().size());
    }

}
