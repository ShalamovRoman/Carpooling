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
package org.jgrapht.alg.spanning;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

/**
 * Greedy algorithm for (2k-1)-multiplicative spanner construction (for any integer
 * {@literal k >= 1}).
 *
 * <p>
 * The spanner is guaranteed to contain O(n^{1+1/k}) edges and the shortest path distance between
 * any two vertices in the spanner is at most 2k-1 times the corresponding shortest path distance in
 * the original graph. Here n denotes the number of vertices of the graph.
 *
 * <p>
 * The algorithm is described in: Althoefer, Das, Dobkin, Joseph, Soares.
 * <a href="https://doi.org/10.1007/BF02189308">On Sparse Spanners of Weighted Graphs</a>. Discrete
 * Computational Geometry 9(1):81-100, 1993.
 *
 * <p>
 * If the graph is unweighted the algorithm runs in O(m n^{1+1/k}) time. Setting k to infinity will
 * result in a slow version of Kruskal's algorithm where cycle detection is performed by a BFS
 * computation. In such a case use the implementation of Kruskal with union-find. Here n and m are
 * the number of vertices and edges of the graph respectively.
 *
 * <p>
 * If the graph is weighted the algorithm runs in O(m (n^{1+1/k} + nlogn)) time by using Dijkstra's
 * algorithm. Edge weights must be non-negative.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 * @since July 15, 2016
 */
