/*
 * (C) Copyright 2003-2017, by Liviu Rau and Contributors.
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
package org.jgrapht.traverse;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.event.*;
import org.jgrapht.graph.*;

/**
 * A basis for testing {@link org.jgrapht.traverse.BreadthFirstIterator} and
 * {@link org.jgrapht.traverse.DepthFirstIterator} classes.
 *
 * @author Patrick Sharp (I pretty much just ripped off Liviu Rau's code from
 *         AbstractGraphIteratorTest)
 * @since May 15, 2017
 */
public abstract class CrossComponentIteratorTest
    extends AbstractGraphIteratorTest
{
    // ~ Instance fields --------------------------------------------------------

    StringBuffer result;

    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testDirectedGraphViaCCI()
    {
        result = new StringBuffer();

        Graph<String, DefaultWeightedEdge> graph = createDirectedGraph();

        AbstractGraphIterator<String, DefaultWeightedEdge> iterator =
            createIterator(graph, Arrays.asList("orphan", "7", "3"));
        MyTraversalListener<DefaultWeightedEdge> listener = new MyTraversalListener<>();
        iterator.addTraversalListener(listener);

        while (iterator.hasNext()) {
            result.append(iterator.next());

            if (iterator.hasNext()) {
                result.append(',');
            }
        }

        assertEquals(getExpectedCCStr3(), result.toString());

        assertEquals(getExpectedCCFinishString(), listener.getFinishString());
    }

    public void testDirectedGraphNullConstructors()
    {
        Graph<String, DefaultWeightedEdge> graph = createDirectedGraph();
        doDirectedGraphTest(createIterator(graph, (String) null));
        doDirectedGraphTest(createIterator(graph, (Iterable<String>) null));
    }

    abstract String getExpectedCCStr1();

    abstract String getExpectedCCStr2();

    abstract String getExpectedCCStr3();

    String getExpectedCCFinishString()
    {
        return "";
    }

    abstract AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(
        Graph<String, DefaultWeightedEdge> g, Iterable<String> startVertex);

    // ~ Inner Classes ----------------------------------------------------------

    /**
     * Internal traversal listener.
     *
     * @author Barak Naveh
     */
    private class MyTraversalListener<E>
        implements TraversalListener<String, E>
    {
        private int componentNumber = 0;
        private int numComponentVertices = 0;

        private String finishString = "";

        /**
         * @see TraversalListener#connectedComponentFinished(ConnectedComponentTraversalEvent)
         */
        @Override
        public void connectedComponentFinished(ConnectedComponentTraversalEvent e)
        {
            switch (componentNumber) {
            case 1:
                assertEquals(getExpectedCCStr1(), result.toString());
                assertEquals(1, numComponentVertices);

                break;

            case 2:
                assertEquals(getExpectedCCStr2(), result.toString());
                assertEquals(5, numComponentVertices);

                break;

            case 3:
                assertEquals(getExpectedCCStr3(), result.toString());
                assertEquals(4, numComponentVertices);

                break;

            default:
                assertFalse();

                break;
            }

            numComponentVertices = 0;
        }

        /**
         * @see TraversalListener#connectedComponentStarted(ConnectedComponentTraversalEvent)
         */
        @Override
        public void connectedComponentStarted(ConnectedComponentTraversalEvent e)
        {
            componentNumber++;
        }

        /**
         * @see TraversalListener#edgeTraversed(EdgeTraversalEvent)
         */
        @Override
        public void edgeTraversed(EdgeTraversalEvent<E> e)
        {
            // to be tested...
        }

        /**
         * @see TraversalListener#vertexTraversed(VertexTraversalEvent)
         */
        @Override
        public void vertexTraversed(VertexTraversalEvent<String> e)
        {
            numComponentVertices++;
        }

        /**
         * @see TraversalListener#vertexTraversed(VertexTraversalEvent)
         */
        @Override
        public void vertexFinished(VertexTraversalEvent<String> e)
        {
            finishString += e.getVertex() + ":";
        }

        public String getFinishString()
        {
            return finishString;
        }
    }
}

// End AbstractGraphIteratorTest.java
