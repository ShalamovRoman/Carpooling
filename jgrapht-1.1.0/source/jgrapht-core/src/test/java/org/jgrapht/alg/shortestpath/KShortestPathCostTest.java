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
package org.jgrapht.alg.shortestpath;

import java.io.*;
import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.*;
import org.jgrapht.graph.*;

import junit.framework.*;

/**
 * @since July 5, 2007
 */
public class KShortestPathCostTest
    extends TestCase
{
    // ~ Methods ----------------------------------------------------------------

    public void testKShortestPathCompleteGraph4()
    {
        int nbPaths = 5;

        KShortestPathCompleteGraph4 graph = new KShortestPathCompleteGraph4();

        KShortestPaths<String, DefaultWeightedEdge> pathFinder =
            new KShortestPaths<>(graph, nbPaths);
        List<GraphPath<String, DefaultWeightedEdge>> pathElements = pathFinder.getPaths("vS", "v3");

        assertEquals(
            "[[(vS : v1), (v1 : v3)], [(vS : v2), (v2 : v3)],"
                + " [(vS : v2), (v1 : v2), (v1 : v3)], " + "[(vS : v1), (v1 : v2), (v2 : v3)], "
                + "[(vS : v3)]]",
            pathElements.toString());

        assertEquals(5, pathElements.size(), 0);
        GraphPath<String, DefaultWeightedEdge> pathElement = pathElements.get(0);
        assertEquals(2, pathElement.getWeight(), 0);

        assertEquals(
            Arrays.asList(new Object[] { graph.eS1, graph.e13 }), pathElement.getEdgeList());
    }

    public void testPicture1Graph()
    {
        Picture1Graph picture1Graph = new Picture1Graph();

        int maxSize = 10;

        KShortestPaths<String, DefaultWeightedEdge> pathFinder =
            new KShortestPaths<>(picture1Graph, maxSize);

        // assertEquals(2, pathFinder.getPaths("v5").size());

        List<GraphPath<String, DefaultWeightedEdge>> pathElements = pathFinder.getPaths("vS", "v5");
        GraphPath<String, DefaultWeightedEdge> pathElement = pathElements.get(0);
        assertEquals(
            Arrays.asList(new Object[] { picture1Graph.eS1, picture1Graph.e15 }),
            pathElement.getEdgeList());

        List<String> vertices = pathElement.getVertexList();
        assertEquals(Arrays.asList(new Object[] { "vS", "v1", "v5" }), vertices);

        pathElement = pathElements.get(1);
        assertEquals(
            Arrays.asList(new Object[] { picture1Graph.eS2, picture1Graph.e25 }),
            pathElement.getEdgeList());

        vertices = pathElement.getVertexList();
        assertEquals(Arrays.asList(new Object[] { "vS", "v2", "v5" }), vertices);

        pathElements = pathFinder.getPaths("vS", "v7");
        pathElement = pathElements.get(0);
        double lastCost = pathElement.getWeight();
        for (int i = 0; i < pathElements.size(); i++) {
            pathElement = pathElements.get(i);
            double cost = pathElement.getWeight();

            assertTrue(lastCost <= cost);
            lastCost = cost;
        }
    }

    public void testShortestPathsInIncreasingOrder()
    {
        BiconnectedGraph biconnectedGraph = new BiconnectedGraph();
        verifyShortestPathsInIncreasingOrderOfWeight(biconnectedGraph);

        KShortestPathCompleteGraph4 kSPCompleteGraph4 = new KShortestPathCompleteGraph4();
        verifyShortestPathsInIncreasingOrderOfWeight(kSPCompleteGraph4);

        KShortestPathCompleteGraph5 kSPCompleteGraph5 = new KShortestPathCompleteGraph5();
        verifyShortestPathsInIncreasingOrderOfWeight(kSPCompleteGraph5);

        KShortestPathCompleteGraph6 kSPCompleteGraph6 = new KShortestPathCompleteGraph6();
        verifyShortestPathsInIncreasingOrderOfWeight(kSPCompleteGraph6);

        KSPExampleGraph kSPExampleGraph = new KSPExampleGraph();
        verifyShortestPathsInIncreasingOrderOfWeight(kSPExampleGraph);

        NotBiconnectedGraph notBiconnectedGraph = new NotBiconnectedGraph();
        verifyShortestPathsInIncreasingOrderOfWeight(notBiconnectedGraph);

        Picture1Graph picture1Graph = new Picture1Graph();
        verifyShortestPathsInIncreasingOrderOfWeight(picture1Graph);
    }

    public void testShortestPathsWeightsWithMaxSizeIncreases()
    {
        BiconnectedGraph biconnectedGraph = new BiconnectedGraph();
        verifyShortestPathsWeightsWithMaxSizeIncreases(biconnectedGraph);

        KShortestPathCompleteGraph4 kSPCompleteGraph4 = new KShortestPathCompleteGraph4();
        verifyShortestPathsWeightsWithMaxSizeIncreases(kSPCompleteGraph4);

        KShortestPathCompleteGraph5 kSPCompleteGraph5 = new KShortestPathCompleteGraph5();
        verifyShortestPathsWeightsWithMaxSizeIncreases(kSPCompleteGraph5);

        KShortestPathCompleteGraph6 kSPCompleteGraph6 = new KShortestPathCompleteGraph6();
        verifyShortestPathsWeightsWithMaxSizeIncreases(kSPCompleteGraph6);

        KSPExampleGraph kSPExampleGraph = new KSPExampleGraph();
        verifyShortestPathsWeightsWithMaxSizeIncreases(kSPExampleGraph);

        NotBiconnectedGraph notBiconnectedGraph = new NotBiconnectedGraph();
        verifyShortestPathsWeightsWithMaxSizeIncreases(notBiconnectedGraph);

        Picture1Graph picture1Graph = new Picture1Graph();
        verifyShortestPathsWeightsWithMaxSizeIncreases(picture1Graph);
    }

    private <E> void verifyShortestPathsInIncreasingOrderOfWeight(Graph<String, E> graph)
    {
        int maxSize = 20;

        for (String sourceVertex : graph.vertexSet()) {
            for (String targetVertex : graph.vertexSet()) {
                if (targetVertex != sourceVertex) {
                    KShortestPaths<String, E> pathFinder = new KShortestPaths<>(graph, maxSize);

                    List<GraphPath<String, E>> pathElements =
                        pathFinder.getPaths(sourceVertex, targetVertex);
                    if (pathElements.isEmpty()) {
                        // no path exists between the start vertex and the end
                        // vertex
                        continue;
                    }
                    GraphPath<String, E> pathElement = pathElements.get(0);
                    double lastWeight = pathElement.getWeight();
                    for (int i = 0; i < pathElements.size(); i++) {
                        pathElement = pathElements.get(i);
                        double weight = pathElement.getWeight();
                        assertTrue(lastWeight <= weight);
                        lastWeight = weight;
                    }
                    assertTrue(pathElements.size() <= maxSize);
                }
            }
        }
    }

    private <E> void verifyShortestPathsWeightsWithMaxSizeIncreases(Graph<String, E> graph)
    {
        int maxSizeLimit = 10;

        for (String sourceVertex : graph.vertexSet()) {
            for (String targetVertex : graph.vertexSet()) {
                if (targetVertex != sourceVertex) {
                    KShortestPaths<String, E> pathFinder = new KShortestPaths<>(graph, 1);
                    List<GraphPath<String, E>> prevPathElementsResults =
                        pathFinder.getPaths(sourceVertex, targetVertex);

                    if (prevPathElementsResults.isEmpty()) {
                        // no path exists between the start vertex and the
                        // end vertex
                        continue;
                    }

                    for (int maxSize = 2; maxSize < maxSizeLimit; maxSize++) {
                        pathFinder = new KShortestPaths<>(graph, maxSize);
                        List<GraphPath<String, E>> pathElementsResults =
                            pathFinder.getPaths(sourceVertex, targetVertex);

                        verifyWeightsConsistency(prevPathElementsResults, pathElementsResults);
                    }
                }
            }
        }
    }

    /**
     * Verify weights consistency between the results when the max-size argument increases.
     *
     * @param prevPathElementsResults results obtained with a max-size argument equal to
     *        <code>k</code>
     * @param pathElementsResults results obtained with a max-size argument equal to
     *        <code>k+1</code>
     */
    private <E> void verifyWeightsConsistency(
        List<GraphPath<String, E>> prevPathElementsResults,
        List<GraphPath<String, E>> pathElementsResults)
    {
        for (int i = 0; i < prevPathElementsResults.size(); i++) {
            GraphPath<String, E> pathElementResult = pathElementsResults.get(i);
            GraphPath<String, E> prevPathElementResult = prevPathElementsResults.get(i);
            assertTrue(pathElementResult.getWeight() == prevPathElementResult.getWeight());
        }
    }

    /**
     * Currently disabled since it takes more than a few seconds to run. Also need to actually check
     * the output instead of printing.
     *
     * @see <a href=
     *      "http://jgrapht-users.107614.n3.nabble.com/quot-graph-must-contain-the-start-vertex-quot-when-running-KShortestPaths-td4024797.html">bug
     *      description</a>.
     */
    public void _testIllegalArgumentExceptionGraphNotThrown()
        throws Exception
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        InputStream fstream = getClass().getClassLoader().getResourceAsStream("edges.txt");
        BufferedReader in = new BufferedReader(new InputStreamReader(fstream));

        String[] edgeText;
        DefaultWeightedEdge ed;
        String line = in.readLine();
        while (line != null) {
            edgeText = line.split("\t");

            graph.addVertex(edgeText[0]);
            graph.addVertex(edgeText[1]);
            ed = graph.addEdge(edgeText[0], edgeText[1]);
            graph.setEdgeWeight(ed, Double.parseDouble(edgeText[2]));

            line = in.readLine();
        }

        // Close the input stream
        in.close();

        DefaultWeightedEdge src = graph.getEdge("M013", "M014");

        KShortestPaths<String, DefaultWeightedEdge> kPaths = new KShortestPaths<>(graph, 5);
        List<GraphPath<String, DefaultWeightedEdge>> paths;

        try {
            paths = kPaths.getPaths(graph.getEdgeSource(src), graph.getEdgeTarget(src));
            for (GraphPath<String, DefaultWeightedEdge> path : paths) {
                for (DefaultWeightedEdge edge : path.getEdgeList()) {
                    System.out.print(
                        "<" + graph.getEdgeSource(edge) + "\t" + graph.getEdgeTarget(edge) + "\t"
                            + edge + ">\t");
                }
                System.out.println(": " + path.getWeight());
            }
        } catch (IllegalArgumentException e) {
            fail("IllegalArgumentException thrown");
        }
    }
}

// End KShortestPathCostTest.java
