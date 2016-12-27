package org.graphast.query.astarrnn;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.graphast.exception.PathNotFoundException;
import org.graphast.geometry.Point;
import org.graphast.model.Graph;
import org.graphast.model.GraphBounds;
import org.graphast.model.GraphBoundsImpl;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;
import org.graphast.query.knn.NearestNeighbor;
import org.graphast.query.rnn.IRNNTimeDependent;
import org.graphast.query.rnn.RNNBacktrackingSearch;
import org.graphast.query.rnn.RNNBreadthFirstSearch;
import org.graphast.query.route.shortestpath.ShortestPathService;
import org.graphast.query.route.shortestpath.astar.AStarLinearFunction;
import org.graphast.query.route.shortestpath.model.Path;
import org.graphast.util.DateUtils;
import org.graphast.util.NumberUtils;
import org.slf4j.LoggerFactory;

// TODO REFATORAR
public class CompareRNNSearchMethodsAndPoisMapping {
	private static String PATH_GRAPH = "src//main//resources//";

	protected static final Logger LOGGER = Logger.getGlobal();
	private static org.slf4j.Logger log = LoggerFactory.getLogger(CompareRNNSearchMethodsAndPoisMapping.class);

	private static List<Point> readRealPois;
	private static RTreePoisIndexImpl rtree;
	//private static long preprocessingTime;

	public static void main(String[] args) throws IOException {
		
		if(args[0]!=null) PATH_GRAPH = args[0];

		runAnalysis(PATH_GRAPH+"//"+"view_exp_100k0Pois", 0, "Sat Jul 23 2016 12:01:01 GMT-0300 (BRT)",
				"points_2016-07-23.12_01_01.txt", "100k12horas");
		
		runAnalysis(PATH_GRAPH+"//"+"view_exp_100k0Pois", 0, "Sat Jul 23 2016 07:01:01 GMT-0300 (BRT)",
				"points_2016-07-23.07_01_01.txt", "100k7horas");
		
		runAnalysis(PATH_GRAPH+"//"+"view_exp_100k0Pois", 0, "Sat Jul 23 2016 17:01:01 GMT-0300 (BRT)",
				"points_2016-07-23.17_01_01.txt", "100k17horas");
	}

