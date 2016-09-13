package org.graphast.query.astarrnn;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.graphast.model.GraphBounds;
import org.graphast.model.GraphBoundsImpl;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;
import org.graphast.query.knn.NearestNeighbor;
import org.graphast.query.rnn.RNNBacktrackingSearch;
import org.graphast.util.DateUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class RNNAstarSearchTeste {
	
	private static final String PATH_GRAPH = "C:\\Users\\Lívia\\git\\graphast\\core\\src\\main\\resources\\";
	private static GraphImpl g;
	private static GraphImpl graph;
	private static RNNAstarSearch rnnAstar;
	private static RNNBacktrackingSearch rnnBack;
	
	@BeforeClass
	public static void setup(){
		g = new GraphImpl(PATH_GRAPH + "view_exp_100k500Pois");
		g.load();
		graph = new GraphBoundsImpl(g.getAbsoluteDirectory());
		((GraphBoundsImpl) graph).loadFromGraph();		
		rnnAstar = new RNNAstarSearch(graph, new RTreePoisIndexImpl(graph), 400);
		rnnBack = new RNNBacktrackingSearch((GraphBounds) graph);
		
	}
	
	@Test
	public void test1(){
		Date timeout = DateUtils.parseDate(23, 00, 00);
		Date timestamp = DateUtils.parseDate(12, 00, 00);
		Node query = graph.getNode(5001);
		NearestNeighbor nnAstar = rnnAstar.search(query, timeout, timestamp);
		System.out.println(nnAstar.getTravelTime());
		NearestNeighbor nnBFS = rnnBack.search(query, timeout, timestamp);
		System.out.println(nnBFS.getTravelTime());
		assertEquals(nnAstar.getId(),nnBFS.getId());
	}

}
