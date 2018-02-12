/*
 * (C) Copyright 2008-2017, by Andrew Newell and Contributors.
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

import java.util.*;

import org.jgrapht.*;

/**
 * Generates a <a href="http://mathworld.wolfram.com/StarGraph.html">star graph</a> of any size.
 * This is a graph where every vertex has exactly one edge with a center vertex.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Andrew Newell
 * @since Dec 21, 2008
 */
public class StarGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
    public static final String CENTER_VERTEX = "Center Vertex";

    private int order;

    /**
     * Creates a new StarGraphGenerator object.
     *
     * @param order number of total vertices including the center vertex
     */
    public StarGraphGenerator(int order)
    {
        this.order = order;
    }

    /**
     * Generates a star graph with the designated order from the constructor
     */
    @Override
    public void generateGraph(
        Graph<V, E> target, final VertexFactory<V> vertexFactory, Map<String, V> resultMap)
    {
        if (order < 1) {
            return;
        }

        // Create center vertex
        V centerVertex = vertexFactory.createVertex();
        target.addVertex(centerVertex);
        if (resultMap != null) {
            resultMap.put(CENTER_VERTEX, centerVertex);
        }

        // Create other vertices
        for (int i = 0; i < (order - 1); i++) {
            V newVertex = vertexFactory.createVertex();
            target.addVertex(newVertex);
            target.addEdge(newVertex, centerVertex);
        }
    }
}

// End StarGraphGenerator.java
