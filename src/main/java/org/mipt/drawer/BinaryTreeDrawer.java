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

import static org.mipt.drawer.Util.listFromIterable;

public class BinaryTreeDrawer {
    private static final double WIDTH = 20.0;
    private static final double HEIGHT = 20.0;

    private static int offsetByX = 0;
    private final Vertex rootVertex;

    public BinaryTreeDrawer(Vertex rootVertex) {
        this.rootVertex = rootVertex;
    }

    private static Object fillTreeFromVertex(mxGraph tree, Vertex vertex, int offsetByY) {
        List<Edge> edges = listFromIterable(vertex.getEdges(Direction.OUT));
        if (edges.size() > 0) {
            Object leftSubTreeHead = fillTreeFromVertex(tree, edges.get(0).getVertex(Direction.IN), offsetByY + 1);
            Object currentTreeHead = insertAndGetCurrentVertexToTree(tree, vertex, offsetByY);
            tree.insertEdge(tree.getDefaultParent(), null, null, currentTreeHead, leftSubTreeHead);
            if (edges.size() > 1) {
                Object rightSubTreeHead = fillTreeFromVertex(tree, edges.get(1).getVertex(Direction.IN), offsetByY + 1);
                tree.insertEdge(tree.getDefaultParent(), null, null, currentTreeHead, rightSubTreeHead);
            }
            return currentTreeHead;
        } else {
            return insertAndGetCurrentVertexToTree(tree, vertex, offsetByY);
        }
    }

    private static Object insertAndGetCurrentVertexToTree(mxGraph tree, Vertex vertex, int offsetByY) {
        return tree.insertVertex(tree.getDefaultParent(),
                null, vertex.getId(), WIDTH * (offsetByX++), 3 * HEIGHT * (offsetByY + 1),
                WIDTH, HEIGHT, mxConstants.STYLE_IMAGE);
    }

    public void drawTreeToFile(String fileName) {
        mxGraph tree = new mxGraph();

        fillTreeFromVertex(tree, rootVertex, 1);

        BufferedImage image = mxCellRenderer.createBufferedImage(tree, null, 1, Color.WHITE, true, null);

        try {
            ImageIO.write(image, "png", new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
