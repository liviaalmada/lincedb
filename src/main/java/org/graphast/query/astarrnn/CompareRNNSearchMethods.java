package org.graphast.query.astarrnn;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import org.graphast.exception.PathNotFoundException;
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

public class CompareRNNSearchMethods {
	private static final String PATH_GRAPH = "C:\\Users\\Lívia\\git\\graphast\\core\\src\\main\\resources\\";

	protected static final Logger LOGGER = Logger.getGlobal();
	private static org.slf4j.Logger log = LoggerFactory.getLogger(CompareRNNSearchMethods.class);

	public static void main(String[] args) throws IOException {

		// runAnalysis("view_exp_1k", 10);
		// runAnalysis("view_exp_10k", Integer.parseInt(args[0]));
		// runAnalysis("view_exp_50k", Integer.parseInt(args[0]));
		runAnalysis("view_exp_100k100Pois", 100);
		runAnalysis("view_exp_100k200Pois", 100);
		runAnalysis("view_exp_100k300Pois", 100);
		runAnalysis("view_exp_100k400Pois", 100);
		runAnalysis("view_exp_100k500Pois", 100);
	}

	public static void runAnalysis(String tableName, int testTimes) throws IOException {

		GraphImpl graph = new GraphImpl(PATH_GRAPH + tableName);
		graph.load();
		// GraphImpl graph = new GraphBoundsImpl(PATH_GRAPH + tableName);
		GraphImpl gbounds = new GraphBoundsImpl(PATH_GRAPH + tableName);
		((GraphBoundsImpl) gbounds).loadFromGraph();

		GraphImpl gboundsReverse = new GraphBoundsImpl(PATH_GRAPH + tableName);
		((GraphBoundsImpl) gboundsReverse).loadFromGraph();

		IRNNTimeDependent rnnBTFS = new RNNBacktrackingSearch((GraphBoundsImpl) gbounds);
		IRNNTimeDependent rnnBFS = new RNNBreadthFirstSearch((GraphBoundsImpl) gboundsReverse);
		/*IRNNTimeDependent rnnAstarRTree25 = new RNNAstarSearch(graph, new RTreePoisIndexImpl(), 25);
		IRNNTimeDependent rnnAstarRTree50 = new RNNAstarSearch(graph, new RTreePoisIndexImpl(), 50);
		IRNNTimeDependent rnnAstarRTree75 = new RNNAstarSearch(graph, new RTreePoisIndexImpl(), 75);
		IRNNTimeDependent rnnAstarRTree100 = new RNNAstarSearch(graph, new RTreePoisIndexImpl(), 100);*/
		/*IRNNTimeDependent rnnAstarVpTree25 = new RNNAstarSearch(graph, new VPTreePoisIndex(), 25);
		IRNNTimeDependent rnnAstarVpTree50 = new RNNAstarSearch(graph, new VPTreePoisIndex(), 50);
		IRNNTimeDependent rnnAstarVpTree75 = new RNNAstarSearch(graph, new VPTreePoisIndex(), 75);
		IRNNTimeDependent rnnAstarVpTree100 = new RNNAstarSearch(graph, new VPTreePoisIndex(), 100);

		Date timestamp = getRandomTimeStamp();
		Date timeout = DateUtils.parseDate(23, 55, 00);

		FileWriter rnnBacktrackingFileCsv = new FileWriter(tableName + "_rnn_baseline.csv");
		FileWriter rnnBFSFileCsv = new FileWriter(tableName + "_rnn_bfs.csv");
		FileWriter rnnAstarFileCsvRTree25 = new FileWriter(tableName + "_rnn_astar25RTree.csv");
		FileWriter rnnAstarFileCsvRTree50 = new FileWriter(tableName + "_rnn_astar50RTree.csv");
		FileWriter rnnAstarFileCsvRTree75 = new FileWriter(tableName + "_rnn_astar75RTree.csv");
		FileWriter rnnAstarFileCsvRTree100 = new FileWriter(tableName + "_rnn_astar100RTree.csv");
		FileWriter rnnAstarFileCsvVpTree25 = new FileWriter(tableName + "_rnn_astar25VpTree.csv");
		FileWriter rnnAstarFileCsvVpTree50 = new FileWriter(tableName + "_rnn_astar50VpTree.csv");
		FileWriter rnnAstarFileCsvVpTree75 = new FileWriter(tableName + "_rnn_astar75VpTree.csv");
		FileWriter rnnAstarFileCsvVpTree100 = new FileWriter(tableName + "_rnn_astar100VpTree.csv");
		ShortestPathService shortestPathService = new AStarLinearFunction(graph);

		writeHeader(rnnBacktrackingFileCsv);
		writeHeader(rnnBFSFileCsv);
		writeHeader(rnnAstarFileCsvRTree25);
		writeHeader(rnnAstarFileCsvRTree50);
		writeHeader(rnnAstarFileCsvRTree75);
		writeHeader(rnnAstarFileCsvRTree100);
		writeHeader(rnnAstarFileCsvVpTree25);
		writeHeader(rnnAstarFileCsvVpTree50);
		writeHeader(rnnAstarFileCsvVpTree75);
		writeHeader(rnnAstarFileCsvVpTree100);
	

		for (int i = 0; i < testTimes; i++) {
			Node query = getRandomCustomerInGraph((GraphBounds) gbounds);
			//Node query = gbounds.getNode(73290);
			//log.debug("rnnDFS for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnBTFS, query, timeout, timestamp, rnnBacktrackingFileCsv,
					shortestPathService);
			log.debug("rnnBFS for query " + query.getId());
			runSearchAndWrite((GraphBounds) gboundsReverse, rnnBFS, query, timeout, timestamp, rnnBFSFileCsv,
					shortestPathService);
	/*		log.debug("rnnAstarRTree25 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarRTree25, query, timeout, timestamp, rnnAstarFileCsvRTree25,
					shortestPathService);
			log.debug("rnnAstarRTree50 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarRTree50, query, timeout, timestamp, rnnAstarFileCsvRTree50,
					shortestPathService);
			log.debug("rnnAstarRTree75 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarRTree75, query, timeout, timestamp, rnnAstarFileCsvRTree75,
					shortestPathService);
			log.debug("rnnAstarRTree100 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarRTree100, query, timeout, timestamp,
					rnnAstarFileCsvRTree100, shortestPathService);
			log.debug("rnnAstarVpTree25 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarVpTree25, query, timeout, timestamp,
					rnnAstarFileCsvVpTree25, shortestPathService);
			log.debug("rnnAstarVpTree50 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarVpTree50, query, timeout, timestamp,
					rnnAstarFileCsvVpTree50, shortestPathService);
			log.debug("rnnAstarVpTree75 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarVpTree75, query, timeout, timestamp,
					rnnAstarFileCsvVpTree75, shortestPathService);
			log.debug("rnnAstarVpTree100 for query " + query.getId());
			runSearchAndWrite((GraphBounds) gbounds, rnnAstarVpTree100, query, timeout, timestamp,
					rnnAstarFileCsvVpTree100, shortestPathService);*/

//		}

//		rnnBacktrackingFileCsv.close();
//		rnnBFSFileCsv.close();
//		rnnAstarFileCsvRTree25.close();
//		rnnAstarFileCsvRTree50.close();
//		rnnAstarFileCsvRTree75.close();
//		rnnAstarFileCsvRTree100.close();
//		rnnAstarFileCsvVpTree25.close();
//		rnnAstarFileCsvVpTree50.close();
//		rnnAstarFileCsvVpTree75.close();
//		rnnAstarFileCsvVpTree100.close();
	}

