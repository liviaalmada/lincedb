package org.graphast.query.astarrnn;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ReadOnlyFileSystemException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.graphast.geometry.Point;
import org.graphast.importer.CostGenerator;
//import org.graphast.importer.SyntheticPoisGenerator;
import org.graphast.model.Graph;
import org.graphast.model.GraphBoundsImpl;
import org.graphast.model.GraphImpl;
import org.graphast.model.Node;
import org.graphast.model.NodeImpl;

import com.eatthepath.jvptree.VPTree;
import com.google.protobuf.TextFormat.ParseException;

public class GraphRealExperimentsGenerator {

	private static final long oneHour = 3600000;

	/**
	 * @author Leopoldo Melo
	 *
	 */
	public static List<Point> readRealPois(String pois, Date hourRange) {
		// Open file
		FileInputStream fstream;
		String strLine = null;
		ArrayList<Point> list = new ArrayList<Point>();
		String AddedNodes = "";
		SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'z '('Z')'", Locale.ENGLISH);

		try {
			fstream = new FileInputStream(pois);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				int pos1 = 0, pos2 = 0;
				int col = 0;
				long id = 0;
				Date dh = null;
				double lat = 0., lon = 0.;
				while (strLine.indexOf(";", pos1) > 0) {
					pos2 = strLine.indexOf(";", pos1 + 1);
					switch (col) {
					case 0:
						id = Long.parseLong(strLine.substring(pos1, pos2));
						break;
					case 1:
						lat = Double.parseDouble(strLine.substring(pos1, pos2));
						break;
					case 2:
						lon = Double.parseDouble(strLine.substring(pos1, pos2));
						break;
					case 7:
						dh = formatter.parse(strLine.substring(pos1, pos2)); // Date
																				// and
																				// hour
																				// of
																				// taxi
																				// position

						if (!AddedNodes.contains(id + ";")) { // Test to
																// avoid
																// registering
																// the same
																// taxi
																// twice.
							list.add(new Point(lat, lon));
							AddedNodes += id + ";";
						}

					}
					col++;
					if (col == 8)
						col = 0;
					pos1 = pos2 + 1;
				}
			}
			// Close the input stream
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return list;
	}

	public static GraphBoundsImpl generateGraphBounds(String path) {
		GraphBoundsImpl gbounds = new GraphBoundsImpl(path);
		gbounds.createBounds();
		gbounds.loadFromGraph();
		return gbounds;

	}
}
