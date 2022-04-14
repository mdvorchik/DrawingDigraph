package org.mipt.drawer;

import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DiGraphDrawer {
    private static final double WIDTH = 20.0;
    private static final double HEIGHT = 20.0;

    private static int offsetByX = 0;
    private final List<Vertex> rootVertexes;

    public DiGraphDrawer(List<Vertex> rootVertexes) {
        this.rootVertexes = rootVertexes;
    }

    private static Object fillTreeFromVertex(mxGraph tree, Vertex vertex, int offsetByY) {
        List<Edge> edges = Util.listFromIterable(vertex.getEdges(Direction.OUT));
        if (edges.size() > 0) {
            Object leftSubTreeHead = fillTreeFromVertex(tree, edges.get(0).getVertex(Direction.IN), offsetByY + 1);
            Object currentTreeHead = insertAndGetCurrentVertexToGraph(tree, vertex, offsetByY);
            tree.insertEdge(tree.getDefaultParent(), null, null, currentTreeHead, leftSubTreeHead);
            if (edges.size() > 1) {
                Object rightSubTreeHead = fillTreeFromVertex(tree, edges.get(1).getVertex(Direction.IN), offsetByY + 1);
                tree.insertEdge(tree.getDefaultParent(), null, null, currentTreeHead, rightSubTreeHead);
            }
            return currentTreeHead;
        } else {
            return insertAndGetCurrentVertexToGraph(tree, vertex, offsetByY);
        }
    }

    private static Object insertAndGetCurrentVertexToGraph(mxGraph graph, Vertex vertex, int offsetByY) {
        return graph.insertVertex(graph.getDefaultParent(),
                null, vertex.getId(), 2 * WIDTH * (++offsetByX), 3 * HEIGHT * (offsetByY + 1),
                WIDTH, HEIGHT, mxConstants.STYLE_IMAGE);
    }

    public void drawGraphToFile(String fileName, int w) {
        mxGraph graph = new mxGraph();
        if (w > 0) {
            fillGraphByCGLalg(graph, rootVertexes, w);
        }

        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, null);

        try {
            ImageIO.write(image, "png", new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillGraphByCGLalg(mxGraph graph, List<Vertex> vertexes, int w) {
        int label = 0;
        int layer = 1;
        Map<Vertex, Integer> vToLabel = new HashMap<>();
        Map<Vertex, Integer> vToLayer = new HashMap<>();
        Map<Vertex, Object> vToGraph = new HashMap<>();
        vToLabel.put(vertexes.get((int)(Math.random() * vertexes.size())), ++label);

        for (int i = 1; i < vertexes.size(); i++) {
            Vertex unlabeledV = getUnlabeledV(vertexes, vToLabel);
            vToLabel.put(unlabeledV, ++label);
        }

        List<Vertex> U = new ArrayList<>();

        Vertex firstToU = vToLabel.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .collect(Collectors.toList()).get(vToLabel.size() - 1).getKey();

        U.add(firstToU);
        vToLayer.put(firstToU, layer);

        Object head = insertAndGetCurrentVertexToGraph(graph, firstToU, layer);
        vToGraph.put(firstToU, head);
        for (int i = 1; i < vertexes.size(); i++) {
            Vertex vWithMaxNeighbourInU = getVWithMaxNeighbourInU(vertexes, U, vToLabel);
            U.add(vWithMaxNeighbourInU);
            int finalLayer = layer;
            long vInCurrentLayerCount = vToLayer.entrySet().stream()
                    .filter(e -> e.getValue() == finalLayer).count();
            if (vInCurrentLayerCount >= w || hasEdgesInCurrentLayer(vWithMaxNeighbourInU, vToLayer, layer)) {
                layer++;
                offsetByX = 0;
            }
            vToLayer.put(vWithMaxNeighbourInU, layer);
            Object next = insertAndGetCurrentVertexToGraph(graph, vWithMaxNeighbourInU, layer);
            vToGraph.put(vWithMaxNeighbourInU, next);
        }

        vToGraph.entrySet().stream().forEach(e -> {
            e.getKey().getVertices(Direction.OUT).forEach(vertex -> {
                graph.insertEdge(graph.getDefaultParent(), null, null, e.getValue(), vToGraph.get(vertex));
            });
        });
    }

    private boolean hasEdgesInCurrentLayer(Vertex vWithMaxNeighbourInU, Map<Vertex, Integer> vToLayer, int layer) {
        AtomicBoolean hasEdgesInCurrentLayer = new AtomicBoolean(false);
        List<Vertex> vertices = Util.listFromIterable(vWithMaxNeighbourInU.getVertices(Direction.BOTH));
        vToLayer.entrySet().stream()
                .filter(e -> e.getValue() == layer)
                .map(Map.Entry::getKey)
                .forEach(v -> {
                    if (vertices.contains(v)) {
                        hasEdgesInCurrentLayer.set(true);
                    }
                });
        return hasEdgesInCurrentLayer.get();
    }

    private Vertex getVWithMaxNeighbourInU(List<Vertex> vertexes, List<Vertex> u, Map<Vertex, Integer> vToLabel) {
        List<Vertex> UminusV = new ArrayList<>(vertexes);
        UminusV.removeAll(u);
        Map<Vertex, Integer> vToNumberNeighbourInU = new HashMap<>();
        for (Vertex candidate : UminusV) {
            candidate.getVertices(Direction.BOTH).forEach(v -> {
                if (u.contains(v)) {
                    vToNumberNeighbourInU.computeIfPresent(candidate, (e, val) -> val + 1);
                    vToNumberNeighbourInU.putIfAbsent(candidate, 1);
                }
            });
        }
        int maxNeighbourInUCount = vToNumberNeighbourInU.values().stream().max(Integer::compareTo).get();
        List<Vertex> vWithMaxLabeledList = vToLabel.entrySet().stream()
                .filter(e -> UminusV.contains(e.getKey()))
                .filter(e -> vToNumberNeighbourInU.containsKey(e.getKey()))
                .filter(e -> vToNumberNeighbourInU.get(e.getKey()) == maxNeighbourInUCount)
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return vWithMaxLabeledList.get(vWithMaxLabeledList.size() - 1);
    }

    private Vertex getUnlabeledV(List<Vertex> vertexes, Map<Vertex, Integer> vNameToLabel) {
        Map<Vertex, Integer> vToLabeledNeighbour = new HashMap<>();
        List<Vertex> candidates = vertexes.stream()
                .filter(v -> !vNameToLabel.containsKey(v)).collect(Collectors.toList());
        for (Vertex candidate : candidates) {
            candidate.getVertices(Direction.BOTH).forEach(v -> {
                if (vNameToLabel.containsKey(v)) {
                    vToLabeledNeighbour.computeIfPresent(candidate, (e, val) -> val + 1);
                    vToLabeledNeighbour.putIfAbsent(candidate, 0);
                }
            });
        }
        return vToLabeledNeighbour.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .findFirst().get().getKey();
    }
}
