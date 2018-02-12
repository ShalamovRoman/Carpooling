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

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

/**
 * A base implementation of the shortest path interface.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
abstract class BaseShortestPathAlgorithm<V, E>
    implements ShortestPathAlgorithm<V, E>
{
    /**
     * Error message for reporting the existence of a negative-weight cycle.
     */
    static final String GRAPH_CONTAINS_A_NEGATIVE_WEIGHT_CYCLE =
        "Graph contains a negative-weight cycle";
    /**
     * Error message for reporting that a source vertex is missing.
     */
    static final String GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX =
        "Graph must contain the source vertex!";
    /**
     * Error message for reporting that a sink vertex is missing.
     */
    static final String GRAPH_MUST_CONTAIN_THE_SINK_VERTEX = "Graph must contain the sink vertex!";

    /**
     * The underlying graph.
     */
    protected final Graph<V, E> graph;

    /**
     * Constructs a new instance of the algorithm for a given graph.
     * 
     * @param graph the graph
     */
    public BaseShortestPathAlgorithm(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph is null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SingleSourcePaths<V, E> getPaths(V source)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("graph must contain the source vertex");
        }

        Map<V, GraphPath<V, E>> paths = new HashMap<>();
        for (V v : graph.vertexSet()) {
            paths.put(v, getPath(source, v));
        }
        return new ListSingleSourcePathsImpl<>(graph, source, paths);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getPathWeight(V source, V sink)
    {
        GraphPath<V, E> p = getPath(source, sink);
        if (p == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return p.getWeight();
        }
    }

    /**
     * Create an empty path. Returns null if the source vertex is different than the target vertex.
     * 
     * @param source the source vertex
     * @param sink the sink vertex
     * @return an empty path or null null if the source vertex is different than the target vertex
     */
    protected final GraphPath<V, E> createEmptyPath(V source, V sink)
    {
        if (source.equals(sink)) {
            return GraphWalk.singletonWalk(graph, source, 0d);
        } else {
            return null;
        }
    }

}
