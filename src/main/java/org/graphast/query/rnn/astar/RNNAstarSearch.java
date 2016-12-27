package org.graphast.query.astarrnn;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import org.graphast.exception.PathNotFoundException;
import org.graphast.geometry.Point;
import org.graphast.model.Graph;
import org.graphast.model.GraphBounds;
import org.graphast.model.GraphBoundsImpl;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;
import org.graphast.query.knn.NearestNeighbor;
import org.graphast.query.model.LowerBoundEntry;
import org.graphast.query.rnn.IRNNTimeDependent;
import org.graphast.query.rnn.RNNBacktrackingSearch;
import org.graphast.query.rnn.RNNBreadthFirstSearch;
import org.graphast.query.route.shortestpath.ShortestPathService;
import org.graphast.query.route.shortestpath.astar.AStarLinearFunction;
import org.graphast.query.route.shortestpath.dijkstra.DijkstraLinearFunction;
import org.graphast.query.route.shortestpath.model.RouteEntry;
import org.graphast.util.DateUtils;
import org.graphast.util.DistanceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RNNAstarSearch extends AStarLinearFunction implements IRNNTimeDependent {

	private static final String PATH_GRAPH = "C:\\Users\\Lívia\\git\\graphast\\core\\src\\main\\resources\\";
	private static Logger log = LoggerFactory.getLogger(RNNAstarSearch.class);
	private PoisIndex index;
	private int filterSize;
	private int maxDist = 100000;
	private static int numberOfVisitedVertex = 0;

	public RNNAstarSearch(Graph graph, PoisIndex index, int k) {
		super(graph);
		this.index = index;
		setFilterSize(k);
	}
	
	public void indexPois(List<Point> pois){
		index.indexingPois(pois);
	}

	@Override
	public NearestNeighbor search(Node root, Date timeout, Date timestamp) throws PathNotFoundException {
		try {
			ArrayList<NearestNeighbor> nns = search(root.getId(), timeout, timestamp, 1);
			if (!nns.isEmpty())
				return nns.get(0);
		} catch (PathNotFoundException e) {
			throw e;
		}
		throw new PathNotFoundException();

	}
	

	public ArrayList<NearestNeighbor> search(Long queryId, Date timeout, Date timestamp, int numberOfNN) throws PathNotFoundException {
		List<NearestNeighbor> nns = filterPois(queryId);
		HashMap<Long, Integer> wasTraversed = new HashMap<Long, Integer>();
		HashMap<Long, RouteEntry> parents = new HashMap<Long, RouteEntry>();
		PriorityQueue<LowerBoundEntry> queue = new PriorityQueue<LowerBoundEntry>();
		Node target = graph.getNode(queryId);
		ArrayList<NearestNeighbor> ret = new ArrayList<>();
		long maxTravelTimeMilliseconds = DateUtils.dateToMilli(timeout);
		int found = 0;
		
		for (NearestNeighbor candidate : nns) {
			long vid = candidate.getId();
			long arrivalTime = DateUtils.dateToMilli(timestamp);
			int travelTime = 0;
			int lowerBound = (int) (DistanceUtils.timeCost(graph.getNode(candidate.getId()), graph.getNode(queryId)));
			queue.offer(new LowerBoundEntry(vid, travelTime, (int) arrivalTime, -1, lowerBound));
			numberOfVisitedVertex++;
			wasTraversed.put(vid, 0);
		}
		
		while ((queue.peek()) != null) {
			LowerBoundEntry removed = queue.poll();
			if (removed.getId() == queryId) {
				
				if (removed.getTravelTime() > maxTravelTimeMilliseconds) {
					throw new PathNotFoundException(String.format("not found path for parameter time %s milliseconds.",
							maxTravelTimeMilliseconds));
				}
				List<RouteEntry> reconstructPath = super.reconstructPath(queryId, parents);
				ArrayList<Long> path = new ArrayList<>();
				for (RouteEntry routeEntry : reconstructPath) {
					path.add(graph.getEdge(routeEntry.getEdgeId()).getFromNode());
				}
				path.add(queryId);
				numberOfVisitedVertex++;
				double totalCostInMilissegundo = removed.getTravelTime();
				ret.add(new NearestNeighbor(path.get(0), totalCostInMilissegundo, path, numberOfVisitedVertex));

				if(found == numberOfNN)return ret;

			} else {
				expandVertex(target, removed, wasTraversed, queue, parents);
				numberOfVisitedVertex++;
			}

		}
		return ret;

	}

	private List<NearestNeighbor> filterPois(Long queryId) {
				List<NearestNeighbor> nns = index.knnSearch(filterSize, maxDist , queryId);
		return nns;
	}

	public static void main(String[] args) {
		String PATH_GRAPH = "C:\\Users\\Lívia\\git\\graphast\\core\\src\\main\\resources\\view_exp_100k100Pois";
		
		GraphImpl graphReverse, graph;
		RNNAstarSearch rnnAstar;
		RNNBacktrackingSearch rnnBack;
		RNNBreadthFirstSearch rnnBFS;
		
		//g = new GraphImpl(PATH_GRAPH + "view_exp_100k500Pois");
		graph = new GraphBoundsImpl(PATH_GRAPH);
		((GraphBoundsImpl) graph).loadFromGraph();
		
		graphReverse = new GraphBoundsImpl(PATH_GRAPH);
		((GraphBoundsImpl) graphReverse).loadFromGraph();
		
		RTreePoisIndexImpl rTreePoisIndexImpl = new RTreePoisIndexImpl(graph);

		
		rnnAstar = new RNNAstarSearch(graph, rTreePoisIndexImpl , 70);
		rnnBack = new RNNBacktrackingSearch((GraphBounds) graph);
		rnnBFS = new RNNBreadthFirstSearch((GraphBounds) graphReverse);
		ShortestPathService shortestPathService = new DijkstraLinearFunction(graph);
		//((GraphBoundsImpl) graphReverse).save();
		
		
		Date timeout = DateUtils.parseDate(23, 55, 00);
		Date timestamp = DateUtils.parseDate(15, 30, 00);
		Node query = graphReverse.getNode(73290);
		
		//Node query = graphReverse.getNode(38651); 
		//Node query = graphReverse.getNode(27597);
		ArrayList<Point> pois = new ArrayList<>();
		for (Long id : graph.getPoiIds()) {
			Node node = graph.getNode(id);
			pois.add(new Point(node.getLatitude(), node.getLongitude()));
		}
		rnnAstar.indexPois(pois);
		NearestNeighbor nnAstar = rnnAstar.search(query, timeout, timestamp);
		System.out.println(nnAstar);
		//System.out.println(shortestPathService.shortestPath(nnAstar.getId(), query.getId()).getTotalCost());
		
		NearestNeighbor nnBFS = rnnBFS.search(query, timeout, timestamp);
		System.out.println(nnBFS.getTravelTime());
		System.out.println(shortestPathService.shortestPath(nnBFS.getId(), query.getId()).getTotalCost());
		
		
/*		NearestNeighbor nnBack = rnnBack.search(query, timeout, timestamp);
		System.out.println(nnBack.getTravelTime());
		System.out.println(shortestPathService.shortestPath(nnBack.getId(), query.getId()).getTotalCost());*/

	}

	public int getFilterSize() {
		return filterSize;
	}

	public void setFilterSize(int filterSize) {
		this.filterSize = filterSize;
	}

}
