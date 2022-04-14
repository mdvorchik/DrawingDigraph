package org.mipt.drawer;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;

import java.io.IOException;
import java.io.InputStream;

public class GraphMLParser {
    private final Graph graph;

    public GraphMLParser(InputStream inputStream) {
        this.graph = new TinkerGraph();
        GraphMLReader reader = new GraphMLReader(graph);

        try {
            reader.inputGraph(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Vertex getRootVertex() {

        for (Vertex vertex : graph.getVertices()) {
            if (!vertex.getEdges(Direction.IN).iterator().hasNext()) {
                return vertex;
            }
        }

        return null;
    }
}
