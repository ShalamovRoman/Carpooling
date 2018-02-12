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
package org.jgrapht.graph;

import java.util.*;

import org.jgrapht.*;

/**
 * A unit test for the AsDirectedGraph view.
 *
 * @author John V. Sichi
 */
public class AsUndirectedGraphTest
    extends EnhancedTestCase
{
    // ~ Instance fields --------------------------------------------------------

    private Graph<String, DefaultEdge> directed;
    private DefaultEdge loop;
    private DefaultEdge e12;
    private DefaultEdge e23;
    private DefaultEdge e24;
    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";
    private Graph<String, DefaultEdge> undirected;

    // ~ Constructors -----------------------------------------------------------

    /**
     * @see junit.framework.TestCase#TestCase(java.lang.String)
     */
    public AsUndirectedGraphTest(String name)
    {
        super(name);
    }

    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testAddEdge()
    {
        try {
            undirected.addEdge(v3, v4);
            assertFalse();
        } catch (UnsupportedOperationException e) {
            assertTrue();
        }

        assertEquals(
            "([v1, v2, v3, v4], [{v1,v2}, {v2,v3}, {v2,v4}, {v4,v4}])", undirected.toString());
    }

    /**
     * .
     */
    public void testAddVertex()
    {
        String v5 = "v5";

        undirected.addVertex(v5);
        assertEquals(true, undirected.containsVertex(v5));
        assertEquals(true, directed.containsVertex(v5));
    }

    /**
     * .
     */
    public void testDegreeOf()
    {
        assertEquals(1, undirected.degreeOf(v1));
        assertEquals(3, undirected.degreeOf(v2));
        assertEquals(1, undirected.degreeOf(v3));
        assertEquals(3, undirected.degreeOf(v4));
    }

    /**
     * .
     */
    public void testEdgesOf()
    {
        assertEquals(new HashSet<>(Arrays.asList(e12)), undirected.edgesOf(v1));
        assertEquals(new HashSet<>(Arrays.asList(e12, e23, e24)), undirected.edgesOf(v2));
        assertEquals(new HashSet<>(Arrays.asList(e23)), undirected.edgesOf(v3));
        assertEquals(new HashSet<>(Arrays.asList(e24, loop)), undirected.edgesOf(v4));
    }

    /**
     * .
     */
    public void testInDegreeOf()
    {
        assertEquals(1, undirected.inDegreeOf(v1));
        assertEquals(3, undirected.inDegreeOf(v2));
        assertEquals(1, undirected.inDegreeOf(v3));
        assertEquals(3, undirected.inDegreeOf(v4));
    }

    /**
     * .
     */
    public void testIncomingEdgesOf()
    {
        assertEquals(new HashSet<>(Arrays.asList(e12)), undirected.incomingEdgesOf(v1));
        assertEquals(new HashSet<>(Arrays.asList(e12, e23, e24)), undirected.incomingEdgesOf(v2));
        assertEquals(new HashSet<>(Arrays.asList(e23)), undirected.incomingEdgesOf(v3));
        assertEquals(new HashSet<>(Arrays.asList(e24, loop)), undirected.incomingEdgesOf(v4));
    }

    /**
     * .
     */
    public void testOutDegreeOf()
    {
        assertEquals(1, undirected.outDegreeOf(v1));
        assertEquals(3, undirected.outDegreeOf(v2));
        assertEquals(1, undirected.outDegreeOf(v3));
        assertEquals(3, undirected.outDegreeOf(v4));
    }

    /**
     * .
     */
    public void testOutgoingEdgesOf()
    {
        assertEquals(new HashSet<>(Arrays.asList(e12)), undirected.outgoingEdgesOf(v1));
        assertEquals(new HashSet<>(Arrays.asList(e12, e23, e24)), undirected.outgoingEdgesOf(v2));
        assertEquals(new HashSet<>(Arrays.asList(e23)), undirected.outgoingEdgesOf(v3));
        assertEquals(new HashSet<>(Arrays.asList(e24, loop)), undirected.outgoingEdgesOf(v4));
    }

    /**
     * .
     */
    public void testGetAllEdges()
    {
        Set<DefaultEdge> edges = undirected.getAllEdges(v3, v2);
        assertEquals(1, edges.size());
        assertEquals(directed.getEdge(v2, v3), edges.iterator().next());

        edges = undirected.getAllEdges(v4, v4);
        assertEquals(1, edges.size());
        assertEquals(loop, edges.iterator().next());
    }

    /**
     * .
     */
    public void testGetEdge()
    {
        assertEquals(directed.getEdge(v1, v2), undirected.getEdge(v1, v2));
        assertEquals(directed.getEdge(v1, v2), undirected.getEdge(v2, v1));

        assertEquals(directed.getEdge(v4, v4), undirected.getEdge(v4, v4));
    }

    /**
     * .
     */
    public void testRemoveEdge()
    {
        undirected.removeEdge(loop);
        assertEquals(false, undirected.containsEdge(loop));
        assertEquals(false, directed.containsEdge(loop));
    }

    /**
     * .
     */
    public void testRemoveVertex()
    {
        undirected.removeVertex(v4);
        assertEquals(false, undirected.containsVertex(v4));
        assertEquals(false, directed.containsVertex(v4));
    }

    /**
     * .
     */
    public void testToString()
    {
        assertEquals(
            "([v1, v2, v3, v4], [(v1,v2), (v2,v3), (v2,v4), (v4,v4)])", directed.toString());
        assertEquals(
            "([v1, v2, v3, v4], [{v1,v2}, {v2,v3}, {v2,v4}, {v4,v4}])", undirected.toString());
    }

    /**
     * .
     */
    @Override
    protected void setUp()
    {
        directed = new DefaultDirectedGraph<>(DefaultEdge.class);
        undirected = new AsUndirectedGraph<>(directed);

        directed.addVertex(v1);
        directed.addVertex(v2);
        directed.addVertex(v3);
        directed.addVertex(v4);
        e12 = directed.addEdge(v1, v2);
        e23 = directed.addEdge(v2, v3);
        e24 = directed.addEdge(v2, v4);
        loop = directed.addEdge(v4, v4);
    }
}

// End AsUndirectedGraphTest.java
