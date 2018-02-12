/*
 * (C) Copyright 2017-2017, by Joris Kinable and Contributors.
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
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 * Tests for GeneralizedPetersenGraphGenerator
 *
 * @author Joris Kinable
 */
public class WindmillGraphsGeneratorTest
{

    @Test
    public void testCubicalGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        GeneralizedPetersenGraphGenerator<Integer, DefaultEdge> gpgg =
            new GeneralizedPetersenGraphGenerator<>(4, 1);
        gpgg.generateGraph(g, new IntegerVertexFactory(), null);
        this.validateBasics(g, 8, 12, 3, 3, 4);
        assertTrue(GraphTests.isBipartite(g));
        assertTrue(GraphTests.isCubic(g));

    }

    // --------------Tests for Windmill graphs ---------------------
    @Test
    public void testGraph1a()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        new WindmillGraphsGenerator<Integer, DefaultEdge>(
            WindmillGraphsGenerator.Mode.WINDMILL, 3, 4)
                .generateGraph(g, new IntegerVertexFactory(), null);
        assertEquals(10, g.vertexSet().size());
        assertEquals(18, g.edgeSet().size());
        this.verifyVertexDegree(g, WindmillGraphsGenerator.Mode.WINDMILL, 3, 4);
    }

    @Test
    public void testGraph2a()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        new WindmillGraphsGenerator<Integer, DefaultEdge>(
            WindmillGraphsGenerator.Mode.WINDMILL, 4, 3)
                .generateGraph(g, new IntegerVertexFactory(), null);
        assertEquals(9, g.vertexSet().size());
        assertEquals(12, g.edgeSet().size());
        this.verifyVertexDegree(g, WindmillGraphsGenerator.Mode.WINDMILL, 4, 3);
    }

    @Test
    public void testGraph3a()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        new WindmillGraphsGenerator<Integer, DefaultEdge>(
            WindmillGraphsGenerator.Mode.WINDMILL, 3, 5)
                .generateGraph(g, new IntegerVertexFactory(), null);
        assertEquals(13, g.vertexSet().size());
        assertEquals(30, g.edgeSet().size());
        this.verifyVertexDegree(g, WindmillGraphsGenerator.Mode.WINDMILL, 3, 5);
    }

    // --------------Tests for Dutch Windmill Graphs ---------------
    @Test
    public void testButterflyGraph()
    {
        Graph<Integer, DefaultEdge> g = NamedGraphGenerator.butterflyGraph();
        this.validateBasics(g, 5, 6, 1, 2, 3);
        this.verifyVertexDegree(g, WindmillGraphsGenerator.Mode.DUTCHWINDMILL, 2, 3);
        assertTrue(GraphTests.isEulerian(g));
    }

    @Test
    public void testGraph2b()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        new WindmillGraphsGenerator<Integer, DefaultEdge>(
            WindmillGraphsGenerator.Mode.DUTCHWINDMILL, 4, 3)
                .generateGraph(g, new IntegerVertexFactory(), null);
        assertEquals(9, g.vertexSet().size());
        assertEquals(12, g.edgeSet().size());
        this.verifyVertexDegree(g, WindmillGraphsGenerator.Mode.DUTCHWINDMILL, 4, 3);
    }

    @Test
    public void testGraph3b()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        new WindmillGraphsGenerator<Integer, DefaultEdge>(
            WindmillGraphsGenerator.Mode.DUTCHWINDMILL, 3, 5)
                .generateGraph(g, new IntegerVertexFactory(), null);
        assertEquals(13, g.vertexSet().size());
        assertEquals(15, g.edgeSet().size());
        this.verifyVertexDegree(g, WindmillGraphsGenerator.Mode.DUTCHWINDMILL, 3, 5);
    }

    private <V, E> void validateBasics(
        Graph<V, E> g, int vertices, int edges, int radius, int diameter, int girt)
    {
        assertEquals(vertices, g.vertexSet().size());
        assertEquals(edges, g.edgeSet().size());
        GraphMeasurer<V, E> gm = new GraphMeasurer<>(g);
        assertEquals(radius, gm.getRadius(), 0.00000001);
        assertEquals(diameter, gm.getDiameter(), 0.00000001);
        assertEquals(girt, GraphMetrics.getGirth(g), 0.00000001);
    }

    private <V,
        E> void verifyVertexDegree(Graph<V, E> g, WindmillGraphsGenerator.Mode mode, int m, int n)
    {
        List<V> vertices = new ArrayList<>(g.vertexSet());
        if (mode == WindmillGraphsGenerator.Mode.DUTCHWINDMILL) {
            assertEquals(2 * m, g.degreeOf(vertices.get(0))); // degree of center vertex
            for (int i = 1; i < vertices.size(); i++)
                assertEquals(2, g.degreeOf(vertices.get(i))); // degree of other vertices
        } else {
            assertEquals(m * (n - 1), g.degreeOf(vertices.get(0))); // degree of center vertex
            for (int i = 1; i < vertices.size(); i++)
                assertEquals(n - 1, g.degreeOf(vertices.get(i))); // degree of other vertices
        }

    }
}
