package org.graphast.importer;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import org.graphast.importer.Importer;
import org.graphast.model.Edge;
import org.graphast.model.EdgeImpl;
import org.graphast.model.Graph;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;
import org.graphast.model.NodeImpl;
import org.graphast.query.dao.sensors.SensorsDAO;
import org.graphast.util.DistanceUtils;
import org.graphast.util.GeoUtils;
import org.graphast.util.NumberUtils;
import org.postgis.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class POISensorsImporter implements Importer {

	private SensorsDAO dao;
	private Graph graph;
	private Graph newGraph;
	private String table;
	private HashSet<Long> edgesToExclude;
	private static double THRESHOLD = 1;
	// private static String PATH_GRAPH;
	private static final int FIELD_EXTERNAL_ID = 1;
	private static final int FIELD_POINT = 3;
	private static final int FIELD_NAME = 2;

	private static Logger log = LoggerFactory.getLogger(POISensorsImporter.class);

	public POISensorsImporter(String tableName, Graph graph) {
		this.table = tableName;
		this.graph = graph;
		this.edgesToExclude = new HashSet<>();
		this.newGraph = new GraphImpl(graph.getAbsoluteDirectory() + "/pois");
		this.dao = new SensorsDAO();
	}

	@Override
	public Graph execute() {
		try {
			copyNodes();
			loadSensorsPois();
			copyEdges();

		} catch (SQLException | ClassNotFoundException | IOException e) {
			System.err.println("[ERRO] Ocorreu um erro na construção do grafo.");
			System.err.println(e);
		}

		return newGraph;
	}

	private void copyEdges() {
		for (int id = 0; id < graph.getNumberOfEdges(); id++) {
			if (!edgesToExclude.contains(id)) {
				Edge edge = graph.getEdge(id);
				Node from = graph.getNode(edge.getFromNode());
				Node to = graph.getNode(edge.getToNode());
				int distance = (int) NumberUtils.round(DistanceUtils.distanceLatLong(from.getLatitude(),
						from.getLongitude(), to.getLatitude(), to.getLongitude()), 0);
				newGraph.addEdge(new EdgeImpl(edge.getFromNode(), edge.getToNode(), distance));
				log.debug("Copy edge: {}", id);
			} else {
				log.debug("Descart edge: {}", id);
			}
		}
	}

	private void copyNodes() {

		for (int id = 0; id < graph.getNumberOfNodes(); id++) {
			Node oldNode = graph.getNode(id);
			Node node = new NodeImpl(oldNode.getLatitude(), oldNode.getLongitude());
			newGraph.addNode(node);
		}

	}

	@SuppressWarnings("unused")
	private Graph loadSensorsPois() throws SQLException, ClassNotFoundException, IOException {

		ResultSet result = dao.getSensorsLocations(table);
		while (result.next()) {

			String strPointSensor = result.getString(FIELD_POINT);
			String strLabelSensor = result.getString(FIELD_NAME);
			String strExtIdSensor = result.getString(FIELD_EXTERNAL_ID);
			// add node
			Point point = new Point(strPointSensor);
			Node node = new NodeImpl(point.getY(), point.getX());
			node.setLabel(strLabelSensor);
			node.setExternalId(Long.valueOf(strExtIdSensor));
			newGraph.addNode(node);
			// TODO verify if is necessary include as a poi

			Long nodeId = newGraph.getNodeId(node.getLatitude(), node.getLongitude());
			node = newGraph.getNode(nodeId);

			log.info("Import sensor node: {}", node);

			// split edge where the node is on
			long numberOfEdges = graph.getNumberOfEdges();
			long edgeId = 0;

			while (edgeId < numberOfEdges) {
				Edge edge = graph.getEdge(edgeId);

				if (isPointInEdgeLine(graph, node, edge, THRESHOLD)) {
					edgesToExclude.add(edge.getId());
					splitEdge(node, edge);
					break;
				}
				edgeId++;
			}
		}
		return newGraph;

	}

	private void splitEdge(Node node, Edge edge) {
		Node from = graph.getNode(edge.getFromNode());
		Node to = graph.getNode(edge.getToNode());
		
		Long nodeFromId = newGraph.getNodeId(GeoUtils.latLongToInt(from.getLatitude()), GeoUtils.latLongToInt(from.getLongitude()));
		Long nodeToId = newGraph.getNodeId(GeoUtils.latLongToInt(to.getLatitude()), GeoUtils.latLongToInt(to.getLongitude()));
		
		EdgeImpl edgeIn = new EdgeImpl(nodeFromId, node.getId().longValue(), 0);	
		EdgeImpl edgeOut = new EdgeImpl(node.getId().longValue(), nodeToId, 0);
		
		newGraph.addEdge(edgeIn);
		newGraph.addEdge(edgeOut);
		log.debug("Success for edge: {}", edge);
	}

	private boolean isPointInEdgeLine(Graph graph2, Node node, Edge edge, double threshold) {
		Node start = graph.getNode(edge.getFromNode());
		Node end = graph.getNode(edge.getToNode());
		return DistanceUtils.distanceLatLong(start, node) + DistanceUtils.distanceLatLong(node, end)
				- DistanceUtils.distanceLatLong(start, end) <= threshold;

	}

	public static void runImport(String tableName, Graph graph) throws IOException {

		POISensorsImporter importer = new POISensorsImporter(tableName, graph);
		Graph newGraph = importer.execute();
		newGraph.save();	

	}

}
