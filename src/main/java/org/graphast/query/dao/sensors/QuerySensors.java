package org.graphast.query.dao.sensors;

public interface QuerySensors {
	String QUERY_ALL_SENSORS = "SELECT id, name, ST_AsText( ST_Force_2D(geom)) FROM amc.sensores;";
}