public class GreedyMultiplicativeSpanner<V, E>
    implements SpannerAlgorithm<E>
{
    private final Graph<V, E> graph;
    private final int k;
    private static final int MAX_K = 1 << 29;

    /**
     * Constructs instance to compute a (2k-1)-spanner of an undirected graph.
     * 
     * @param graph an undirected graph
     * @param k positive integer.
     * 
     * @throws IllegalArgumentException if the graph is not undirected
     * @throws IllegalArgumentException if k is not positive
     */
    public GreedyMultiplicativeSpanner(Graph<V, E> graph, int k)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        if (!graph.getType().isUndirected()) {
            throw new IllegalArgumentException("graph is not undirected");
        }
        if (k <= 0) {
            throw new IllegalArgumentException(
                "k should be positive in (2k-1)-spanner construction");
        }
        this.k = Math.min(k, MAX_K);
    }

    @Override
    public Spanner<E> getSpanner()
    {
        if (graph.getType().isWeighted()) {
            return new WeightedSpannerAlgorithm().run();
        } else {
            return new UnweightedSpannerAlgorithm().run();
        }
    }

    // base algorithm implementation
    private abstract class SpannerAlgorithmBase
    {
        public abstract boolean isSpannerReachable(V s, V t, double distance);

        public abstract void addSpannerEdge(V s, V t, double weight);

        public Spanner<E> run()
        {
            // sort edges
            ArrayList<E> allEdges = new ArrayList<>(graph.edgeSet());
            Collections.sort(
                allEdges, (e1, e2) -> Double
                    .valueOf(graph.getEdgeWeight(e1)).compareTo(graph.getEdgeWeight(e2)));

            // check precondition
            double minWeight = graph.getEdgeWeight(allEdges.get(0));
            if (minWeight < 0.0) {
                throw new IllegalArgumentException("Illegal edge weight: negative");
            }

            // run main loop
            Set<E> edgeList = new LinkedHashSet<>();
            double edgeListWeight = 0d;

            for (E e : allEdges) {
                V s = graph.getEdgeSource(e);
                V t = graph.getEdgeTarget(e);

                if (!s.equals(t)) { // self-loop?
                    double eWeight = graph.getEdgeWeight(e);
                    if (!isSpannerReachable(s, t, (2 * k - 1) * eWeight)) {
                        edgeList.add(e);
                        edgeListWeight += eWeight;
                        addSpannerEdge(s, t, eWeight);
                    }
                }
            }
            return new SpannerImpl<>(edgeList, edgeListWeight);
        }

    }

    private class UnweightedSpannerAlgorithm
        extends SpannerAlgorithmBase
    {
        protected Graph<V, E> spanner;
        protected Map<V, Integer> vertexDistance;
        protected Deque<V> queue;
        protected Deque<V> touchedVertices;

        public UnweightedSpannerAlgorithm()
        {
            spanner = new SimpleGraph<V, E>(graph.getEdgeFactory());
            touchedVertices = new ArrayDeque<V>(graph.vertexSet().size());
            for (V v : graph.vertexSet()) {
                spanner.addVertex(v);
                touchedVertices.push(v);
            }
            vertexDistance = new HashMap<V, Integer>(graph.vertexSet().size());
            queue = new ArrayDeque<>();
        }

        /**
         * Check if two vertices are reachable by a BFS in the spanner graph using only a certain
         * number of hops.
         *
         * We execute this procedure repeatedly, therefore we need to keep track of what it touches
         * and only clean those before the next execution.
         */
        @Override
        public boolean isSpannerReachable(V s, V t, double hops)
        {
            // initialize distances and queue
            while (!touchedVertices.isEmpty()) {
                V u = touchedVertices.pop();
                vertexDistance.put(u, Integer.MAX_VALUE);
            }
            while (!queue.isEmpty()) {
                queue.pop();
            }

            // do BFS
            touchedVertices.push(s);
            queue.push(s);
            vertexDistance.put(s, 0);

            while (!queue.isEmpty()) {
                V u = queue.pop();
                Integer uDistance = vertexDistance.get(u);

                if (u.equals(t)) { // found
                    return uDistance <= hops;
                }

                for (E e : spanner.edgesOf(u)) {
                    V v = Graphs.getOppositeVertex(spanner, e, u);
                    Integer vDistance = vertexDistance.get(v);

                    if (vDistance == Integer.MAX_VALUE) {
                        touchedVertices.push(v);
                        vertexDistance.put(v, uDistance + 1);
                        queue.push(v);
                    }
                }
            }

            return false;
        }

        @Override
        public void addSpannerEdge(V s, V t, double weight)
        {
            spanner.addEdge(s, t);
        }
    }

    private class WeightedSpannerAlgorithm
        extends SpannerAlgorithmBase
    {
        protected Graph<V, DefaultWeightedEdge> spanner;
        protected FibonacciHeap<V> heap;
        protected Map<V, FibonacciHeapNode<V>> nodes;

        public WeightedSpannerAlgorithm()
        {
            this.spanner = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
            for (V v : graph.vertexSet()) {
                spanner.addVertex(v);
            }
            this.heap = new FibonacciHeap<V>();
            this.nodes = new LinkedHashMap<V, FibonacciHeapNode<V>>();
        }

        @Override
        public boolean isSpannerReachable(V s, V t, double distance)
        {
            // init
            heap.clear();
            nodes.clear();

            FibonacciHeapNode<V> sNode = new FibonacciHeapNode<V>(s);
            nodes.put(s, sNode);
            heap.insert(sNode, 0d);

            while (!heap.isEmpty()) {
                FibonacciHeapNode<V> uNode = heap.removeMin();
                double uDistance = uNode.getKey();
                V u = uNode.getData();

                if (uDistance > distance) {
                    return false;
                }

                if (u.equals(t)) { // found
                    return true;
                }

                for (DefaultWeightedEdge e : spanner.edgesOf(u)) {
                    V v = Graphs.getOppositeVertex(spanner, e, u);
                    FibonacciHeapNode<V> vNode = nodes.get(v);
                    double vDistance = uDistance + spanner.getEdgeWeight(e);

                    if (vNode == null) { // no distance
                        vNode = new FibonacciHeapNode<V>(v);
                        nodes.put(v, vNode);
                        heap.insert(vNode, vDistance);
                    } else if (vDistance < vNode.getKey()) {
                        heap.decreaseKey(vNode, vDistance);
                    }
                }

            }

            return false;
        }

        @Override
        public void addSpannerEdge(V s, V t, double weight)
        {
            Graphs.addEdge(spanner, s, t, weight);
        }

    }

}
