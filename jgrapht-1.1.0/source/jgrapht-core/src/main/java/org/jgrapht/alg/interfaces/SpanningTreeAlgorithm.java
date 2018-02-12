/*
 * (C) Copyright 2013-2017, by Alexey Kudinkin and Contributors.
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
package org.jgrapht.alg.interfaces;

import java.io.*;
import java.util.*;

/**
 * An algorithm which computes a <a href="https://en.wikipedia.org/wiki/Spanning_tree"> spanning
 * tree</a> of a given connected graph. In the case of disconnected graphs it would rather derive a
 * spanning <i>forest</i>.
 *
 * @param <E> the graph edge type
 */
public interface SpanningTreeAlgorithm<E>
{
    /**
     * Computes a spanning tree.
     *
     * @return a spanning tree
     */
    SpanningTree<E> getSpanningTree();

    /**
     * A spanning tree.
     *
     * @param <E> the graph edge type
     */
    interface SpanningTree<E>
        extends Iterable<E>
    {
        /**
         * Returns the weight of the spanning tree.
         * 
         * @return weight of the spanning tree
         */
        double getWeight();

        /**
         * Set of edges of the spanning tree.
         * 
         * @return edge set of the spanning tree
         */
        Set<E> getEdges();

        /**
         * Returns an iterator over the edges in the spanning tree.
         * 
         * @return iterator over the edges in the spanning tree.
         */
        @Override
        default Iterator<E> iterator()
        {
            return getEdges().iterator();
        }
    }

    /**
     * Default implementation of the spanning tree interface.
     *
     * @param <E> the graph edge type
     */
    class SpanningTreeImpl<E>
        implements SpanningTree<E>, Serializable
    {
        private static final long serialVersionUID = 402707108331703333L;

        private final double weight;
        private final Set<E> edges;

        /**
         * Construct a new spanning tree.
         *
         * @param edges the edges
         * @param weight the weight
         */
        public SpanningTreeImpl(Set<E> edges, double weight)
        {
            this.edges = edges;
            this.weight = weight;
        }

        @Override
        public double getWeight()
        {
            return weight;
        }

        @Override
        public Set<E> getEdges()
        {
            return edges;
        }

        @Override
        public String toString()
        {
            return "Spanning-Tree [weight=" + weight + ", edges=" + edges + "]";
        }
    }

}
