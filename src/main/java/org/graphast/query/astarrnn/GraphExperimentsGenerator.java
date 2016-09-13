package org.graphast.query.astarrnn;

import org.graphast.importer.CostGenerator;
import org.graphast.importer.SyntheticPoisGenerator;
import org.graphast.model.Graph;
import org.graphast.model.GraphBoundsImpl;
import org.graphast.model.GraphImpl;

public class GraphExperimentsGenerator {

	/**
	 * Generate synthetic travel time and pois for existent graph. Run with
	 * parameters: -Xms2048m -Xmx2048m for generate Ceara network graph
	 * 
	 * @author Lívia
	 *
	 */
	
	public static void main(String[] args) {
		String pathFortaleza = "C:\\Users\\Lívia\\git\\graphast\\core\\src\\main\\resources\\view_exp_100k";
		//String pathCeara = "C:\\Users\\Lívia\\git\\graphast\\core\\src\\main\\resources\\ceara";
		generateAndSaveGraph(pathFortaleza, 0, 1);
	/*	generateAndSaveGraph(pathFortaleza, 200, 1);
		generateAndSaveGraph(pathFortaleza, 300, 1);
		generateAndSaveGraph(pathFortaleza, 400, 1);
		generateAndSaveGraph(pathFortaleza, 500, 1);*/
		
	}
	
	public static void generateAndSaveGraph(String path, double numberOfPois, int category ){
		Graph graph = read(path);
		generateSyntethicFunction(graph);
	/*	generateUniformPois(numberOfPois, graph, category);*/
		graph.setDirectory(graph.getAbsoluteDirectory() + (int)numberOfPois + "Pois");
		graph.save();
	}

	public static Graph read(String path) {
		GraphImpl graph = new GraphImpl(path);
		graph.load();
		return graph;
	}

	public static void generateSyntethicFunction(Graph graph) {
		CostGenerator.generateAllSyntheticEdgesCosts(graph);
	}

	public static void generateUniformPois(double numberOfPois, Graph graph, int category) {
		double rate = numberOfPois / graph.getNumberOfNodes();
		SyntheticPoisGenerator.generateRandomSyntheticPois(graph, rate, category);
	}

	public static GraphBoundsImpl generateGraphBounds(String path) {
		GraphBoundsImpl gbounds = new GraphBoundsImpl(path);
		gbounds.createBounds();
		gbounds.loadFromGraph();
		return gbounds;

	}
}
