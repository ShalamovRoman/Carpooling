/*
 * (C) Copyright 2017-2017, by Dimitrios Michail and Contributors.
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
 * Barabási-Albert growth and preferential attachment graph generator.
 * 
 * <p>
 * The generator is described in the paper: A.-L. Barabási and R. Albert. Emergence of scaling in
 * random networks. Science, 286:509-512, 1999.
 * 
 * <p>
 * The generator starts with a complete graph of m0 nodes and grows the network by adding n-m0
 * additional nodes. The additional nodes are added one by one and each of them is connected to m
 * previously added nodes, where the probability of connecting to a node is proportional to its
 * degree.
 * 
 * <p>
 * Note that the Barabàsi-Albert model is designed for undirected networks. Nevertheless, this
 * generator also works with directed networks where the probabilities are proportional to the sum
 * of incoming and outgoing degrees. For a more general discussion see the paper: M. E. J. Newman.
 * The Structure and Function of Complex Networks. SIAM Rev., 45(2):167--256, 2003.
 * 
 * @author Dimitrios Michail
 * @since February 2017
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class BarabasiAlbertGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
    private final Random rng;
    private final int m0;
    private final int m;
    private final int n;

    /**
     * Constructor
     * 
     * @param m0 number of initial nodes
     * @param m number of edges of each new node added during the network growth
     * @param n final number of nodes
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public BarabasiAlbertGraphGenerator(int m0, int m, int n)
    {
        this(m0, m, n, new Random());
    }

    /**
     * Constructor
     * 
     * @param m0 number of initial nodes
     * @param m number of edges of each new node added during the network growth
     * @param n final number of nodes
     * @param seed seed for the random number generator
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public BarabasiAlbertGraphGenerator(int m0, int m, int n, long seed)
    {
        this(m0, m, n, new Random(seed));
    }

    /**
     * Constructor
     * 
     * @param m0 number of initial nodes
     * @param m number of edges of each new node added during the network growth
     * @param n final number of nodes
     * @param rng the random number generator to use
     * @throws IllegalArgumentException in case of invalid parameters
     */
    public BarabasiAlbertGraphGenerator(int m0, int m, int n, Random rng)
    {
        if (m0 < 1) {
            throw new IllegalArgumentException("invalid initial nodes (" + m0 + " < 1)");
        }
        this.m0 = m0;
        if (m <= 0) {
            throw new IllegalArgumentException("invalid edges per node (" + m + " <= 0");
        }
        if (m > m0) {
            throw new IllegalArgumentException("invalid edges per node (" + m + " > " + m0 + ")");
        }
        this.m = m;
        if (n < m0) {
            throw new IllegalArgumentException(
                "total number of nodes must be at least equal to the initial set");
        }
        this.n = n;
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
    }

    /**
     * Generates an instance.
     * 
     * @param target the target graph
     * @param vertexFactory the vertex factory
     * @param resultMap not used by this generator, can be null
     */
    @Override
    public void generateGraph(
        Graph<V, E> target, VertexFactory<V> vertexFactory, Map<String, V> resultMap)
    {
        /*
         * Create complete graph with m0 nodes
         */
        Set<V> oldNodes = new HashSet<>(target.vertexSet());
        Set<V> newNodes = new HashSet<>();
        new CompleteGraphGenerator<V, E>(m0).generateGraph(target, vertexFactory, resultMap);
        target.vertexSet().stream().filter(v -> !oldNodes.contains(v)).forEach(newNodes::add);

        List<V> nodes = new ArrayList<>(n * m);
        nodes.addAll(newNodes);
        /*
         * Augment node list to have node multiplicity equal to min(1,m0-1).
         */
        for (int i = 0; i < m0 - 2; i++) {
            nodes.addAll(newNodes);
        }

        /*
         * Grow network with preferential attachment
         */
        for (int i = m0; i < n; i++) {
            V v = vertexFactory.createVertex();
            if (!target.addVertex(v)) {
                throw new IllegalArgumentException("Invalid vertex factory");
            }

            List<V> newEndpoints = new ArrayList<>();
            int added = 0;
            while (added < m) {
                V u = nodes.get(rng.nextInt(nodes.size()));
                if (!target.containsEdge(v, u)) {
                    target.addEdge(v, u);
                    added++;
                    newEndpoints.add(v);
                    if (i > 1) {
                        newEndpoints.add(u);
                    }
                }
            }
            nodes.addAll(newEndpoints);
        }

    }

}
