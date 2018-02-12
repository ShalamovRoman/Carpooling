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
package org.jgrapht.ext;

/**
 * Supported CSV formats.
 * 
 * <ul>
 * <li>
 * <p>
 * Format {@link #EDGE_LIST} contains one edge per line. The following example
 * 
 * <pre>
 * a,b
 * b,c
 * </pre>
 * 
 * represents a graph with two edges: a-&gt;b and b-&gt;c.</li>
 * 
 * <li>
 * <p>
 * Format {@link #ADJACENCY_LIST} contains the adjacency list of each vertex per line. The first
 * field on a line is a vertex while the remaining fields are its neighbors.
 * 
 * <pre>
 * a,b
 * b,c,d
 * c,a,c,d
 * </pre>
 * 
 * represents a graph with edges: a-&gt;b, b-&gt;c, b-&gt;d, c-&gt;a, c-&gt;c, c-&gt;d.
 * 
 * <p>
 * Mixed variants of {@link #EDGE_LIST} and {@link #ADJACENCY_LIST} are also considered valid. As an
 * example consider the following input
 * 
 * <pre>
 * a,b
 * b,a
 * d,a
 * c,a,b
 * b,d,a
 * </pre>
 * 
 * which represents a graph with edges: a-&gt;b, b-&gt;a, d-&gt;a, c-&gt;a, c-&gt;b, b-&gt;d,
 * b-&gt;a. Multiple occurrences of the same edge result into a multi-graph.
 * 
 * </li>
 * <li>
 * <p>
 * Format {@link #MATRIX} outputs an adjacency matrix representation of the graph. Each line
 * represents a vertex.
 * 
 * The following
 * 
 * <pre>
 * 0,1,0,1,0
 * 1,0,0,0,0
 * 0,0,1,0,0
 * 0,1,0,1,0
 * 0,0,0,0,0
 * </pre>
 * 
 * represents a graph with five vertices 1,2,3,4,5 which contains edges: 1-&gt;2, 1-&gt;4, 2-&gt;1,
 * 3-&gt;3, 4-&gt;2, 4-&gt;4.
 * 
 * <p>
 * In case {@link CSVFormat.Parameter#MATRIX_FORMAT_ZERO_WHEN_NO_EDGE} is not set the equivalent
 * format would be:
 * 
 * <pre>
 * ,1,,1,
 * 1,,,,
 * ,,1,,
 * ,1,,1,
 * ,,,,
 * </pre>
 * 
 * <p>
 * Weighted variants are also valid if {@link CSVFormat.Parameter#MATRIX_FORMAT_EDGE_WEIGHTS} is
 * set. The above example would then be:
 * 
 * <pre>
 * ,1.0,,1.0,
 * 1.0,,,,
 * ,,1.0,,
 * ,1.0,,1.0,
 * ,,,,
 * </pre>
 * 
 * If additionally {@link CSVFormat.Parameter#MATRIX_FORMAT_ZERO_WHEN_NO_EDGE} is set then a zero as
 * an integer means that the corresponding edge is missing, while a zero as a double means than the
 * edge exists and has zero weight.
 * 
 * <p>
 * If parameter {@link CSVFormat.Parameter#MATRIX_FORMAT_NODEID} is set then node identifiers are
 * also included as in the following example:
 * 
 * <pre>
 * ,a,b,c,d,e
 * a,,1,,1,
 * b,1,,,,
 * c,,,1,,
 * d,,1,,1,
 * e,,,,,
 * </pre>
 * 
 * In the above example the first line contains the node identifiers and the first field of each
 * line contain the vertex it corresponds to. In case node identifiers are present line-shuffled
 * input is also valid such as:
 * 
 * <pre>
 * ,a,b,c,d,e
 * c,,,1,,
 * b,1,,,,
 * e,,,,,
 * d,,1,,1,
 * a,,1,,1,
 * </pre>
 * 
 * The last example represents the graph with edges: a-&gt;b, a-&gt;d, b-&gt;a, c-&gt;c, d-&gt;b,
 * d-&gt;d.
 * 
 * </li>
 * </ul>
 * 
 * @author Dimitrios Michail
 * @since August 2016
 * @deprecated Use {@link org.jgrapht.io.CSVFormat} instead.
 */
@Deprecated
public enum CSVFormat
{
    /**
     * Edge list
     */
    EDGE_LIST,
    /**
     * Adjacency list
     */
    ADJACENCY_LIST,
    /**
     * Matrix
     */
    MATRIX;

    /**
     * Parameters that affect the behavior of CVS importers/exporters.
     * 
     * @deprecated Use {@link org.jgrapht.io.CSVFormat.Parameter} instead.
     */
    @Deprecated
    public enum Parameter
    {
        /**
         * Whether to import/export node ids. Only valid for the {@link CSVFormat#MATRIX MATRIX}
         * format.
         */
        MATRIX_FORMAT_NODEID,
        /**
         * Whether to import/export edge weights. Only valid for the {@link CSVFormat#MATRIX MATRIX}
         * format.
         */
        MATRIX_FORMAT_EDGE_WEIGHTS,
        /**
         * Whether the input/output contains zero for missing edges. Only valid for the
         * {@link CSVFormat#MATRIX MATRIX} format.
         */
        MATRIX_FORMAT_ZERO_WHEN_NO_EDGE,
    }

}

// End CSVFormat.java
