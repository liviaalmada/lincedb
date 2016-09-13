package org.graphast.importer.amc;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.graphast.importer.Importer;
import org.graphast.model.Edge;
import org.graphast.model.EdgeImpl;
import org.graphast.model.Graph;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;
import org.graphast.model.NodeImpl;
import org.graphast.query.dao.postgis.GraphastDAO;
import org.graphast.util.ConnectionJDBC;
import org.graphast.util.DistanceUtils;
import org.graphast.util.GeoUtils;
import org.postgis.LineString;
import org.postgis.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load a time-dependent graph from a database with all costs set to zero
 * 
 */
public class SimpleDBOSMImporter implements Importer {

	private GraphastDAO dao;
	protected Graph graph;
	private String table;
	private final int FIELD_ID_LINESTRING = 1;
	private final int FIELD_LINESTRING = 2;
	private final int SIZE_INTERVAL = 96;
	protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	protected static final String PATH_GRAPH = "C:\\Users\\Lívia\\git\\graphast\\core\\src\\main\\resources\\";

	public SimpleDBOSMImporter(String table, String directory) {
		this.table = table;
		dao = new GraphastDAO();
		graph = new GraphImpl(directory);
	}

	private void loadNodes() throws SQLException, ClassNotFoundException, IOException {

		ResultSet result = dao.getPoints(table);
		while (result.next()) {
			LineString lineString = new LineString(result.getString(FIELD_LINESTRING));
			Point[] arrayPoints = lineString.getPoints();
			LOGGER.info(String.format("registro: %s", result.getString(FIELD_LINESTRING)));

			int idRoad = result.getInt(FIELD_ID_LINESTRING);
			loadFromArrayPoints(arrayPoints, idRoad);
		}

		ConnectionJDBC.getConnection().close();

	}

	protected void loadFromArrayPoints(Point[] arrayPoints, int idRoad) {
		Node previousNode = null;
		int pointCount = 0;

		for (Point point : arrayPoints) {
			pointCount++;
			LOGGER.info(String.format("Point [x,y]: %s,%s", point.getX(), point.getY()));
			Node node = addNode(idRoad, point);

			addEdge(idRoad, previousNode, node);

			LOGGER.info(String.format("Graph now has %s nodes", graph.getNumberOfNodes()));
			LOGGER.info(String.format("Graph now has %s edges", graph.getNumberOfEdges()));

			previousNode = node;
		}
		LOGGER.info(String.format("Total points parsed are: %s", pointCount));
	}

	private void addEdge(int idRoad, Node previousNode, Node node) {
		if (previousNode != null && !previousNode.getId().equals(node.getId())) {
			LOGGER.info(String.format("Add edge from previous: %s to current: %s node", previousNode.getId(),
					node.getId()));
			Edge edge = new EdgeImpl(idRoad, previousNode.getId().longValue(), node.getId().longValue(),
					(int) DistanceUtils.distanceLatLong(previousNode, node), String.valueOf(idRoad));
			addCostZero(edge);
			graph.addEdge(edge);

		}
	}

	private Node addNode(int idRoad, Point point) {
		Node node = new NodeImpl(point.getY(), point.getX());
		node.setLabel(Long.valueOf(idRoad).toString());
		Long nodeId = graph.getNodeId(GeoUtils.latLongToInt(node.getLatitude()),
				GeoUtils.latLongToInt(node.getLongitude()));

		if (nodeId != null) {
			LOGGER.info(String.format("point already exist in graph"));
			node = graph.getNode(nodeId);
		} else {
			graph.addNode(node);
			LOGGER.info(String.format("point inserted in graph with ID: %s", node.getId()));
		}
		return node;
	}

	public static void runImport(String tableName) throws IOException {
		SimpleDBOSMImporter importer = new SimpleDBOSMImporter(tableName, PATH_GRAPH + tableName);
		Graph graph = importer.execute();
		graph.save();

	}

	@Override
	public Graph execute() {
		try {
			loadNodes();
		} catch (SQLException | ClassNotFoundException | IOException e) {
			// System.err.println("[ERRO] Ocorreu um erro na construção do
			// grafo.");
			System.err.println(e);
		}
		return graph;
	}

	private void addCostZero(Edge edge) {

		int[] costs = new int[SIZE_INTERVAL];
		for (int i : costs) {
			costs[i] = 0;
		}
		edge.setCosts(costs);
	}

	public static void main(String[] args) {
		try {
			SimpleDBOSMImporter.runImport("view_exp_100k");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
