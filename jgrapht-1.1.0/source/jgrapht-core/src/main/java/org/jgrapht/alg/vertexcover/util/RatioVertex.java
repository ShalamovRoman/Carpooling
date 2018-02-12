/*
 * (C) Copyright 2016-2017, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.vertexcover.util;

import java.util.*;

import org.jgrapht.util.*;

/**
 * Helper class for vertex covers. Guarantees that vertices can be sorted, thereby obtaining a
 * unique ordering.
 *
 * @param <V> the graph vertex type
 *
 * @author Joris Kinable
 */
public class RatioVertex<V>
    implements Comparable<RatioVertex<V>>
{
    /** original vertex **/
    public final V v;

    /** weight of the vertex **/
    public double weight;

    /** unique id, used to guarantee that compareTo never returns 0 **/
    public final int ID;

    /** degree of this vertex **/
    protected int degree = 0;

    /** Map of neighbors, and a count of the number of edges to this neighbor **/
    public final Map<RatioVertex<V>, Integer> neighbors;

    /**
     * Create a new ratio vertex
     * 
     * @param ID unique id
     * @param v the vertex
     * @param weight the vertex weight
     */
    public RatioVertex(int ID, V v, double weight)
    {
        this.ID = ID;
        this.v = v;
        this.weight = weight;
        neighbors = new LinkedHashMap<>();
    }

    /**
     * Add a neighbor.
     * 
     * @param v the neighbor
     */
    public void addNeighbor(RatioVertex<V> v)
    {
        if (!neighbors.containsKey(v))
            neighbors.put(v, 1);
        else
            neighbors.put(v, neighbors.get(v) + 1);
        degree++;

        assert (neighbors.values().stream().mapToInt(Integer::intValue).sum() == degree);
    }

    /**
     * Remove a neighbor.
     * 
     * @param v the neighbor to remove
     */
    public void removeNeighbor(RatioVertex<V> v)
    {
        degree -= neighbors.get(v);
        neighbors.remove(v);
    }

    /**
     * Returns the degree of the vertex
     * 
     * @return degree of the vertex
     */
    public int getDegree()
    {
        return degree;
    }

    /**
     * Returns the ratio between the vertex' weight and its degree
     * 
     * @return the ratio between the vertex' weight and its degree
     */
    public double getRatio()
    {
        return weight / degree;
    }

    @Override
    public int compareTo(RatioVertex<V> other)
    {
        if (this.ID == other.ID) // Same vertex
            return 0;
        int result = Double.compare(this.getRatio(), other.getRatio());
        if (result == 0) // If vertices have the same value, resolve tie by an ID comparison
            return Integer.compare(this.ID, other.ID);
        else
            return result;
    }

    @Override
    public int hashCode()
    {
        return ID;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        else if (!(o instanceof RatioVertex))
            return false;
        RatioVertex<V> other = TypeUtil.uncheckedCast(o, null);
        return this.ID == other.ID;
    }

    @Override
    public String toString()
    {
        return "v" + ID + "(" + degree + ")";
    }
}
