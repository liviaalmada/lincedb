package org.graphast.util.amc;

import org.graphast.importer.CostGenerator;
import org.graphast.importer.SyntheticPoisGenerator;
import org.graphast.model.Edge;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;

/**
 * A main class to read a graph, generate synthetic travel time and pois.
 * Tun with parameters: -Xms2048m  -Xmx2048m
 * @author Lívia
 *
 */

public class GraphReader {

	private static final String PATH_GRAPH = "C:\\Users\\Lívia\\git\\graphast\\core\\src\\main\\resources\\";

	public static void main(String[] args) {
		GraphImpl graph = new GraphImpl(PATH_GRAPH+"view_exp_100k");
		graph.load();
		
		
		for (int i = 0; i < graph.getNumberOfEdges(); i++) {
			Edge edge = graph.getEdge(i);
			long fromNodeId = edge.getFromNode();
			Node fromNode = graph.getNode(fromNodeId);
			fromNode.getLatitude();
			fromNode.getLongitude();
			edge.getToNode();
		}
		
		
		CostGenerator.generateAllSyntheticEdgesCosts(graph);
		SyntheticPoisGenerator.generateRandomSyntheticPois(graph, 0.1, 1);
		System.out.println(graph.getPOIs().size());
		graph.setDirectory(PATH_GRAPH+"fortaleza_100k"+"1pois");
		graph.save();
		
	}
}
