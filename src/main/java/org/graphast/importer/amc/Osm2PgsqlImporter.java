package org.graphast.importer.amc;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.security.auth.login.Configuration;

import org.graphast.model.Graph;
import org.graphast.query.dao.postgis.osm2psql.Osm2PgsqlDao;
import org.graphast.util.ConnectionJDBC;
import org.postgis.LineString;
import org.postgis.Point;

public class Osm2PgsqlImporter extends SimpleDBOSMImporter {

	private Osm2PgsqlDao dao;

	public Osm2PgsqlImporter(String directory) {
		super("planet_osm_line", directory);
		dao = new Osm2PgsqlDao();
	}

	@Override
	public Graph execute() {
		
		try {
			loadNodes();
		} catch (SQLException | ClassNotFoundException | IOException e) {
			System.err.println(e);
		}
		return super.graph;

	}

	private void loadNodes() throws ClassNotFoundException, SQLException, IOException {

		ResultSet result = dao.getPoints();

		while (result.next()) {
			System.out.println(result.getString(2));
			LineString lineString = new LineString(result.getString(2));
			Point[] arrayPoints = lineString.getPoints();
			int idRoad = result.getInt(1);
			loadFromArrayPoints(arrayPoints, idRoad);
		}

		ConnectionJDBC.getConnection().close();

	}
	
	public static void runImport(String osmName) throws IOException {
		Osm2PgsqlImporter importer = new Osm2PgsqlImporter(PATH_GRAPH + osmName);
		Graph graph = importer.execute();
		graph.save();

	}

	public static void main(String[] args) {
		try {
			Osm2PgsqlImporter.runImport("ceara");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
