package org.graphast.query.astarrnn;

import java.util.ArrayList;
import java.util.List;

import org.graphast.geometry.Point;
import org.graphast.model.Graph;
import org.graphast.model.Node;
import org.graphast.model.NodeImpl;
import org.graphast.query.knn.NearestNeighbor;

import com.eatthepath.jvptree.VPTree;

public class VPTreePoisIndex implements PoisIndex {

	private VPTree<Node> vpTreePois;
	private VPTree<Node> vpTreeNodes;
	Graph graph;

	public VPTreePoisIndex(Graph graph) {
		vpTreePois = new VPTree<Node>(new VPTreeEuclideanDistance());
		vpTreeNodes = new VPTree<Node>(new VPTreeEuclideanDistance());
		this.graph = graph;
		indexingNodesGraph();
	}

	@Override
	public List<NearestNeighbor> knnSearch(int k, int maxDist, long queryId) {
		// TODO Auto-generated method stub
		Node node = graph.getNode(queryId);
		VPTreeEuclideanDistance d = new VPTreeEuclideanDistance();
		List<NearestNeighbor> knn = new ArrayList<NearestNeighbor>();
		List<Node> vpTreeSearch = new ArrayList<Node>();

		vpTreeSearch = vpTreePois.getNearestNeighbors(node, k);
		for (Node n : vpTreeSearch) {
			knn.add(new NearestNeighbor(n.getId(), (int) d.getDistance(node, n)));
		}

		return knn;
	}

	@Override
	public void indexingPois(List<Point> pois) {
		for (Point poi : pois) {
			List<Node> nearestNeighbors = vpTreeNodes.getNearestNeighbors(new NodeImpl(poi.getLatitude(), poi.getLongitude()), 1);
			vpTreePois.add(nearestNeighbors.get(0));
		}		
	}


	@Override
	public void indexingNodesGraph() {
		for (Long id=(long) 0;  id< this.graph.getNumberOfNodes(); id++) {
			Node node = graph.getNode(id);
			vpTreeNodes.add(node);
		} 
		
	}


	@Override
	public Long nearestNode(Point point) {
		List<Node> nearestNeighbors = vpTreeNodes.getNearestNeighbors(new NodeImpl(point.getLatitude(),point.getLongitude()), 1);
		if(nearestNeighbors.get(0)!=null){
			return nearestNeighbors.get(0).getId();
		}
		return null;
	}

}
