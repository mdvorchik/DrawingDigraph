package org.mipt.drawer;

import com.tinkerpop.blueprints.Vertex;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        InputStream is = GraphMLParser.class.getClassLoader().getResourceAsStream("digraph.xml");
        int w = 2;
        if (args.length > 0) {
            try {
                is = new FileInputStream(args[0]);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (args.length > 1) {
            w = Integer.parseInt(args[1]);
        }
        GraphMLParser graphMLParser = new GraphMLParser(is);
        List<Vertex> vertexes = graphMLParser.getVertexes();
        DiGraphDrawer graphDrawer = new DiGraphDrawer(vertexes);

        graphDrawer.drawGraphToFile("tree.png", w);
        String s = args.length > 0 ? args[0] : "";
        System.out.println("Done! " + s);

        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ignored) {
            }

            ImageIcon icon = new ImageIcon("tree.png");
            JOptionPane.showMessageDialog(
                    null,
                    "Graph",
                    "graph", JOptionPane.INFORMATION_MESSAGE,
                    icon);
        });
    }
}
