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
package org.jgrapht.alg.flow;

import java.util.*;
import java.util.stream.*;

import org.jgrapht.*;
import org.jgrapht.alg.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;

import junit.framework.*;

/**
 * Test class for the PadbergRaoOddMinimumCutset implementation
 *
 * @author Joris Kinable
 */
public class PadbergRaoOddMinimumCutsetTest
    extends TestCase
{

    private void runTest(
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network, Set<Integer> oddVertices,
        boolean useTreeCompression)
    {
        PadbergRaoOddMinimumCutset<Integer, DefaultWeightedEdge> padbergRaoOddMinimumCutset =
            new PadbergRaoOddMinimumCutset<>(network);
        double cutValue =
            padbergRaoOddMinimumCutset.calculateMinCut(oddVertices, useTreeCompression);
        Set<Integer> sourcePartition = padbergRaoOddMinimumCutset.getSourcePartition();
        Set<Integer> sinkPartition = padbergRaoOddMinimumCutset.getSinkPartition();
        Set<DefaultWeightedEdge> cutEdges = padbergRaoOddMinimumCutset.getCutEdges();

        Set<Integer> intersection = new HashSet<>(sourcePartition);
        intersection.retainAll(sinkPartition);
        assertTrue(intersection.isEmpty());
        Set<Integer> union = new HashSet<>(sourcePartition);
        union.addAll(sinkPartition);
        assertEquals(network.vertexSet(), union);

        assertTrue(PadbergRaoOddMinimumCutset.isOddVertexSet(sourcePartition, oddVertices));
        assertTrue(PadbergRaoOddMinimumCutset.isOddVertexSet(sinkPartition, oddVertices));

        Set<DefaultWeightedEdge> expectedCutEdges = network
            .edgeSet().stream()
            .filter(
                e -> sourcePartition.contains(network.getEdgeSource(e))
                    ^ sourcePartition.contains(network.getEdgeTarget(e)))
            .collect(Collectors.toSet());
        assertEquals(expectedCutEdges, cutEdges);
        double expectedWeight = cutEdges.stream().mapToDouble(network::getEdgeWeight).sum();
        assertEquals(expectedWeight, cutValue);

        // Verify whether the returned odd cut-set is indeed of minimum weight. To verify this, we
        // exhaustively iterate over all possible cutsets.
        GusfieldGomoryHuCutTree<Integer, DefaultWeightedEdge> gusfieldGomoryHuCutTreeAlgorithm =
            new GusfieldGomoryHuCutTree<>(network);
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> gomoryHuCutTree =
            gusfieldGomoryHuCutTreeAlgorithm.getGomoryHuTree();
        Set<DefaultWeightedEdge> edges = new LinkedHashSet<>(gomoryHuCutTree.edgeSet());
        boolean foundBest = false; // Just to make sure that our brute-force approach is exhaustive
        for (DefaultWeightedEdge edge : edges) {
            Integer source = gomoryHuCutTree.getEdgeSource(edge);
            Integer target = gomoryHuCutTree.getEdgeTarget(edge);
            double edgeWeight = gomoryHuCutTree.getEdgeWeight(edge);
            gomoryHuCutTree.removeEdge(edge); // Temporarily remove edge
            Set<Integer> partition =
                new ConnectivityInspector<>(gomoryHuCutTree).connectedSetOf(source);
            if (PadbergRaoOddMinimumCutset.isOddVertexSet(partition, oddVertices)) { // If the
                                                                                     // source
                // partition forms an
                // odd cutset, check
                // whether the cut
                // isn't better than
                // the one we already
                // found.
                assertTrue(cutValue <= edgeWeight);
                foundBest |= cutValue == edgeWeight;
            }
            gomoryHuCutTree.addEdge(source, target, edge); // Place edge back
        }
        assertTrue(foundBest);
    }

    public void testIsOddSetMethod()
    {
        Set<Integer> vertices = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6));
        Set<Integer> oddVertices1 = new HashSet<>(Arrays.asList(1, 2, 3, 7));
        Set<Integer> oddVertices2 = new HashSet<>(Arrays.asList(1, 2, 3, 4));
        assertTrue(PadbergRaoOddMinimumCutset.isOddVertexSet(vertices, oddVertices1));
        assertFalse(PadbergRaoOddMinimumCutset.isOddVertexSet(vertices, oddVertices2));
    }

    /**
     * Test the example graph from the paper Odd Minimum Cut-Sets and b-Matchings by Padberg and Rao
     */
    public void testExampleGraph()
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(network, Arrays.asList(1, 2, 3, 4, 5, 6));
        Graphs.addEdge(network, 1, 2, 10);
        Graphs.addEdge(network, 1, 6, 8);
        Graphs.addEdge(network, 2, 6, 3);
        Graphs.addEdge(network, 2, 3, 4);
        Graphs.addEdge(network, 2, 5, 2);
        Graphs.addEdge(network, 6, 3, 2);
        Graphs.addEdge(network, 6, 4, 2);
        Graphs.addEdge(network, 6, 5, 3);
        Graphs.addEdge(network, 5, 3, 4);
        Graphs.addEdge(network, 5, 4, 7);
        Graphs.addEdge(network, 3, 4, 5);

        Set<Integer> oddVertices = new HashSet<>(Arrays.asList(2, 3, 5, 6));
        this.runTest(network, oddVertices, true);
        this.runTest(network, oddVertices, false);

    }

    /**
     * Test disconnected graph
     */
    public void testDisconnectedGraph()
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addAllVertices(network, Arrays.asList(0, 1, 2, 3, 4));
        Graphs.addEdge(network, 0, 1, 3);
        Graphs.addEdge(network, 1, 2, 4);
        Graphs.addEdge(network, 0, 2, 7);
        Graphs.addEdge(network, 3, 4, 9);
        Set<Integer> oddVertices = new HashSet<>(Arrays.asList(0, 1, 2, 4));
        this.runTest(network, oddVertices, true);
        this.runTest(network, oddVertices, false);
    }

    /**
     * Another graph to test
     */
    public void testGraph()
    {
        SimpleWeightedGraph<Integer, DefaultWeightedEdge> network =
            new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        network.addVertex(7);
        network.addVertex(10);
        network.addVertex(12);
        network.addVertex(3);
        network.addVertex(1);
        network.addVertex(5);
        network.addVertex(6);
        Graphs.addEdge(network, 1, 12, 1.0);
        Graphs.addEdge(network, 3, 5, 1.0);
        Graphs.addEdge(network, 5, 6, 1.0);
        Graphs.addEdge(network, 6, 12, 4.0);

        Set<Integer> oddVertices = new LinkedHashSet<Integer>(Arrays.asList(7, 10, 12, 3));

        this.runTest(network, oddVertices, true);
        this.runTest(network, oddVertices, false);

    }

    /**
     * Test random graphs
     */
    public void testRandomGraphs()
    {
        Random rand = new Random(0);
        for (int i = 0; i < 8; i++) {
            SimpleWeightedGraph<Integer, DefaultWeightedEdge> randomGraph =
                new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
            int vertices = rand.nextInt((30 - 10) + 1) + 10; // 10-30 vertices
            double p = 0.01 * (rand.nextInt((85 - 50) + 1) + 50); // p=[0.5;0.85]
            GnpRandomGraphGenerator<Integer, DefaultWeightedEdge> graphGen =
                new GnpRandomGraphGenerator<>(vertices, p);
            graphGen.generateGraph(randomGraph, new IntegerVertexFactory(0), null);
            for (DefaultWeightedEdge edge : randomGraph.edgeSet())
                randomGraph.setEdgeWeight(edge, rand.nextInt(150));

            for (int j = 0; j < 8; j++) {
                // Select a random subset of vertices of even cardinality. These will be the 'odd'
                // vertices.
                int max = vertices - 1;
                int min = 2;
                if (max % 2 == 1)
                    --max;
                int nrOfOddVertices = min + 2 * (int) (rand.nextDouble() * ((max - min) / 2 + 1)); // even
                // number
                // between
                // 2
                // and
                // |V|-1

                Set<Integer> oddVertices = new LinkedHashSet<>(nrOfOddVertices);
                List<Integer> allVertices = new ArrayList<>(randomGraph.vertexSet());
                for (int k = 0; k < nrOfOddVertices; k++) {
                    oddVertices.add(allVertices.remove(rand.nextInt(allVertices.size())));
                }
                this.runTest(randomGraph, oddVertices, true);
                this.runTest(randomGraph, oddVertices, false);
            }
        }
    }

}
