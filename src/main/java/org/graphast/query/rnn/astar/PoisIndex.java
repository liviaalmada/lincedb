package org.graphast.query.astarrnn;

import java.util.List;
import java.util.Set;

import org.graphast.geometry.Point;
import org.graphast.query.knn.NearestNeighbor;

public interface PoisIndex {
	List<NearestNeighbor> knnSearch(int k, int maxDist, long queryId);
	//List<NearestNeighbor> rangeSearch(double range, long queryId);
	void indexingPois(List<Point> pois);
	void indexingNodesGraph();
	Long nearestNode(Point point);
	
}
