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
package org.jgrapht.generate;

import java.util.*;

import org.jgrapht.*;

/**
 * Create a random bipartite graph based on the G(n, p) Erdős–Rényi model. See the Wikipedia article
 * for details and references about <a href="https://en.wikipedia.org/wiki/Random_graph">Random
 * Graphs</a> and the
 * <a href="https://en.wikipedia.org/wiki/Erd%C5%91s%E2%80%93R%C3%A9nyi_model">Erdős–Rényi model</a>
 * .
 * 
 * The user provides the sizes n1 and n2 of the two partitions (n1+n2=n) and the probability p of
 * the existence of an edge. The generator supports both directed and undirected graphs.
 *
 * @author Dimitrios Michail
 * @since September 2016
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see GnmRandomBipartiteGraphGenerator
 */
public class GnpRandomBipartiteGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
    private final Random rng;
    private final int n1;
    private final int n2;
    private final double p;

    private Map<Integer, V> partitionA;
    private Map<Integer, V> partitionB;

    /**
     * Create a new random bipartite graph generator. The generator uses the G(n, p) model when n =
     * n1 + n2 and the bipartite graph has one partition with size n1 and one partition with size
     * n2. An edge between two vertices of different partitions is included with probability p
     * independent of all other edges.
     * 
     * @param n1 number of vertices of the first partition
     * @param n2 number of vertices of the second partition
     * @param p edge probability
     */
    public GnpRandomBipartiteGraphGenerator(int n1, int n2, double p)
    {
        this(n1, n2, p, new Random());
    }

    /**
     * Create a new random bipartite graph generator. The generator uses the G(n, p) model when n =
     * n1 + n2, the bipartite graph has partition with size n1 and a partition with size n2. An edge
     * between two vertices of different partitions is included with probability p independent of
     * all other edges.
     * 
     * @param n1 number of vertices of the first partition
     * @param n2 number of vertices of the second partition
     * @param p edge probability
     * @param seed seed for the random number generator
     */
    public GnpRandomBipartiteGraphGenerator(int n1, int n2, double p, long seed)
    {
        this(n1, n2, p, new Random(seed));
    }

    /**
     * Create a new random bipartite graph generator. The generator uses the G(n, p) model when n =
     * n1 + n2, the bipartite graph has partition with size n1 and a partition with size n2. An edge
     * between two vertices of different partitions is included with probability p independent of
     * all other edges.
     * 
     * @param n1 number of vertices of the first partition
     * @param n2 number of vertices of the second partition
     * @param p edge probability
     * @param rng random number generator
     */
    public GnpRandomBipartiteGraphGenerator(int n1, int n2, double p, Random rng)
    {
        if (n1 < 0) {
            throw new IllegalArgumentException("number of vertices must be non-negative");
        }
        this.n1 = n1;
        if (n2 < 0) {
            throw new IllegalArgumentException("number of vertices must be non-negative");
        }
        this.n2 = n2;
        if (p < 0.0 || p > 1.0) {
            throw new IllegalArgumentException("not valid probability of edge existence");
        }
        this.p = p;
        this.rng = rng;
    }

    /**
     * Generates a random bipartite graph.
     * 
     * @param target the target graph
     * @param vertexFactory the vertex factory
     * @param resultMap not used by this generator, can be null
     */
    @Override
    public void generateGraph(
        Graph<V, E> target, VertexFactory<V> vertexFactory, Map<String, V> resultMap)
    {
        if (n1 + n2 == 0) {
            return;
        }

        // create vertices
        int previousVertexSetSize = target.vertexSet().size();

        partitionA = new LinkedHashMap<>(n1);
        for (int i = 0; i < n1; i++) {
            V v = vertexFactory.createVertex();
            target.addVertex(v);
            partitionA.put(i, v);
        }

        partitionB = new LinkedHashMap<>(n2);
        for (int i = 0; i < n2; i++) {
            V v = vertexFactory.createVertex();
            target.addVertex(v);
            partitionB.put(i, v);
        }

        if (target.vertexSet().size() != previousVertexSetSize + n1 + n2) {
            throw new IllegalArgumentException(
                "Vertex factory did not produce " + (n1 + n2) + " distinct vertices.");
        }

        // check if graph is directed
        boolean isDirected = target.getType().isDirected();

        // create edges
        for (int i = 0; i < n1; i++) {
            V s = partitionA.get(i);
            for (int j = 0; j < n2; j++) {
                V t = partitionB.get(j);

                // s->t
                if (rng.nextDouble() < p) {
                    target.addEdge(s, t);
                }

                if (isDirected) {
                    // t->s
                    if (rng.nextDouble() < p) {
                        target.addEdge(t, s);
                    }
                }
            }
        }

    }

    /**
     * Returns the first partition of vertices in the bipartite graph. This partition is guaranteed
     * to be smaller than or equal in size to the second partition.
     * 
     * @return one partition of the bipartite graph
     */
    public Set<V> getFirstPartition()
    {
        if (partitionA.size() <= partitionB.size())
            return new LinkedHashSet<>(partitionA.values());
        else
            return new LinkedHashSet<>(partitionB.values());
    }

    /**
     * Returns the second partitions of vertices in the bipartite graph. This partition is
     * guaranteed to be larger than or equal in size to the first partition.
     * 
     * @return one partition of the bipartite graph
     */
    public Set<V> getSecondPartition()
    {
        if (partitionB.size() >= partitionA.size())
            return new LinkedHashSet<>(partitionB.values());
        else
            return new LinkedHashSet<>(partitionA.values());
    }

}

// End GnpRandomBipartiteGraphGenerator.java
