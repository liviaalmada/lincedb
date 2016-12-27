package org.graphast.query.astarrnn;

import org.graphast.model.Node;
import org.graphast.util.DistanceUtils;

import com.eatthepath.jvptree.DistanceFunction;

public class VPTreeEuclideanDistance implements DistanceFunction<Node>{

	@Override
	public double getDistance(Node node1, Node node2) {
		// TODO Auto-generated method stub
		return DistanceUtils.distanceLatLong(node1.getLatitude(), node1.getLongitude(), node2.getLatitude(), node2.getLongitude());
      
	}
}
