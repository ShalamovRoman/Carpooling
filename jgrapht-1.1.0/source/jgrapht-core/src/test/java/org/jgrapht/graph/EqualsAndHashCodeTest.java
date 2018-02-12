/*
 * (C) Copyright 2012-2017, by Vladimir Kostyukov and Contributors.
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

public class EqualsAndHashCodeTest
    extends EnhancedTestCase
{
    // ~ Instance fields --------------------------------------------------------

    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";

    // ~ Constructors -----------------------------------------------------------

    /**
     * @see junit.framework.TestCase#TestCase(java.lang.String)
     */
    public EqualsAndHashCodeTest(String name)
    {
        super(name);
    }

    /**
     * Tests equals/hashCode methods for directed graphs.
     */
    public void testDefaultDirectedGraph()
    {
        Graph<String, DefaultEdge> g1 = new DefaultDirectedGraph<>(DefaultEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addVertex(v4);
        DefaultEdge e12 = g1.addEdge(v1, v2);
        DefaultEdge e23 = g1.addEdge(v2, v3);
        DefaultEdge e31 = g1.addEdge(v3, v1);

        Graph<String, DefaultEdge> g2 = new DefaultDirectedGraph<>(DefaultEdge.class);
        g2.addVertex(v4);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v3, v1, e31);
        g2.addEdge(v2, v3, e23);
        g2.addEdge(v1, v2, e12);

        Graph<String, DefaultEdge> g3 = new DefaultDirectedGraph<>(DefaultEdge.class);
        g3.addVertex(v4);
        g3.addVertex(v3);
        g3.addVertex(v2);
        g3.addVertex(v1);
        g3.addEdge(v3, v1, e31);
        g3.addEdge(v2, v3, e23);

        assertTrue(g2.equals(g1));
        assertTrue(!g3.equals(g2));

        assertEquals(g2.hashCode(), g1.hashCode());
    }

    /**
     * Tests equals/hashCode methods for undirected graphs.
     */
    public void testSimpleGraph()
    {
        Graph<String, DefaultEdge> g1 = new SimpleGraph<>(DefaultEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addVertex(v4);
        DefaultEdge e12 = g1.addEdge(v1, v2);
        DefaultEdge e23 = g1.addEdge(v2, v3);
        DefaultEdge e31 = g1.addEdge(v3, v1);

        Graph<String, DefaultEdge> g2 = new SimpleGraph<>(DefaultEdge.class);
        g2.addVertex(v4);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v3, v1, e31);
        g2.addEdge(v2, v3, e23);
        g2.addEdge(v1, v2, e12);

        Graph<String, DefaultEdge> g3 = new SimpleGraph<>(DefaultEdge.class);
        g3.addVertex(v4);
        g3.addVertex(v3);
        g3.addVertex(v2);
        g3.addVertex(v1);
        g3.addEdge(v3, v1, e31);
        g3.addEdge(v2, v3, e23);

        assertTrue(g2.equals(g1));
        assertTrue(!g3.equals(g2));

        assertEquals(g2.hashCode(), g1.hashCode());
    }

    /**
     * Tests equals/hashCode methods for graphs with non-Intrusive edges.
     */
    public void testGraphsWithNonIntrusiveEdge()
    {
        Graph<String, String> g1 = new DefaultDirectedGraph<>(String.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addEdge(v1, v2, v1 + v2);
        g1.addEdge(v3, v1, v3 + v1);

        Graph<String, String> g2 = new DefaultDirectedGraph<>(String.class);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v3, v1, v3 + v1);
        g2.addEdge(v1, v2, v1 + v2);

        Graph<String, String> g3 = new DefaultDirectedGraph<>(String.class);
        g3.addVertex(v3);
        g3.addVertex(v2);
        g3.addVertex(v1);
        g3.addEdge(v3, v1, v3 + v1);
        g3.addEdge(v1, v2, v1 + v2);
        g3.addEdge(v2, v3, v2 + v3);

        assertTrue(g1.equals(g2));
        assertTrue(!g2.equals(g3));

        assertEquals(g2.hashCode(), g1.hashCode());
    }

    /**
     * Tests equals/hashCode methods for graphs with multiple edges and loops.
     */
    public void testPseudograph()
    {
        Graph<String, DefaultEdge> g1 = new Pseudograph<>(DefaultEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        DefaultEdge e121 = g1.addEdge(v1, v2);
        DefaultEdge e23 = g1.addEdge(v2, v3);
        DefaultEdge e31 = g1.addEdge(v3, v1);
        DefaultEdge e122 = g1.addEdge(v1, v2);
        DefaultEdge e11 = g1.addEdge(v1, v1);

        Graph<String, DefaultEdge> g2 = new Pseudograph<>(DefaultEdge.class);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v1, v1, e11);
        g2.addEdge(v1, v2, e121);
        g2.addEdge(v3, v1, e31);
        g2.addEdge(v2, v3, e23);
        g2.addEdge(v1, v2, e122);

        Graph<String, DefaultEdge> g3 = new Pseudograph<>(DefaultEdge.class);
        g3.addVertex(v3);
        g3.addVertex(v2);
        g3.addVertex(v1);
        g3.addEdge(v1, v1, e11);
        g3.addEdge(v1, v2, e121);
        g3.addEdge(v3, v1, e31);
        g3.addEdge(v2, v3, e23);

        assertTrue(g1.equals(g2));
        assertTrue(!g2.equals(g3));

        assertEquals(g2.hashCode(), g1.hashCode());
    }

    /**
     * Tests equals/hashCode methods for graphs with custom edges.
     */
    public void testGrapshWithCustomEdges()
    {
        Graph<String, CustomEdge> g1 = new SimpleGraph<>(CustomEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addEdge(v1, v2, new CustomEdge("v1-v2"));
        g1.addEdge(v3, v1, new CustomEdge("v3-v1"));

        Graph<String, CustomEdge> g2 = new SimpleGraph<>(CustomEdge.class);
        g2.addVertex(v1);
        g2.addVertex(v2);
        g2.addVertex(v3);
        g2.addEdge(v1, v2, new CustomEdge("v1-v2"));
        g2.addEdge(v3, v1, new CustomEdge("v3-v1"));

        Graph<String, CustomEdge> g3 = new SimpleGraph<>(CustomEdge.class);
        g3.addVertex(v1);
        g3.addVertex(v2);
        g3.addVertex(v3);
        g3.addEdge(v1, v2, new CustomEdge("v1::v2"));
        g3.addEdge(v3, v1, new CustomEdge("v3-v1"));

        assertTrue(g1.equals(g2));
        assertTrue(!g2.equals(g3));

        assertEquals(g2.hashCode(), g1.hashCode());
    }

    /**
     * Tests equals/hashCode for graphs transformed to weighted.
     */
    public void testAsWeightedGraphs()
    {
        Graph<String, DefaultEdge> g1 = new SimpleGraph<>(DefaultEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        DefaultEdge e12 = g1.addEdge(v1, v2);
        DefaultEdge e23 = g1.addEdge(v2, v3);
        DefaultEdge e31 = g1.addEdge(v3, v1);

        Graph<String, DefaultEdge> g2 = new SimpleGraph<>(DefaultEdge.class);
        g2.addVertex(v1);
        g2.addVertex(v2);
        g2.addVertex(v3);
        g2.addEdge(v1, v2, e12);
        g2.addEdge(v2, v3, e23);
        g2.addEdge(v3, v1, e31);

        Map<DefaultEdge, Double> weightMap1 = new HashMap<>();

        weightMap1.put(e12, 10.0);
        weightMap1.put(e23, 20.0);
        weightMap1.put(e31, 30.0);

        Graph<String, DefaultEdge> g3 = new AsWeightedGraph<>(g1, weightMap1);

        Map<DefaultEdge, Double> weightMap2 = new HashMap<>();

        weightMap2.put(e12, 10.0);
        weightMap2.put(e23, 20.0);
        weightMap2.put(e31, 30.0);

        Graph<String, DefaultEdge> g4 = new AsWeightedGraph<>(g2, weightMap2);

        Map<DefaultEdge, Double> weightMap3 = new HashMap<>();

        weightMap3.put(e12, 100.0);
        weightMap3.put(e23, 200.0);
        weightMap3.put(e31, 300.0);

        Graph<String, DefaultEdge> g5 = new AsWeightedGraph<>(g2, weightMap3);

        assertTrue(g1.equals(g2));
        assertEquals(g2.hashCode(), g1.hashCode());

        assertTrue(g3.equals(g4));
        assertEquals(g4.hashCode(), g3.hashCode());

        assertTrue(!g4.equals(g5));
    }

    /**
     * Simple custom edge class.
     */
    public static class CustomEdge
        extends DefaultEdge
    {
        private static final long serialVersionUID = 1L;
        private String label;

        public CustomEdge(String label)
        {
            this.label = label;
        }

        @Override
        public int hashCode()
        {
            return label.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof CustomEdge)) {
                return false;
            }

            CustomEdge edge = (CustomEdge) obj;
            return label.equals(edge.label);
        }
    }
}
