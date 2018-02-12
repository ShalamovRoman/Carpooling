/*
 * (C) Copyright 2005-2017, by Assaf Lehr, Dimitrios Michail and Contributors.
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
import static org.junit.Assert.fail;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

/**
 * .
 * 
 * @author Assaf Lehr
 * @since Aug 6, 2005
 */
public class GnmRandomGraphGeneratorTest
{

    private static final long SEED = 5;

    @Test
    public void testZeroNodes()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new GnmRandomGraphGenerator<>(0, 0);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);
        assertEquals(0, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
    }

    @Test
    public void testZeroEdge()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen = new GnmRandomGraphGenerator<>(10, 0);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(), null);
        assertEquals(10, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
    }

    @Test
    public void testBadParameters()
    {
        try {
            new GnmRandomGraphGenerator<>(-10, 10);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }

        try {
            new GnmRandomGraphGenerator<>(10, -10);
            fail("Bad parameter");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testDirectedGraphGnp1()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnmRandomGraphGenerator<>(6, 18, SEED);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(1), null);

        int[][] edges = { { 6, 5 }, { 1, 6 }, { 5, 6 }, { 3, 4 }, { 6, 4 }, { 2, 1 }, { 3, 5 },
            { 1, 2 }, { 1, 3 }, { 2, 5 }, { 4, 3 }, { 2, 3 }, { 5, 4 }, { 1, 4 }, { 2, 6 },
            { 6, 1 }, { 4, 6 }, { 3, 1 } };

        assertEquals(6, g.vertexSet().size());
        for (int[] e : edges) {
            assertTrue(g.containsEdge(e[0], e[1]));
        }
        assertEquals(edges.length, g.edgeSet().size());
    }

    @Test
    public void testDirectedGraphGnp1WithLoops()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnmRandomGraphGenerator<>(6, 18, SEED, true, false);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(1), null);

        int[][] edges = { { 6, 5 }, { 3, 3 }, { 1, 6 }, { 5, 6 }, { 3, 4 }, { 6, 4 }, { 2, 1 },
            { 3, 5 }, { 1, 2 }, { 1, 3 }, { 2, 5 }, { 4, 3 }, { 2, 3 }, { 2, 2 }, { 5, 4 },
            { 2, 2 }, { 1, 4 }, { 5, 5 } };

        assertEquals(6, g.vertexSet().size());
        for (int[] e : edges) {
            assertTrue(g.containsEdge(e[0], e[1]));
        }
        assertEquals(edges.length, g.edgeSet().size());
    }

    @Test
    public void testDirectedGraphGnp1WithMultipleEdges()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnmRandomGraphGenerator<>(6, 18, SEED, false, true);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(1), null);

        int[][] edges = { { 6, 5 }, { 1, 6 }, { 5, 6 }, { 3, 4 }, { 6, 4 }, { 2, 1 }, { 3, 5 },
            { 1, 2 }, { 6, 4 }, { 1, 6 }, { 1, 3 }, { 2, 5 }, { 3, 4 }, { 4, 3 }, { 2, 3 },
            { 2, 3 }, { 5, 4 }, { 1, 6 } };
        assertEquals(6, g.vertexSet().size());
        for (int[] e : edges) {
            assertTrue(g.containsEdge(e[0], e[1]));
        }
        assertEquals(edges.length, g.edgeSet().size());
    }

    @Test
    public void testDirectedGraphGnp1WithLoopsAndMultipleEdges()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnmRandomGraphGenerator<>(6, 18, SEED, true, true);
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(1), null);

        int[][] edges = { { 6, 5 }, { 3, 3 }, { 1, 6 }, { 5, 6 }, { 3, 4 }, { 6, 4 }, { 2, 1 },
            { 3, 5 }, { 1, 2 }, { 6, 4 }, { 1, 6 }, { 1, 3 }, { 2, 5 }, { 3, 4 }, { 4, 3 },
            { 2, 3 }, { 2, 2 }, { 2, 3 } };
        assertEquals(6, g.vertexSet().size());
        for (int[] e : edges) {
            assertTrue(g.containsEdge(e[0], e[1]));
        }
        assertEquals(edges.length, g.edgeSet().size());
    }

    @Test
    public void testUndirectedGraphGnp1()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnmRandomGraphGenerator<>(6, 15, SEED);
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(1), null);

        int[][] edges = { { 6, 5 }, { 1, 6 }, { 3, 4 }, { 6, 4 }, { 2, 1 }, { 3, 5 }, { 1, 3 },
            { 2, 5 }, { 2, 3 }, { 5, 4 }, { 1, 4 }, { 2, 6 }, { 5, 1 }, { 4, 2 }, { 6, 3 } };

        assertEquals(6, g.vertexSet().size());
        for (int[] e : edges) {
            assertTrue(g.containsEdge(e[0], e[1]));
        }
        assertEquals(edges.length, g.edgeSet().size());
    }

    @Test
    public void testUndirectedGraphGnp1WithLoops()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> gen =
            new GnmRandomGraphGenerator<>(6, 15, SEED, true, false);
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        gen.generateGraph(g, new IntegerVertexFactory(1), null);

        int[][] edges = { { 6, 5 }, { 3, 3 }, { 1, 6 }, { 3, 4 }, { 6, 4 }, { 2, 1 }, { 3, 5 },
            { 1, 3 }, { 2, 5 }, { 2, 3 }, { 2, 2 }, { 5, 4 }, { 2, 2 }, { 1, 4 }, { 5, 5 } };

        assertEquals(6, g.vertexSet().size());
        for (int[] e : edges) {
            assertTrue(g.containsEdge(e[0], e[1]));
        }
        assertEquals(edges.length, g.edgeSet().size());
    }

    @Test
    public void testNotAllowedLoopsOrMultipleEdges()
    {
        try {
            GraphGenerator<Integer, DefaultEdge, Integer> gen =
                new GnmRandomGraphGenerator<>(6, 18, SEED, true, false);
            Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
            gen.generateGraph(g, new IntegerVertexFactory(), null);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }

        try {
            GraphGenerator<Integer, DefaultEdge, Integer> gen =
                new GnmRandomGraphGenerator<>(6, 18, SEED, false, true);
            Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
            gen.generateGraph(g, new IntegerVertexFactory(), null);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testEdgeLimitsDirected()
    {
        try {
            GraphGenerator<Integer, DefaultEdge, Integer> gen1 =
                new GnmRandomGraphGenerator<>(5, 21, SEED, false, false);
            Graph<Integer, DefaultEdge> g1 = new SimpleDirectedGraph<>(DefaultEdge.class);
            gen1.generateGraph(g1, new IntegerVertexFactory(), null);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }

        GraphGenerator<Integer, DefaultEdge, Integer> gen2 =
            new GnmRandomGraphGenerator<>(5, 20, SEED, false, false);
        Graph<Integer, DefaultEdge> g2 = new SimpleDirectedGraph<>(DefaultEdge.class);
        gen2.generateGraph(g2, new IntegerVertexFactory(), null);

        GraphGenerator<Integer, DefaultEdge, Integer> gen3 =
            new GnmRandomGraphGenerator<>(5, 25, SEED, true, false);
        Graph<Integer, DefaultEdge> g3 = new DirectedPseudograph<>(DefaultEdge.class);
        gen3.generateGraph(g3, new IntegerVertexFactory(), null);

        GraphGenerator<Integer, DefaultEdge, Integer> gen4 =
            new GnmRandomGraphGenerator<>(5, 25, SEED, false, true);
        Graph<Integer, DefaultEdge> g4 = new DirectedPseudograph<>(DefaultEdge.class);
        gen4.generateGraph(g4, new IntegerVertexFactory(), null);
    }

    @Test
    public void testEdgeLimitsUndirected()
    {
        try {
            GraphGenerator<Integer, DefaultEdge, Integer> gen1 =
                new GnmRandomGraphGenerator<>(5, 11, SEED, false, false);
            Graph<Integer, DefaultEdge> g1 = new SimpleGraph<>(DefaultEdge.class);
            gen1.generateGraph(g1, new IntegerVertexFactory(), null);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }

        GraphGenerator<Integer, DefaultEdge, Integer> gen2 =
            new GnmRandomGraphGenerator<>(5, 10, SEED, false, false);
        Graph<Integer, DefaultEdge> g2 = new SimpleGraph<>(DefaultEdge.class);
        gen2.generateGraph(g2, new IntegerVertexFactory(), null);

        GraphGenerator<Integer, DefaultEdge, Integer> gen3 =
            new GnmRandomGraphGenerator<>(5, 15, SEED, true, false);
        Graph<Integer, DefaultEdge> g3 = new Pseudograph<>(DefaultEdge.class);
        gen3.generateGraph(g3, new IntegerVertexFactory(), null);

        GraphGenerator<Integer, DefaultEdge, Integer> gen4 =
            new GnmRandomGraphGenerator<>(5, 15, SEED, false, true);
        Graph<Integer, DefaultEdge> g4 = new Pseudograph<>(DefaultEdge.class);
        gen4.generateGraph(g4, new IntegerVertexFactory(), null);
    }

    @Test
    public void testMaximumAllowedEdges()
    {
        // undirected graphs
        boolean isDirected = false;
        assertEquals(
            0, GnmRandomGraphGenerator.computeMaximumAllowedEdges(0, isDirected, false, false));
        assertEquals(
            0, GnmRandomGraphGenerator.computeMaximumAllowedEdges(0, isDirected, false, true));
        assertEquals(
            0, GnmRandomGraphGenerator.computeMaximumAllowedEdges(0, isDirected, true, false));
        assertEquals(
            0, GnmRandomGraphGenerator.computeMaximumAllowedEdges(0, isDirected, true, true));

        assertEquals(
            0, GnmRandomGraphGenerator.computeMaximumAllowedEdges(1, isDirected, false, false));
        assertEquals(
            1, GnmRandomGraphGenerator.computeMaximumAllowedEdges(1, isDirected, true, false));
        assertEquals(
            0, GnmRandomGraphGenerator.computeMaximumAllowedEdges(1, isDirected, false, true));
        assertEquals(
            Integer.MAX_VALUE,
            GnmRandomGraphGenerator.computeMaximumAllowedEdges(1, isDirected, true, true));

        assertEquals(
            45, GnmRandomGraphGenerator.computeMaximumAllowedEdges(10, isDirected, false, false));
        assertEquals(
            55, GnmRandomGraphGenerator.computeMaximumAllowedEdges(10, isDirected, true, false));
        assertEquals(
            Integer.MAX_VALUE,
            GnmRandomGraphGenerator.computeMaximumAllowedEdges(10, isDirected, false, true));
        assertEquals(
            Integer.MAX_VALUE,
            GnmRandomGraphGenerator.computeMaximumAllowedEdges(10, isDirected, true, true));

        assertEquals(
            Integer.MAX_VALUE,
            GnmRandomGraphGenerator.computeMaximumAllowedEdges(200000, isDirected, false, false));

        // directed graphs
        isDirected = true;
        assertEquals(
            0, GnmRandomGraphGenerator.computeMaximumAllowedEdges(0, isDirected, false, false));
        assertEquals(
            0, GnmRandomGraphGenerator.computeMaximumAllowedEdges(0, isDirected, false, true));
        assertEquals(
            0, GnmRandomGraphGenerator.computeMaximumAllowedEdges(0, isDirected, true, false));
        assertEquals(
            0, GnmRandomGraphGenerator.computeMaximumAllowedEdges(0, isDirected, true, true));

        assertEquals(
            0, GnmRandomGraphGenerator.computeMaximumAllowedEdges(1, isDirected, false, false));
        assertEquals(
            2, GnmRandomGraphGenerator.computeMaximumAllowedEdges(1, isDirected, true, false));
        assertEquals(
            0, GnmRandomGraphGenerator.computeMaximumAllowedEdges(1, isDirected, false, true));
        assertEquals(
            Integer.MAX_VALUE,
            GnmRandomGraphGenerator.computeMaximumAllowedEdges(1, isDirected, true, true));

        assertEquals(
            90, GnmRandomGraphGenerator.computeMaximumAllowedEdges(10, isDirected, false, false));
        assertEquals(
            110, GnmRandomGraphGenerator.computeMaximumAllowedEdges(10, isDirected, true, false));
        assertEquals(
            Integer.MAX_VALUE,
            GnmRandomGraphGenerator.computeMaximumAllowedEdges(10, isDirected, false, true));
        assertEquals(
            Integer.MAX_VALUE,
            GnmRandomGraphGenerator.computeMaximumAllowedEdges(10, isDirected, true, true));

        assertEquals(
            Integer.MAX_VALUE,
            GnmRandomGraphGenerator.computeMaximumAllowedEdges(200000, isDirected, false, false));
    }

    @Test
    public void testCannotGuessGraphType()
    {
        try {
            GraphGenerator<Integer, DefaultEdge, Integer> gen1 =
                new GnmRandomGraphGenerator<>(5, 11, SEED, false, false);
            GraphDelegator<Integer, DefaultEdge> g1 =
                new GraphDelegator<>(new SimpleGraph<>(DefaultEdge.class));
            gen1.generateGraph(g1, new IntegerVertexFactory(), null);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }

        GraphGenerator<Integer, DefaultEdge, Integer> gen2 =
            new GnmRandomGraphGenerator<>(5, 10, SEED, false, false);
        GraphDelegator<Integer, DefaultEdge> g2 =
            new GraphDelegator<>(new SimpleGraph<>(DefaultEdge.class));
        gen2.generateGraph(g2, new IntegerVertexFactory(), null);

        try {
            GraphGenerator<Integer, DefaultEdge, Integer> gen3 =
                new GnmRandomGraphGenerator<>(5, 11, SEED, true, false);
            GraphDelegator<Integer, DefaultEdge> g3 =
                new GraphDelegator<>(new Pseudograph<>(DefaultEdge.class));
            gen3.generateGraph(g3, new IntegerVertexFactory(), null);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }

        try {
            GraphGenerator<Integer, DefaultEdge, Integer> gen4 =
                new GnmRandomGraphGenerator<>(5, 11, SEED, false, true);
            GraphDelegator<Integer, DefaultEdge> g4 =
                new GraphDelegator<>(new Pseudograph<>(DefaultEdge.class));
            gen4.generateGraph(g4, new IntegerVertexFactory(), null);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGenerateDirectedGraph()
    {
        List<Graph<Integer, DefaultEdge>> graphArray = new ArrayList<>();
        for (int i = 0; i < 4; ++i) {
            graphArray.add(new SimpleDirectedGraph<>(DefaultEdge.class));
        }

        generateGraphs(graphArray, 11, 100);

        assertTrue(
            new EdgeTopologyCompare<Integer, DefaultEdge>()
                .compare(graphArray.get(0), graphArray.get(2)));

        assertTrue(
            new EdgeTopologyCompare<Integer, DefaultEdge>()
                .compare(graphArray.get(1), graphArray.get(3)));

    }

    @Test
    public void testGenerateListenableUndirectedGraph()
    {
        List<Graph<Integer, DefaultEdge>> graphArray = new ArrayList<>();
        for (int i = 0; i < 4; ++i) {
            graphArray.add(new DefaultListenableGraph<>(new SimpleGraph<>(DefaultEdge.class)));
        }

        generateGraphs(graphArray, 11, 50);

        assertTrue(
            new EdgeTopologyCompare<Integer, DefaultEdge>()
                .compare(graphArray.get(0), graphArray.get(2)));

        assertTrue(
            new EdgeTopologyCompare<Integer, DefaultEdge>()
                .compare(graphArray.get(1), graphArray.get(3)));
    }

    @Test
    public void testBadVertexFactory()
    {
        GraphGenerator<String, DefaultEdge, String> randomGen =
            new GnmRandomGraphGenerator<>(10, 3);
        Graph<String, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);
        try {
            randomGen.generateGraph(graph, new ClassBasedVertexFactory<>(String.class), null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    /**
     * Generates 4 graphs with the same numOfVertex and numOfEdges. The first two are generated
     * using the same RandomGraphGenerator; the last two are generated using a new instance.
     *
     * @param graphs array of graphs to generate
     * @param numOfVertex number of vertices to generate per graph
     * @param numOfEdges number of edges to generate per graph
     */
    private static void generateGraphs(
        List<Graph<Integer, DefaultEdge>> graphs, int numOfVertex, int numOfEdges)
    {
        final int seed = 17;
        GraphGenerator<Integer, DefaultEdge, Integer> randomGen =
            new GnmRandomGraphGenerator<>(numOfVertex, numOfEdges, seed);

        randomGen.generateGraph(graphs.get(0), new IntegerVertexFactory(), null);
        randomGen.generateGraph(graphs.get(1), new IntegerVertexFactory(), null);

        // use new randomGen here
        GraphGenerator<Integer, DefaultEdge, Integer> newRandomGen =
            new GnmRandomGraphGenerator<>(numOfVertex, numOfEdges, seed);

        newRandomGen.generateGraph(graphs.get(2), new IntegerVertexFactory(), null);
        newRandomGen.generateGraph(graphs.get(3), new IntegerVertexFactory(), null);
    }

    static class EdgeTopologyCompare<V, E>
    {
        /**
         * Compare topology of the two graphs. It does not compare the contents of the
         * vertexes/edges, but only the relationships between them.
         *
         * @param g1
         * @param g2
         */
        public boolean compare(Graph<V, E> g1, Graph<V, E> g2)
        {
            boolean result;
            VertexOrdering<V, E> lg1 = new VertexOrdering<>(g1);
            VertexOrdering<V, E> lg2 = new VertexOrdering<>(g2);
            result = lg1.equalsByEdgeOrder(lg2);

            return result;
        }
    }

    static class VertexOrdering<V, E>
    {
        /**
         * Holds a mapping between key=V(vertex) and value=Integer(vertex order). It can be used for
         * identifying the order of regular vertex/edge.
         */
        private Map<V, Integer> mapVertexToOrder;

        /**
         * Holds a HashSet of all LabelsGraph of the graph.
         */
        private Set<LabelsEdge> labelsEdgesSet;

        /**
         * Creates a new labels graph according to the regular graph. After its creation they will
         * no longer be linked, thus changes to one will not affect the other.
         *
         * @param regularGraph
         */
        public VertexOrdering(Graph<V, E> regularGraph)
        {
            this(regularGraph, regularGraph.vertexSet(), regularGraph.edgeSet());
        }

        /**
         * Creates a new labels graph according to the regular graph. After its creation they will
         * no longer be linked, thus changes to one will not affect the other.
         *
         * @param regularGraph
         * @param vertexSet
         * @param edgeSet
         */
        public VertexOrdering(Graph<V, E> regularGraph, Set<V> vertexSet, Set<E> edgeSet)
        {
            init(regularGraph, vertexSet, edgeSet);
        }

        private void init(Graph<V, E> g, Set<V> vertexSet, Set<E> edgeSet)
        {
            // create a map between vertex value to its order(1st,2nd,etc)
            // "CAT"=1 "DOG"=2 "RHINO"=3

            this.mapVertexToOrder = new HashMap<>(vertexSet.size());

            int counter = 0;
            for (V vertex : vertexSet) {
                mapVertexToOrder.put(vertex, counter);
                counter++;
            }

            // create a friendlier representation of an edge
            // by order, like 2nd->3rd instead of B->A
            // use the map to convert vertex to order
            // on directed graph, edge A->B must be (A,B)
            // on undirected graph, edge A-B can be (A,B) or (B,A)

            this.labelsEdgesSet = new HashSet<>(edgeSet.size());
            for (E edge : edgeSet) {
                V sourceVertex = g.getEdgeSource(edge);
                int sourceLabel = mapVertexToOrder.get(sourceVertex);
                int targetLabel = mapVertexToOrder.get(g.getEdgeTarget(edge));

                LabelsEdge lablesEdge = new LabelsEdge(sourceLabel, targetLabel);
                this.labelsEdgesSet.add(lablesEdge);

                if (g.getType().isUndirected()) {
                    LabelsEdge oppositeEdge = new LabelsEdge(targetLabel, sourceLabel);
                    this.labelsEdgesSet.add(oppositeEdge);
                }
            }
        }

        /**
         * Tests equality by order of edges
         */
        public boolean equalsByEdgeOrder(VertexOrdering<V, E> otherGraph)
        {

            return this.getLabelsEdgesSet().equals(otherGraph.getLabelsEdgesSet());
        }

        public Set<LabelsEdge> getLabelsEdgesSet()
        {
            return labelsEdgesSet;
        }

        /**
         * This is the format example:
         *
         * <pre>
         mapVertexToOrder=        labelsOrder=
         * </pre>
         */
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("mapVertexToOrder=");

            // vertex will be printed in their order
            Object[] vertexArray = new Object[this.mapVertexToOrder.size()];
            Set<V> keySet = this.mapVertexToOrder.keySet();
            for (V currVertex : keySet) {
                Integer index = this.mapVertexToOrder.get(currVertex);
                vertexArray[index] = currVertex;
            }
            sb.append(Arrays.toString(vertexArray));
            sb.append("labelsOrder=").append(this.labelsEdgesSet.toString());
            return sb.toString();
        }

        private static class LabelsEdge
        {
            private int source;
            private int target;
            private int hashCode;

            public LabelsEdge(int aSource, int aTarget)
            {
                this.source = aSource;
                this.target = aTarget;
                this.hashCode = (this.source + "" + this.target).hashCode();
            }

            /**
             * Checks both source and target. Does not check class type to be fast, so it may throw
             * ClassCastException. Careful!
             *
             * @see java.lang.Object#equals(java.lang.Object)
             */
            @Override
            public boolean equals(Object obj)
            {
                if (this == obj)
                    return true;
                else if (!(obj instanceof VertexOrdering.LabelsEdge))
                    return false;
                LabelsEdge otherEdge = TypeUtil.uncheckedCast(obj, null);
                return (this.source == otherEdge.source) && (this.target == otherEdge.target);
            }

            /**
             * @see java.lang.Object#hashCode()
             */
            @Override
            public int hashCode()
            {
                return this.hashCode; // filled on constructor
            }

            @Override
            public String toString()
            {
                return this.source + "->" + this.target;
            }
        }
    }
}

// End RandomGraphGeneratorTest.java
