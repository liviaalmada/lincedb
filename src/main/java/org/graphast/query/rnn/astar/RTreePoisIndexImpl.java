package org.graphast.query.astarrnn;

import java.util.ArrayList;
import java.util.List;

import org.graphast.model.Graph;
import org.graphast.model.Node;
import org.graphast.query.knn.NearestNeighbor;
import org.graphast.util.DistanceUtils;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

public class RTreePoisIndexImpl implements PoisIndex {
	
	public RTree<Long, Point> getRtreePois() {
		return rtreePois;
	}

	public void setRtreePois(RTree<Long, Point> rtreePois) {
		this.rtreePois = rtreePois;
	}

	public RTree<Long, Point> getRtreeNodes() {
		return rtreeNodes;
	}

	public void setRtreeNodes(RTree<Long, Point> rtreeNodes) {
		this.rtreeNodes = rtreeNodes;
	}

	RTree<Long, Point> rtreePois;
	RTree<Long, Point> rtreeNodes;
	Graph graph;
	
	public RTreePoisIndexImpl(Graph graph) {
		this.graph = graph;
		this.rtreePois = RTree.create();
		this.rtreeNodes = RTree.create();
		indexingNodesGraph();
	}

	@Override
	public void indexingPois(List<org.graphast.geometry.Point> pois) {
		this.rtreePois = RTree.create();
		Long poiId = (long) 0;
		// index pois to filter step
		for (org.graphast.geometry.Point poi : pois) {
			com.github.davidmoten.rtree.geometry.Point p = Geometries.point(poi.getLatitude(), poi.getLongitude());
			this.rtreePois = this.rtreePois.add(poiId, p);			
			poiId++;
		} 
	}
	
	@Override
	public void indexingNodesGraph() {
		for (Long id=(long) 0;  id< this.graph.getNumberOfNodes(); id++) {
			com.github.davidmoten.rtree.geometry.Point p = Geometries.point(graph.getNode(id).getLatitude(),graph.getNode(id).getLongitude());
			this.rtreeNodes = this.rtreeNodes.add(id, p);			
		} 
	}

	@Override
	public List<NearestNeighbor> knnSearch(int k, int maxDist, long queryId) {
		List<NearestNeighbor> nns = new ArrayList<>();
		Point query = Geometries.point(graph.getNode(queryId).getLatitude(), graph.getNode(queryId).getLongitude());
		// k candidates nearest from query according to Euclidean distance
		List<Entry<Long, Point>> list = rtreePois.nearest(query, maxDist, k).toList().toBlocking().single();
		
		for (Entry<Long, Point> entry : list) {
			long id = entry.value();
			// mapping to nodes in graph
			List<Entry<Long, Point>> nodeEntryList = rtreeNodes.nearest(entry.geometry(), maxDist, 1).toList().toBlocking().single();
			Long nodeId = nodeEntryList.get(0).value();
			NearestNeighbor nn = new NearestNeighbor(nodeId, (int) DistanceUtils.distanceLatLong(graph.getNode(nodeId), graph.getNode(queryId)));
			nns.add(nn);
		}
		return nns;
	}
	
	@Override
	public Long nearestNode(org.graphast.geometry.Point point){
		Point p = Geometries.point(point.getLatitude(), point.getLongitude());
		List<Entry<Long, Point>> nodeEntryList = getRtreeNodes().nearest(p, 1000, 1).toList().toBlocking().single();
		Long nodeId = nodeEntryList.get(0).value();
		return nodeId;
		
	}

	/*public List<NearestNeighbor> rangeSearch(double range, long queryId) {
		List<NearestNeighbor> nns = new ArrayList<>();
		Point query = Geometries.point(graph.getNode(queryId).getLatitude(), graph.getNode(queryId).getLongitude());
		List<Entry<Long, Point>> list = rtreePois.search(query, range).toList().toBlocking().single();
		for (Entry<Long, Point> entry : list) {
			long id = entry.value();
			NearestNeighbor nn = new NearestNeighbor(id, (int) DistanceUtils.distanceLatLong(graph.getNode(id), graph.getNode(queryId)));
			nns.add(nn);
		}	
		return nns;

	}*/


}
