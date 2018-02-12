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
package org.jgrapht.io;

import java.io.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import junit.framework.*;

/**
 * .
 * 
 * @author Dimitrios Michail
 */
public class CSVExporterTest
    extends TestCase
{
    // ~ Static fields/initializers
    // ---------------------------------------------

    private static final String NL = System.getProperty("line.separator");

    private static ComponentNameProvider<Integer> nameProvider =
        new ComponentNameProvider<Integer>()
        {
            @Override
            public String getName(Integer vertex)
            {
                return String.valueOf(vertex);
            }
        };

    private static ComponentNameProvider<String> stringNameProvider =
        new ComponentNameProvider<String>()
        {
            @Override
            public String getName(String vertex)
            {
                return vertex;
            }
        };

    // @formatter:off
    private static final String UNDIRECTED_EDGE_LIST =
          "1;2" + NL
        + "1;3" + NL
        + "3;4" + NL
        + "4;5" + NL
        + "5;1" + NL;
    
    private static final String DIRECTED_EDGE_LIST =
          "1;2" + NL
        + "1;3" + NL
        + "3;1" + NL
        + "3;4" + NL
        + "4;5" + NL
        + "5;1" + NL;
    
    private static final String UNDIRECTED_ADJACENCY_LIST =
          "1;2;3;3;5" + NL
        + "2;1;5" + NL
        + "3;1;1;4;5" + NL
        + "4;3;5;5" + NL
        + "5;4;1;2;3;4;5;5" + NL;
    
    private static final String DIRECTED_ADJACENCY_LIST =
        "1;2;3" + NL
        + "2" + NL
        + "3;1;4" + NL
        + "4;5" + NL
        + "5;1;2;3;4;5;5" + NL;
    
    private static final String DIRECTED_MATRIX_NODEID =
          ";1;2;3;4;5" + NL
        + "1;;1;1;;" + NL
        + "2;;;;;" + NL
        + "3;1;;;1;" + NL
        + "4;;;;;1" + NL
        + "5;1;1;1;1;1" + NL;
    
    private static final String DIRECTED_MATRIX_NODEID_ZERO_NO_EDGE =
        ";1;2;3;4;5" + NL
      + "1;0;1;1;0;0" + NL
      + "2;0;0;0;0;0" + NL
      + "3;1;0;0;1;0" + NL
      + "4;0;0;0;0;1" + NL
      + "5;1;1;1;1;1" + NL;
    
    private static final String DIRECTED_MATRIX_NO_NODEID =
          ";1;1;;" + NL
        + ";;;;" + NL
        + "1;;;1;" + NL
        + ";;;;1" + NL
        + "1;1;1;1;1" + NL;
    
    private static final String DIRECTED_MATRIX_NO_NODEID_ZERO_NO_EDGE =
        "0;1;1;0;0" + NL
      + "0;0;0;0;0" + NL
      + "1;0;0;1;0" + NL
      + "0;0;0;0;1" + NL
      + "1;1;1;1;1" + NL;
    
    private static final String DIRECTED_MATRIX_NO_NODEID_ZERO_NO_EDGE_WEIGHTED =
        "0;1.0;13.0;0;0" + NL
      + "0;0;0;0;0" + NL
      + "1.0;0;0;1.0;0" + NL
      + "0;0;0;0;1.0" + NL
      + "1.0;1.0;53.0;1.0;1.0" + NL;
    
    private static final String DIRECTED_MATRIX_NO_NODEID_WEIGHTED =
        ";1.0;13.0;;" + NL
      + ";;;;" + NL
      + "1.0;;;1.0;" + NL
      + ";;;;1.0" + NL
      + "1.0;1.0;53.0;1.0;1.0" + NL;
    
    private static final String DIRECTED_EDGE_LIST_ESCAPE = 
        "'john doe';fred" + NL 
      + "fred;\"fred\n\"\"21\"\"\"" + NL
      + "\"fred\n\"\"21\"\"\";\"who;;\"" +NL
      + "\"who;;\";'john doe'" + NL;
    
    //     // @formatter:on

    // ~ Methods
    // ----------------------------------------------------------------

    public void testUndirectedEdgeList()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(3, 1);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 1);

        CSVExporter<Integer, DefaultEdge> exporter =
            new CSVExporter<>(nameProvider, CSVFormat.EDGE_LIST, ';');
        StringWriter w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(UNDIRECTED_EDGE_LIST, w.toString());
    }

    public void testDirectedEdgeList()
    {
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(3, 1);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 1);

        CSVExporter<Integer, DefaultEdge> exporter =
            new CSVExporter<>(nameProvider, CSVFormat.EDGE_LIST, ';');
        StringWriter w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(DIRECTED_EDGE_LIST, w.toString());
    }

    public void testDirectedAdjacencyList()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(3, 1);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 1);
        g.addEdge(5, 2);
        g.addEdge(5, 3);
        g.addEdge(5, 4);
        g.addEdge(5, 5);
        g.addEdge(5, 5);

        CSVExporter<Integer, DefaultEdge> exporter =
            new CSVExporter<>(nameProvider, CSVFormat.ADJACENCY_LIST, ';');
        StringWriter w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(DIRECTED_ADJACENCY_LIST, w.toString());
    }

    public void testUndirectedAdjacencyList()
    {
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(3, 1);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 1);
        g.addEdge(5, 2);
        g.addEdge(5, 3);
        g.addEdge(5, 4);
        g.addEdge(5, 5);
        g.addEdge(5, 5);

        CSVExporter<Integer, DefaultEdge> exporter =
            new CSVExporter<>(nameProvider, CSVFormat.ADJACENCY_LIST, ';');
        StringWriter w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(UNDIRECTED_ADJACENCY_LIST, w.toString());
    }

    public void testDirectedMatrixNodeId()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(3, 1);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 1);
        g.addEdge(5, 2);
        g.addEdge(5, 3);
        g.addEdge(5, 4);
        g.addEdge(5, 5);

        CSVExporter<Integer, DefaultEdge> exporter =
            new CSVExporter<>(nameProvider, CSVFormat.MATRIX, ';');
        exporter.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID, true);
        StringWriter w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(DIRECTED_MATRIX_NODEID, w.toString());
    }

    public void testDirectedMatrixNoNodeId()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(3, 1);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 1);
        g.addEdge(5, 2);
        g.addEdge(5, 3);
        g.addEdge(5, 4);
        g.addEdge(5, 5);

        CSVExporter<Integer, DefaultEdge> exporter =
            new CSVExporter<>(nameProvider, CSVFormat.MATRIX, ';');
        StringWriter w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(DIRECTED_MATRIX_NO_NODEID, w.toString());
    }

    public void testDirectedMatrixNodeIdZeroMissingEdges()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(3, 1);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 1);
        g.addEdge(5, 2);
        g.addEdge(5, 3);
        g.addEdge(5, 4);
        g.addEdge(5, 5);

        CSVExporter<Integer, DefaultEdge> exporter =
            new CSVExporter<>(nameProvider, CSVFormat.MATRIX, ';');
        exporter.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID, true);
        exporter.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        StringWriter w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(DIRECTED_MATRIX_NODEID_ZERO_NO_EDGE, w.toString());
    }

    public void testDirectedMatrixNoNodeIdZeroMissingEdges()
    {
        Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(3, 1);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 1);
        g.addEdge(5, 2);
        g.addEdge(5, 3);
        g.addEdge(5, 4);
        g.addEdge(5, 5);

        CSVExporter<Integer, DefaultEdge> exporter =
            new CSVExporter<>(nameProvider, CSVFormat.MATRIX, ';');
        exporter.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        StringWriter w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(DIRECTED_MATRIX_NO_NODEID_ZERO_NO_EDGE, w.toString());
    }

    public void testDirectedMatrixNoNodeIdZeroMissingEdgesWeighted()
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(3, 1);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 1);
        g.addEdge(5, 2);
        g.addEdge(5, 3);
        g.addEdge(5, 4);
        g.addEdge(5, 5);

        g.setEdgeWeight(g.getEdge(1, 3), 13);
        g.setEdgeWeight(g.getEdge(5, 3), 53);

        CSVExporter<Integer, DefaultWeightedEdge> exporter =
            new CSVExporter<>(nameProvider, CSVFormat.MATRIX, ';');
        exporter.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        exporter.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_EDGE_WEIGHTS, true);
        StringWriter w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(DIRECTED_MATRIX_NO_NODEID_ZERO_NO_EDGE_WEIGHTED, w.toString());
    }

    public void testDirectedMatrixNoNodeIdWeighted()
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(3, 1);
        g.addEdge(3, 4);
        g.addEdge(4, 5);
        g.addEdge(5, 1);
        g.addEdge(5, 2);
        g.addEdge(5, 3);
        g.addEdge(5, 4);
        g.addEdge(5, 5);

        g.setEdgeWeight(g.getEdge(1, 3), 13);
        g.setEdgeWeight(g.getEdge(5, 3), 53);

        CSVExporter<Integer, DefaultWeightedEdge> exporter =
            new CSVExporter<>(nameProvider, CSVFormat.MATRIX, ';');
        exporter.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_EDGE_WEIGHTS, true);
        StringWriter w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(DIRECTED_MATRIX_NO_NODEID_WEIGHTED, w.toString());
    }

    public void testEdgeListWithStringsDirectedUnweightedWithSemicolon()
        throws ImportException
    {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        g.addVertex("'john doe'");
        g.addVertex("fred");
        g.addVertex("fred\n\"21\"");
        g.addVertex("who;;");
        g.addEdge("'john doe'", "fred");
        g.addEdge("fred", "fred\n\"21\"");
        g.addEdge("fred\n\"21\"", "who;;");
        g.addEdge("who;;", "'john doe'");

        CSVExporter<String, DefaultEdge> exporter =
            new CSVExporter<>(stringNameProvider, CSVFormat.EDGE_LIST, ';');
        StringWriter w = new StringWriter();
        exporter.exportGraph(g, w);
        assertEquals(DIRECTED_EDGE_LIST_ESCAPE, w.toString());
    }

}

// End CSVExporterTest.java