	/**
	 * @param tableName
	 * @param testTimes
	 * @param time
	 * @param poisFile
	 * @throws IOException
	 */
	public static void runAnalysis(String tableName, int testTimes, String time, String poisFile, String prefixfile) throws IOException {

		GraphImpl graph = new GraphImpl(tableName);
		graph.load();
		GraphImpl gbounds = new GraphBoundsImpl(tableName);
		((GraphBoundsImpl) gbounds).loadFromGraph();
		

		GraphImpl gboundsReverse = new GraphBoundsImpl(tableName);
		((GraphBoundsImpl) gboundsReverse).loadFromGraph();
		rtree = new RTreePoisIndexImpl(gboundsReverse);

		Date timestamp = getRandomTimeStamp();
		Date timeout = DateUtils.parseDate(23, 00, 00);
		Date timePois = null;

		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'z '('Z')'", Locale.ENGLISH);
		try {
			timePois = sdf.parse(time);
		} catch (Exception e) {
			e.printStackTrace();
		}

		readRealPois = GraphRealExperimentsGenerator.readRealPois(poisFile, timePois);
		System.out.println(readRealPois.size());

		IRNNTimeDependent rnnBTFS = new RNNBacktrackingSearch((GraphBoundsImpl) gbounds);
		IRNNTimeDependent rnnBFS = new RNNBreadthFirstSearch((GraphBoundsImpl) gboundsReverse);
		IRNNTimeDependent rnnAstarRTree1 = new RNNAstarSearch(gbounds, new RTreePoisIndexImpl(gbounds), 10);
		IRNNTimeDependent rnnAstarRTree25 = new RNNAstarSearch(gbounds, new RTreePoisIndexImpl(gbounds), 20);
		IRNNTimeDependent rnnAstarRTree50 = new RNNAstarSearch(gbounds, new RTreePoisIndexImpl(graph), 30);
		IRNNTimeDependent rnnAstarRTree75 = new RNNAstarSearch(graph, new RTreePoisIndexImpl(graph), 40);
		IRNNTimeDependent rnnAstarRTree100 = new RNNAstarSearch(graph, new RTreePoisIndexImpl(graph), 50);
		IRNNTimeDependent rnnAstarVpTree1 = new RNNAstarSearch(graph, new VPTreePoisIndex(graph), 10);
		IRNNTimeDependent rnnAstarVpTree25 = new RNNAstarSearch(graph, new VPTreePoisIndex(graph), 10);
		IRNNTimeDependent rnnAstarVpTree50 = new RNNAstarSearch(graph, new VPTreePoisIndex(graph), 30);
		IRNNTimeDependent rnnAstarVpTree75 = new RNNAstarSearch(graph, new VPTreePoisIndex(graph), 40);
		IRNNTimeDependent rnnAstarVpTree100 = new RNNAstarSearch(graph, new VPTreePoisIndex(graph), 50);

		FileWriter rnnBacktrackingFileCsv = new FileWriter(prefixfile + "_rnn_baseline.csv");
		FileWriter rnnBFSFileCsv = new FileWriter(prefixfile + "_rnn_bfs.csv");
		FileWriter rnnAstarFileCsvRTree1 = new FileWriter(prefixfile + "_rnn_astar10RTree.csv");
		FileWriter rnnAstarFileCsvRTree25 = new FileWriter(prefixfile + "_rnn_astar20RTree.csv");
		FileWriter rnnAstarFileCsvRTree50 = new FileWriter(prefixfile + "_rnn_astar30RTree.csv");
		FileWriter rnnAstarFileCsvRTree75 = new FileWriter(prefixfile + "_rnn_astar40RTree.csv");
		FileWriter rnnAstarFileCsvRTree100 = new FileWriter(prefixfile + "_rnn_astar50RTree.csv");
		FileWriter rnnAstarFileCsvVpTree1 = new FileWriter(prefixfile + "_rnn_astar10VpTree.csv");
		FileWriter rnnAstarFileCsvVpTree25 = new FileWriter(prefixfile + "_rnn_astar20VpTree.csv");
		FileWriter rnnAstarFileCsvVpTree50 = new FileWriter(prefixfile + "_rnn_astar30VpTree.csv");
		FileWriter rnnAstarFileCsvVpTree75 = new FileWriter(prefixfile + "_rnn_astar40VpTree.csv");
		FileWriter rnnAstarFileCsvVpTree100 = new FileWriter(prefixfile + "_rnn_astar50VpTree.csv");

		ShortestPathService shortestPathService = new AStarLinearFunction(graph);

		writeHeader(rnnBacktrackingFileCsv);
		writeHeader(rnnBFSFileCsv);
		writeHeader(rnnAstarFileCsvRTree1);
		writeHeader(rnnAstarFileCsvRTree25);
		writeHeader(rnnAstarFileCsvRTree50);
		writeHeader(rnnAstarFileCsvRTree75);
		writeHeader(rnnAstarFileCsvRTree100);
		writeHeader(rnnAstarFileCsvVpTree1);
		writeHeader(rnnAstarFileCsvVpTree25);
		writeHeader(rnnAstarFileCsvVpTree50);
		writeHeader(rnnAstarFileCsvVpTree75);
		writeHeader(rnnAstarFileCsvVpTree100);

		for (int i = 0; i < testTimes; i++) {
			Node query = getRandomQuery((GraphBounds) gbounds);

			log.debug("rnnDFS for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnBTFS, query, timeout, timestamp, rnnBacktrackingFileCsv,
					shortestPathService);
			log.debug("rnnBFS for query " + query.getId());
			runSearchAndWrite((GraphBounds) gboundsReverse, rnnBFS, query, timeout, timestamp, rnnBFSFileCsv,
					shortestPathService);
			log.debug("rnnAstarRTree10 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarRTree1, query, timeout, timestamp, rnnAstarFileCsvRTree1,
					shortestPathService);
			log.debug("rnnAstarRTree20 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarRTree25, query, timeout, timestamp, rnnAstarFileCsvRTree25,
					shortestPathService);
			log.debug("rnnAstarRTree30 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarRTree50, query, timeout, timestamp, rnnAstarFileCsvRTree50,
					shortestPathService);
			log.debug("rnnAstarRTree40 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarRTree75, query, timeout, timestamp, rnnAstarFileCsvRTree75,
					shortestPathService);
			log.debug("rnnAstarRTree50 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarRTree100, query, timeout, timestamp,
					rnnAstarFileCsvRTree100, shortestPathService);
			log.debug("rnnAstarVpTree10 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarVpTree1, query, timeout, timestamp, rnnAstarFileCsvVpTree1,
					shortestPathService);
			log.debug("rnnAstarVpTree20 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarVpTree25, query, timeout, timestamp,
					rnnAstarFileCsvVpTree25, shortestPathService);
			log.debug("rnnAstarVpTree30 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarVpTree50, query, timeout, timestamp,
					rnnAstarFileCsvVpTree50, shortestPathService);
			log.debug("rnnAstarVpTree40 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarVpTree75, query, timeout, timestamp,
					rnnAstarFileCsvVpTree75, shortestPathService);
			log.debug("rnnAstarVpTree50 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarVpTree100, query, timeout, timestamp,
					rnnAstarFileCsvVpTree100, shortestPathService);

		}

		rnnBacktrackingFileCsv.close();
		rnnBFSFileCsv.close();
		rnnAstarFileCsvRTree1.close();
		rnnAstarFileCsvRTree25.close();
		rnnAstarFileCsvRTree50.close();
		rnnAstarFileCsvRTree75.close();
		rnnAstarFileCsvRTree100.close();
		rnnAstarFileCsvVpTree1.close();
		rnnAstarFileCsvVpTree25.close();
		rnnAstarFileCsvVpTree50.close();
		rnnAstarFileCsvVpTree75.close();
		rnnAstarFileCsvVpTree100.close();

	}

