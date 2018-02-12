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

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 * @author Dimitrios Michail
 */
public class WattsStrogatzGraphGeneratorTest
{
    @Test(expected = IllegalArgumentException.class)
    public void testLessThan3Nodes()
    {
        new WattsStrogatzGraphGenerator<>(2, 1, 0.5);
    }

    @Test
    public void testBadParameters()
    {
        try {
            new WattsStrogatzGraphGenerator<>(-1, 2, 0.5);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new WattsStrogatzGraphGenerator<>(10, 9, 0.5);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new WattsStrogatzGraphGenerator<>(10, 9, 0.5);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new WattsStrogatzGraphGenerator<>(11, 11, 0.5);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new WattsStrogatzGraphGenerator<>(10, 2, -1.0);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new WattsStrogatzGraphGenerator<>(10, 2, 2.0);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void test4RegularNoRewiring()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(6, 4, 0.0, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);

        assertEquals(6, g.vertexSet().size());
        assertEquals(12, g.edgeSet().size());
    }

    @Test
    public void test4RegularSomeRewiring()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(6, 4, 0.5, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);

        assertEquals(6, g.vertexSet().size());
        assertEquals(12, g.edgeSet().size());
    }

    @Test
    public void test4RegularMoreRewiring()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(6, 4, 0.8, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);

        assertEquals(6, g.vertexSet().size());
        assertEquals(12, g.edgeSet().size());
    }

    @Test
    public void test4RegularAddShortcutInsteadOfRewiring()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(6, 4, 0.5, true, new Random(seed));
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);

        assertEquals(6, g.vertexSet().size());
    }

    @Test
    public void test6RegularNoRewiring()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(12, 6, 0.0, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);

        assertEquals(12, g.vertexSet().size());
        assertEquals(36, g.edgeSet().size());
    }

    @Test
    public void test6RegularSomeRewiring()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(12, 6, 0.7, seed);
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);

        assertEquals(12, g.vertexSet().size());
        assertEquals(36, g.edgeSet().size());
    }

    @Test
    public void test4RegularNoRewiringDirected()
    {
        final long seed = 5;

        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new WattsStrogatzGraphGenerator<>(6, 4, 0.0, seed);
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);

        assertEquals(6, g.vertexSet().size());
        assertEquals(12, g.edgeSet().size());
    }

}
