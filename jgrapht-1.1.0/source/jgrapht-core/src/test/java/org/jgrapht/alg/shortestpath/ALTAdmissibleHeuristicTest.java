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
package org.jgrapht.alg.shortestpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.function.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 * @author Dimitrios Michail
 */
public class ALTAdmissibleHeuristicTest
{

    @Test
    public void testRandom()
    {
        final int tests = 3;
        final int n = 30;
        final double p = 0.35;
        final int landmarksCount = 2;

        Random rng = new Random(47);

        List<Supplier<Graph<Integer, DefaultWeightedEdge>>> graphs = new ArrayList<>();
        graphs.add(() -> new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class));
        graphs.add(() -> new WeightedPseudograph<>(DefaultWeightedEdge.class));

        for (Supplier<Graph<Integer, DefaultWeightedEdge>> gSupplier : graphs) {
            GraphGenerator<Integer, DefaultWeightedEdge, Integer> gen =
                new GnpRandomGraphGenerator<>(n, p, rng, true);
            for (int i = 0; i < tests; i++) {
                Graph<Integer, DefaultWeightedEdge> g = gSupplier.get();
                gen.generateGraph(g, new IntegerVertexFactory(), null);

                // assign random weights
                for (DefaultWeightedEdge e : g.edgeSet()) {
                    g.setEdgeWeight(e, rng.nextDouble());
                }

                // pick random landmarks
                Integer[] allVertices = g.vertexSet().toArray(new Integer[0]);
                Set<Integer> landmarks = new HashSet<>();
                while (landmarks.size() < landmarksCount) {
                    landmarks.add(allVertices[rng.nextInt(n)]);
                }

                AStarAdmissibleHeuristic<Integer> h = new ALTAdmissibleHeuristic<>(g, landmarks);
                ShortestPathAlgorithm<Integer, DefaultWeightedEdge> sp1 =
                    new DijkstraShortestPath<>(g);
                ShortestPathAlgorithm<Integer, DefaultWeightedEdge> sp2 =
                    new AStarShortestPath<>(g, h);

                for (Integer v : g.vertexSet()) {
                    for (Integer u : g.vertexSet()) {
                        GraphPath<Integer, DefaultWeightedEdge> p1 = sp1.getPath(v, u);
                        GraphPath<Integer, DefaultWeightedEdge> p2 = sp2.getPath(v, u);
                        assertEquals(p1.getWeight(), p2.getWeight(), 1e-9);
                    }
                }

            }
        }

    }

    @Test
    public void testRandomAdmissible()
    {
        final int tests = 3;
        final int n = 35;
        final double p = 0.3;

        Random rng = new Random(33);

        List<Supplier<Graph<Integer, DefaultWeightedEdge>>> graphs = new ArrayList<>();
        graphs.add(() -> new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class));
        graphs.add(() -> new WeightedPseudograph<>(DefaultWeightedEdge.class));

        Comparator<Double> comparator = new ToleranceDoubleComparator();

        for (Supplier<Graph<Integer, DefaultWeightedEdge>> gSupplier : graphs) {
            GraphGenerator<Integer, DefaultWeightedEdge, Integer> gen =
                new GnpRandomGraphGenerator<>(n, p, rng, true);
            for (int i = 0; i < tests; i++) {
                Graph<Integer, DefaultWeightedEdge> g = gSupplier.get();
                gen.generateGraph(g, new IntegerVertexFactory(), null);

                // assign random weights
                for (DefaultWeightedEdge e : g.edgeSet()) {
                    g.setEdgeWeight(e, rng.nextDouble());
                }

                for (Integer l : g.vertexSet()) {
                    AStarAdmissibleHeuristic<Integer> h =
                        new ALTAdmissibleHeuristic<>(g, Collections.singleton(l));
                    for (Integer v : g.vertexSet()) {
                        ShortestPathAlgorithm<Integer, DefaultWeightedEdge> sp =
                            new DijkstraShortestPath<>(g);
                        SingleSourcePaths<Integer, DefaultWeightedEdge> paths = sp.getPaths(v);
                        for (Integer u : g.vertexSet()) {
                            GraphPath<Integer, DefaultWeightedEdge> path = paths.getPath(u);
                            // System.out.println(h.getCostEstimate(v, u) + " <= " +
                            // path.getWeight());
                            assertTrue(
                                comparator.compare(h.getCostEstimate(v, u), path.getWeight()) <= 0);
                        }
                    }
                }
            }
        }

    }

}
