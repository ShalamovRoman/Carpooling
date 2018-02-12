/*
 * (C) Copyright 2005-2017, by Charles Fry, Dimitrios Michail and Contributors.
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

import java.io.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.util.*;

/**
 * Exports a graph to a plain text matrix format, which can be processed by matrix manipulation
 * software, such as <a href="http://rs.cipr.uib.no/mtj/"> MTJ</a> or
 * <a href="http://www.mathworks.com/products/matlab/">MATLAB</a>.
 * 
 * <p>
 * The exporter supports three different formats, see {@link Format}.
 * </p>
 * 
 * @see Format
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Charles Fry
 * @author Dimitrios Michail
 * @deprecated Use {@link org.jgrapht.io.MatrixExporter} instead.
 */
@Deprecated
public class MatrixExporter<V, E>
    implements GraphExporter<V, E>
{
    private final String delimiter = " ";
    private Format format;
    private ComponentNameProvider<V> vertexIDProvider;

    /**
     * Formats supported by the exporter.
     * 
     * @deprecated Use {@link org.jgrapht.io.MatrixExporter.Format} instead.
     */
    @Deprecated
    public enum Format
    {
        /**
         * A sparse representation of the adjacency matrix. This is the default. Exports the
         * specified graph into a plain text file format containing a sparse representation of the
         * graph's adjacency matrix. The value stored in each position of the matrix indicates the
         * number of edges between two vertices. With an undirected graph, the adjacency matrix is
         * symmetric.
         */
        SPARSE_ADJACENCY_MATRIX,
        /**
         * A sparse representation of the Laplacian.
         */
        SPARSE_LAPLACIAN_MATRIX,
        /**
         * A sparse representation of the normalized Laplacian.
         */
        SPARSE_NORMALIZED_LAPLACIAN_MATRIX,
    }

    /**
     * Creates a new MatrixExporter with integer name provider for the vertex identifiers and
     * {@link Format#SPARSE_ADJACENCY_MATRIX} as the default format.
     */
    public MatrixExporter()
    {
        this(Format.SPARSE_ADJACENCY_MATRIX, new IntegerComponentNameProvider<>());
    }

    /**
     * Creates a new MatrixExporter with integer name provider for the vertex identifiers.
     * 
     * @param format format to use
     */
    public MatrixExporter(Format format)
    {
        this(format, new IntegerComponentNameProvider<>());
    }

    /**
     * Creates a new MatrixExporter.
     * 
     * @param format format to use
     * @param vertexIDProvider for generating vertex identifiers. Must not be null.
     */
    public MatrixExporter(Format format, ComponentNameProvider<V> vertexIDProvider)
    {
        this.format = format;
        if (vertexIDProvider == null) {
            throw new IllegalArgumentException("Vertex ID provider must not be null");
        }
        this.vertexIDProvider = vertexIDProvider;
    }

    /**
     * Get the format that the exporter is using.
     * 
     * @return the output format
     */
    public Format getFormat()
    {
        return format;
    }

    /**
     * Set the output format of the exporter
     * 
     * @param format the format to use
     */
    public void setFormat(Format format)
    {
        this.format = format;
    }

    @Override
    public void exportGraph(Graph<V, E> g, Writer writer)
        throws ExportException
    {
        switch (format) {
        case SPARSE_ADJACENCY_MATRIX:
            exportAdjacencyMatrix(g, writer);
            break;
        case SPARSE_LAPLACIAN_MATRIX:
            if (g instanceof UndirectedGraph<?, ?>) {
                exportLaplacianMatrix((UndirectedGraph<V, E>) g, writer);
            } else {
                throw new ExportException(
                    "Exporter can only export undirected graphs in this format");
            }
            break;
        case SPARSE_NORMALIZED_LAPLACIAN_MATRIX:
            if (g instanceof UndirectedGraph<?, ?>) {
                exportNormalizedLaplacianMatrix((UndirectedGraph<V, E>) g, writer);
            } else {
                throw new ExportException(
                    "Exporter can only export undirected graphs in this format");
            }
            break;
        }
    }

    private void exportAdjacencyMatrix(Graph<V, E> g, Writer writer)
    {
        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            vertexIDProvider.getName(from);
        }

        PrintWriter out = new PrintWriter(writer);

        if (g instanceof DirectedGraph<?, ?>) {
            for (V from : g.vertexSet()) {
                exportAdjacencyMatrixVertex(
                    out, from, Graphs.successorListOf((DirectedGraph<V, E>) g, from));
            }
        } else {
            for (V from : g.vertexSet()) {
                exportAdjacencyMatrixVertex(out, from, Graphs.neighborListOf(g, from));
            }
        }

        out.flush();
    }

    private void exportAdjacencyMatrixVertex(PrintWriter writer, V from, List<V> neighbors)
    {
        String fromName = vertexIDProvider.getName(from);
        Map<String, ModifiableInteger> counts = new LinkedHashMap<>();
        for (V to : neighbors) {
            String toName = vertexIDProvider.getName(to);
            ModifiableInteger count = counts.get(toName);
            if (count == null) {
                count = new ModifiableInteger(0);
                counts.put(toName, count);
            }

            count.increment();
            if (from.equals(to)) {
                // count loops twice, once for each end
                count.increment();
            }
        }
        for (Map.Entry<String, ModifiableInteger> entry : counts.entrySet()) {
            String toName = entry.getKey();
            ModifiableInteger count = entry.getValue();
            exportEntry(writer, fromName, toName, count.toString());
        }
    }

    private void exportEntry(PrintWriter writer, String from, String to, String value)
    {
        writer.println(from + delimiter + to + delimiter + value);
    }

    private void exportLaplacianMatrix(UndirectedGraph<V, E> g, Writer writer)
    {
        PrintWriter out = new PrintWriter(writer);

        ComponentNameProvider<V> nameProvider = new IntegerComponentNameProvider<>();
        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            nameProvider.getName(from);
        }

        for (V from : g.vertexSet()) {
            String fromName = nameProvider.getName(from);

            List<V> neighbors = Graphs.neighborListOf(g, from);
            exportEntry(out, fromName, fromName, Integer.toString(neighbors.size()));
            for (V to : neighbors) {
                String toName = nameProvider.getName(to);
                exportEntry(out, fromName, toName, "-1");
            }
        }

        out.flush();
    }

    private void exportNormalizedLaplacianMatrix(UndirectedGraph<V, E> g, Writer writer)
    {
        PrintWriter out = new PrintWriter(writer);

        ComponentNameProvider<V> nameProvider = new IntegerComponentNameProvider<>();
        for (V from : g.vertexSet()) {
            // assign ids in vertex set iteration order
            nameProvider.getName(from);
        }

        for (V from : g.vertexSet()) {
            String fromName = nameProvider.getName(from);
            Set<V> neighbors = new LinkedHashSet<>(Graphs.neighborListOf(g, from));
            if (neighbors.isEmpty()) {
                exportEntry(out, fromName, fromName, "0");
            } else {
                exportEntry(out, fromName, fromName, "1");

                for (V to : neighbors) {
                    String toName = nameProvider.getName(to);
                    double value = -1 / Math.sqrt(g.degreeOf(from) * g.degreeOf(to));
                    exportEntry(out, fromName, toName, Double.toString(value));
                }
            }
        }

        out.flush();
    }

}

// End MatrixExporter.java
