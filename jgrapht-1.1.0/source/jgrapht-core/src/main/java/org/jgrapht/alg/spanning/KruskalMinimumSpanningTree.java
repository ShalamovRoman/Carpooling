/*
 * (C) Copyright 2010-2017, by Tom Conerly and Contributors.
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
package org.jgrapht.alg.spanning;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;

/**
 * An implementation of <a href="http://en.wikipedia.org/wiki/Kruskal's_algorithm">Kruskal's minimum
 * spanning tree algorithm</a>. If the given graph is connected it computes the minimum spanning
 * tree, otherwise it computes the minimum spanning forest. The algorithm runs in time O(E log E).
 * This implementation uses the hashCode and equals method of the vertices.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Tom Conerly
 * @since Feb 10, 2010
 */
public class KruskalMinimumSpanningTree<V, E>
    implements SpanningTreeAlgorithm<E>
{
    private final Graph<V, E> graph;

    /**
     * Construct a new instance of the algorithm.
     * 
     * @param graph the input graph
     */
    public KruskalMinimumSpanningTree(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanningTree<E> getSpanningTree()
    {
        UnionFind<V> forest = new UnionFind<>(graph.vertexSet());
        ArrayList<E> allEdges = new ArrayList<>(graph.edgeSet());
        Collections.sort(
            allEdges, (edge1, edge2) -> Double
                .valueOf(graph.getEdgeWeight(edge1)).compareTo(graph.getEdgeWeight(edge2)));

        double spanningTreeCost = 0;
        Set<E> edgeList = new HashSet<>();

        for (E edge : allEdges) {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            if (forest.find(source).equals(forest.find(target))) {
                continue;
            }

            forest.union(source, target);
            edgeList.add(edge);
            spanningTreeCost += graph.getEdgeWeight(edge);
        }

        return new SpanningTreeImpl<>(edgeList, spanningTreeCost);
    }
}

// End KruskalMinimumSpanningTree.java
