package org.graphast.query.dao.postgis.osm2psql;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.graphast.query.dao.postgis.GraphastDAO;
import org.graphast.util.ConnectionJDBC;

/**
 * This class accesses a database created by osm2pgsql tool.
 * 
 * @author Lívia
 *
 */
public class Osm2PgsqlDao extends GraphastDAO{
	
	static String QUERY_WAYS ="select osm_id, ST_AsText(ST_Transform(way,4326)) from planet_osm_line where way is not null;";

	public ResultSet getPoints() throws ClassNotFoundException, SQLException, IOException {

		Statement statement = ConnectionJDBC.getConnection().createStatement();
		return statement.executeQuery(QUERY_WAYS);
	}

}
