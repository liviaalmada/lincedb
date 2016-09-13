package org.graphast.query.dao.sensors;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.graphast.util.ConnectionJDBC;

public class SensorsDAO {
	
	public ResultSet getSensorsLocations(String table) throws ClassNotFoundException, SQLException, IOException {

		String finalQuery = QuerySensors.QUERY_ALL_SENSORS; 
				//String.format(QueryPostgis.QUERY_POINT_ROAD.replace("TABLE_NAME", "%s"), table);
		Statement statement = ConnectionJDBC.getConnection().createStatement();
		return statement.executeQuery(finalQuery);
	}

}
