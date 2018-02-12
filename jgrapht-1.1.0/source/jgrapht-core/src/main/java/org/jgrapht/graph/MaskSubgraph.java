/*
 * (C) Copyright 2007-2017, by France Telecom and Contributors.
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

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.jgrapht.*;

/**
 * An unmodifiable subgraph induced by a vertex/edge masking function. The subgraph will keep track
 * of edges being added to its vertex subset as well as deletion of edges and vertices. When
 * iterating over the vertices/edges, it will iterate over the vertices/edges of the base graph and
 * discard vertices/edges that are masked (an edge with a masked extremity vertex is discarded as
 * well).
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @since July 5, 2007
 */
public class MaskSubgraph<V, E>
    extends AbstractGraph<V, E>
    implements Serializable
{
    private static final long serialVersionUID = -7397441126669119179L;

    private static final String UNMODIFIABLE = "this graph is unmodifiable";

    protected final Graph<V, E> base;
    protected final GraphType baseType;
    protected final Set<E> edges;
    protected final Set<V> vertices;
    protected final Predicate<V> vertexMask;
    protected final Predicate<E> edgeMask;

    /**
     * Creates a new induced subgraph. Running-time = O(1).
     *
     * @param base the base (backing) graph on which the subgraph will be based.
     * @param vertexMask vertices to exclude in the subgraph. If a vertex is masked, it is as if it
     *        is not in the subgraph. Edges incident to the masked vertex are also masked.
     * @param edgeMask edges to exclude in the subgraph. If an edge is masked, it is as if it is not
     *        in the subgraph.
     */
    public MaskSubgraph(Graph<V, E> base, Predicate<V> vertexMask, Predicate<E> edgeMask)
    {
        super();
        this.base = Objects.requireNonNull(base, "Invalid graph provided");
        this.baseType = base.getType();
        this.vertexMask = Objects.requireNonNull(vertexMask, "Invalid vertex mask provided");
        this.edgeMask = Objects.requireNonNull(edgeMask, "Invalid edge mask provided");
        this.vertices = new MaskVertexSet<>(base.vertexSet(), vertexMask);
        this.edges = new MaskEdgeSet<>(base, base.edgeSet(), vertexMask, edgeMask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E edge)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addVertex(V v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsEdge(E e)
    {
        return edgeSet().contains(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsVertex(V v)
    {
        return vertexSet().contains(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgeSet()
    {
        return edges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgesOf(V vertex)
    {
        assertVertexExist(vertex);

        return new MaskEdgeSet<>(base, base.edgesOf(vertex), vertexMask, edgeMask);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * By default this method returns the sum of in-degree and out-degree. The exact value returned
     * depends on the type of the underlying graph.
     */
    @Override
    public int degreeOf(V vertex)
    {
        if (baseType.isDirected()) {
            return inDegreeOf(vertex) + outDegreeOf(vertex);
        } else {
            int degree = 0;
            Iterator<E> it = edgesOf(vertex).iterator();
            while (it.hasNext()) {
                E e = it.next();
                degree++;
                if (getEdgeSource(e).equals(getEdgeTarget(e))) {
                    degree++;
                }
            }
            return degree;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> incomingEdgesOf(V vertex)
    {
        assertVertexExist(vertex);

        return new MaskEdgeSet<>(base, base.incomingEdgesOf(vertex), vertexMask, edgeMask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inDegreeOf(V vertex)
    {
        if (baseType.isUndirected()) {
            return degreeOf(vertex);
        } else {
            return incomingEdgesOf(vertex).size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> outgoingEdgesOf(V vertex)
    {
        assertVertexExist(vertex);

        return new MaskEdgeSet<>(base, base.outgoingEdgesOf(vertex), vertexMask, edgeMask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int outDegreeOf(V vertex)
    {
        if (baseType.isUndirected()) {
            return degreeOf(vertex);
        } else {
            return outgoingEdgesOf(vertex).size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        if (containsVertex(sourceVertex) && containsVertex(targetVertex)) {
            return new MaskEdgeSet<>(
                base, base.getAllEdges(sourceVertex, targetVertex), vertexMask, edgeMask);
        } else
            return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        Set<E> edges = getAllEdges(sourceVertex, targetVertex);

        if (edges == null) {
            return null;
        } else {
            return edges.stream().findAny().orElse(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EdgeFactory<V, E> getEdgeFactory()
    {
        return base.getEdgeFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeSource(E edge)
    {
        assert (edgeSet().contains(edge));

        return base.getEdgeSource(edge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeTarget(E edge)
    {
        assert (edgeSet().contains(edge));

        return base.getEdgeTarget(edge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphType getType()
    {
        return baseType.asUnmodifiable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getEdgeWeight(E edge)
    {
        assert (edgeSet().contains(edge));

        return base.getEdgeWeight(edge);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEdgeWeight(E edge, double weight)
    {
        assert (edgeSet().contains(edge));

        base.setEdgeWeight(edge, weight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAllEdges(Collection<? extends E> edges)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> removeAllEdges(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAllVertices(Collection<? extends V> vertices)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeEdge(E e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeVertex(V v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<V> vertexSet()
    {
        return vertices;
    }

}

// End MaskSubgraph.java
