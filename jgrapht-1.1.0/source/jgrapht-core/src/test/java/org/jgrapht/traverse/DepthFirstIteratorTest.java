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
import org.jgrapht.graph.*;

/**
 * Tests for the {@link DepthFirstIteratorTest} class.
 *
 * <p>
 * NOTE: This test uses hard-coded expected ordering isn't really guaranteed by the specification of
 * the algorithm. This could cause false failures if the traversal implementation changes.
 * </p>
 *
 * @author Liviu Rau, Patrick Sharp
 * @since Jul 30, 2003
 */
public class DepthFirstIteratorTest
    extends CrossComponentIteratorTest
{
    // ~ Methods ----------------------------------------------------------------

    @Override
    String getExpectedStr1()
    {
        return "1,3,6,5,7,9,4,8,2";
    }

    @Override
    String getExpectedStr2()
    {
        return "1,3,6,5,7,9,4,8,2,orphan";
    }

    @Override
    String getExpectedFinishString()
    {
        return "6:4:9:2:8:7:5:3:1:orphan:";
    }

    @Override
    String getExpectedCCStr1()
    {
        return "orphan";
    }

    @Override
    String getExpectedCCStr2()
    {
        return "orphan,7,9,4,8,2";
    }

    @Override
    String getExpectedCCStr3()
    {
        return "orphan,7,9,4,8,2,3,6,1,5";
    }

    @Override
    String getExpectedCCFinishString()
    {
        return "orphan:4:9:2:8:7:1:6:5:3:";
    }

    @Override
    AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(
        Graph<String, DefaultWeightedEdge> g, Iterable<String> startVertex)
    {
        return new DepthFirstIterator<>(g, startVertex);
    }

    @Override
    AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(
        Graph<String, DefaultWeightedEdge> g, String vertex)
    {
        AbstractGraphIterator<String, DefaultWeightedEdge> i = new DepthFirstIterator<>(g, vertex);
        i.setCrossComponentTraversal(true);

        return i;
    }

    /**
     * See <a href="http://sf.net/projects/jgrapht">Sourceforge bug 1169182</a> for details.
     */
    public void testBug1169182()
    {
        Graph<String, DefaultEdge> dg = new DefaultDirectedGraph<>(DefaultEdge.class);

        String a = "A";
        String b = "B";
        String c = "C";
        String d = "D";
        String e = "E";
        String f = "F";
        String g = "G";
        String h = "H";
        String i = "I";
        String j = "J";
        String k = "K";
        String l = "L";

        dg.addVertex(a);
        dg.addVertex(b);
        dg.addVertex(c);
        dg.addVertex(d);
        dg.addVertex(e);
        dg.addVertex(f);
        dg.addVertex(g);
        dg.addVertex(h);
        dg.addVertex(i);
        dg.addVertex(j);
        dg.addVertex(k);
        dg.addVertex(l);

        dg.addEdge(a, b);
        dg.addEdge(b, c);
        dg.addEdge(c, j);
        dg.addEdge(c, d);
        dg.addEdge(c, e);
        dg.addEdge(c, f);
        dg.addEdge(c, g);
        dg.addEdge(d, h);
        dg.addEdge(e, h);
        dg.addEdge(f, i);
        dg.addEdge(g, i);
        dg.addEdge(h, j);
        dg.addEdge(i, c);
        dg.addEdge(j, k);
        dg.addEdge(k, l);

        Iterator<String> dfs = new DepthFirstIterator<>(dg);
        String actual = "";
        while (dfs.hasNext()) {
            String v = dfs.next();
            actual += v;
        }

        String expected = "ABCGIFEHJKLD";
        assertEquals(expected, actual);
    }
}

// End DepthFirstIteratorTest.java