	private static Date getRandomTimeStamp() {
		Date timestamp = DateUtils.parseDate((int) Math.floor(Math.random() * 24), (int) Math.floor(Math.random() * 60),
				(int) Math.floor(Math.random() * 60));
		return timestamp;
	}

	private static void writeHeader(FileWriter rnnAstarFileCsv) throws IOException {
		String header = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s", "coordinatesCustomer",
				"poiCoordinate", "time", "solutionId", "travelTime", "nodesSize", "path",
				"coordinateNodeVisited", "gidCustomer", "gidPoi", "gidVisited", "numberVisitedNodes",
				"shortestPathCost") + "\n";
		rnnAstarFileCsv.write(header);
	}

	private static void runSearchAndWrite(GraphBounds graph, IRNNTimeDependent rnn, Node customer, Date timeout,
			Date timestamp, FileWriter fileCsv, ShortestPathService shortestPathService) throws IOException {
		try {

			// List<Integer> listIdProcess = BenchmarkMemory.listIdProcess();
			// long numberUseMemoryInit = BenchmarkMemory.getUsedMemory();
			long startTime = System.nanoTime();

			NearestNeighbor solution = rnn.search(customer, timeout, timestamp);

			long endTime = System.nanoTime();

			long time = endTime - startTime;
			Path shortesPath = shortestPathService.shortestPath(solution.getId(), customer.getId(), timestamp);

			Long solutionId = null;
			Double travelTime = null;
			Integer nodesSize = null;
			ArrayList<Long> path = null;
			
			int numberVisitedNodes = 0;
			if (solution != null && solution.getPath() != null) {
				solutionId = solution.getId();
				travelTime = solution.getTravelTime();
				nodesSize = solution.getPath().size();
				path = solution.getPath();
				numberVisitedNodes = solution.getNumberVisitedNodes();

				String coordinatesCustomer = customer.getLongitude() + "," + customer.getLatitude();
				String gidCustomer = customer.getLabel();

				Node nodePoi = graph.getNode(solutionId);
				String poiCoordinate = nodePoi.getLongitude() + "," + nodePoi.getLatitude();
				String gidPoi = nodePoi.getLabel();

				String coordinateNodeVisited = "";
				String gidVisited = "";
				for (Long visited : path) {
					Node nodeVisited = graph.getNode(visited);
					coordinateNodeVisited = coordinateNodeVisited + "(" + nodeVisited.getLongitude() + ","
							+ nodeVisited.getLatitude() + ")";

					gidVisited = gidVisited + "-" + nodeVisited.getLabel();
				}

				String currentLine = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s", coordinatesCustomer,
						poiCoordinate, time, solutionId, travelTime, nodesSize, path, coordinateNodeVisited,
						gidCustomer, gidPoi, gidVisited, numberVisitedNodes, shortesPath.getTotalCost()) + "\n";

				System.out.println(currentLine);
				fileCsv.write(currentLine);
			} else {
				System.err.println(String.format("Customer %s (%s, %s) has no POI in subgraph", customer.getId(),
						customer.getLatitude(), customer.getLongitude()));
				fileCsv.write(String.format("Customer %s (%s, %s) has no POI in subgraph ", customer.getId(),
						customer.getLatitude(), customer.getLongitude()));
			}
		} catch (PathNotFoundException e) {
			System.err.println(String.format("Customer %s (%s, %s) has no POI in subgraph", customer.getId(),
					customer.getLatitude(), customer.getLongitude()));
			fileCsv.write(String.format("Customer %s (%s, %s) has no POI in subgraph.\n", customer.getId(),
					customer.getLatitude(), customer.getLongitude()));
		}
	}

	private static Node getRandomCustomerInGraph(Graph graph) {
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

}
