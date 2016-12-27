package org.graphast.query.astarrnn;

import java.util.ArrayList;
import java.util.Date;

import org.graphast.exception.PathNotFoundException;
import org.graphast.model.Graph;
import org.graphast.model.Node;
import org.graphast.query.knn.NearestNeighbor;
import org.graphast.query.rnn.IRNNTimeDependent;
import org.graphast.query.route.shortestpath.astar.AStarLinearFunction;
import org.graphast.query.route.shortestpath.model.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RNNBacktrackingAStar implements IRNNTimeDependent {

	private static Logger log = LoggerFactory.getLogger(RNNBacktrackingAStar.class);
	private Graph graph;
	
	public RNNBacktrackingAStar(Graph graph) {
		this.graph = graph;
	}

	@Override
	public NearestNeighbor search(Node query, Date timeout, Date timestamp) throws PathNotFoundException {
		AStarLinearFunction astar = new AStarLinearFunction(graph);
		double minCost = Double.MAX_VALUE;
		Path minPath = null;
		long currentPoi = -1;
		for (Long poi : graph.getPoiIds()) {
			try {
				// log.debug("Test " + candidate.getId());
				Path shortestPath = astar.shortestPath(graph.getNode(poi), query, timestamp);
				if (shortestPath.getTotalCost() < minCost) {
					minCost = shortestPath.getTotalCost();
					minPath = shortestPath;
					currentPoi = poi;
					/*nn.setId(poi);
					nn.setNumberVisitedNodes(shortestPath.getNumberVisitedNodes());
					nn.setTravelTime(shortestPath.getTotalCost());
					nn.setDistance((int) shortestPath.getTotalDistance());
					nn.setPath(new ArrayList<>(shortestPath.getEdges()));*/
					log.debug("Shortest path cost " + shortestPath.getTotalCost());
				}
			} catch (Exception e) {

			}
		}
		if (currentPoi > -1) {
			NearestNeighbor nn  =  new NearestNeighbor();
			nn.setId(currentPoi);
			nn.setNumberVisitedNodes(minPath.getNumberVisitedNodes());
			nn.setTravelTime(minPath.getTotalCost());
			nn.setDistance((int) minPath.getTotalDistance());
			nn.setPath(new ArrayList<>(minPath.getEdges()));
			//log.debug("Shortest path cost " + shortestPath.getTotalCost());*/
			return nn;
		}

		throw new PathNotFoundException(
				"target not found for root and set timestamp");

	}

}