	private static Date getRandomTimeStamp() {
		Date timestamp = DateUtils.parseDate((int) Math.floor(Math.random() * 24), (int) Math.floor(Math.random() * 60),
				(int) Math.floor(Math.random() * 60));
		return timestamp;
	}

	private static void writeHeader(FileWriter rnnAstarFileCsv) throws IOException {
		String header = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s", 
				"customerId", "preprocessingTime" , "executionTime", 
				"coordinatesCustomer", "solutionId", "poiCoordinate", "calculatedTravelTime",
				"shortesPath.getTotalCost()", "numberVisitedNodes", "pathSize", 
				"path", "shortestPathSize", "shortestPath") + "\n";
		rnnAstarFileCsv.write(header);
	}

	private static void runSearchAndWrite(GraphBounds graph, IRNNTimeDependent rnn, Node customer, Date timeout,
			Date timestamp, FileWriter fileCsv, ShortestPathService shortestPathService) throws IOException {
		try {
			long startTime = System.nanoTime();
			mapPoisToNodes(graph, rnn, readRealPois);
			long preprocessingTime = System.nanoTime() - startTime;
			startTime = System.nanoTime();
			NearestNeighbor solution = rnn.search(customer, timeout, timestamp);
			long endTime = System.nanoTime();
			long executionTime = endTime - startTime;
			Path shortesPath = shortestPathService.shortestPath(solution.getId(), customer.getId(), timestamp);

			Long solutionId = null;
			Double calculatedTravelTime = null;
			Integer pathSize = null;
			ArrayList<Long> path = null;

			int numberVisitedNodes = 0;
			if (solution != null && solution.getPath() != null) {
				solutionId = solution.getId();
				calculatedTravelTime = solution.getTravelTime();
				pathSize = solution.getPath().size();
				path = solution.getPath();
				numberVisitedNodes = solution.getNumberVisitedNodes();

				String coordinatesCustomer = customer.getLongitude() + "," + customer.getLatitude();
				Node nodePoi = graph.getNode(solutionId);
				String poiCoordinate = nodePoi.getLongitude() + "," + nodePoi.getLatitude();
	
				String currentLine = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s", 
						customer.getId(), preprocessingTime , executionTime, 
						coordinatesCustomer, solutionId, poiCoordinate,  calculatedTravelTime,
						shortesPath.getTotalCost(),  numberVisitedNodes, pathSize, path,
						shortesPath.getNumberVisitedNodes(), shortesPath.getPath()) + "\n";

				System.out.println(currentLine);
				fileCsv.write(currentLine);
			} else {
				System.err.println(String.format("Customer %s (%s, %s) has no POI in subgraph", customer.getId(),
						customer.getLatitude(), customer.getLongitude()));
			}
		} catch (PathNotFoundException e) {
			System.err.println(String.format("Customer %s (%s, %s) has no POI in subgraph", customer.getId(),
					customer.getLatitude(), customer.getLongitude()));
		}
	}

	private static void mapPoisToNodes(GraphBounds graph, IRNNTimeDependent rnn, List<Point> pois) {
		if (rnn instanceof RNNAstarSearch) {
			// Inclui os pois em uma estrutura de índice
			((RNNAstarSearch) rnn).indexPois(pois);

		} else {
			for(long id=0; id<graph.getNumberOfNodes();id++){
				graph.setNodeCategory(id, -1);
			}
			
			for (Point point : pois) {
				// Procura o nó mais próximo do poi e inclui o poi numa lista
				Long nearestNodeId = rtree.nearestNode(point);
				graph.setNodeCategory(nearestNodeId, 1);
			}
		}
	}

	private static Node getRandomQuery(Graph graph) {
		Node node;
		double[] bounds = new double[] { -3.710467, -38.591078, -3.802376, -38.465530 };
		do {
			long id = Double
					.valueOf(NumberUtils.generatePseudorandom(0, Long.valueOf(graph.getNumberOfNodes() - 1).intValue()))
					.longValue();
			node = graph.getNode(id);
		} while (node.getCategory() != -1 || node.getLatitude() > bounds[0] || node.getLatitude() < bounds[2]
				|| node.getLongitude() < bounds[1] || node.getLongitude() > bounds[3]);
		return node;
	}

	// TODO
	private static List<Point> getUniformPois(Graph graph) {
		return null;
	}

}
