package org.graphast.importer;

import org.graphast.model.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyntheticPoisGenerator {

	
	private static Logger log = LoggerFactory.getLogger(SyntheticPoisGenerator.class);
	
	public static void generateRandomSyntheticPois(Graph graph, double randomFactor, int categoryId) {
		int numberPois = 0;
		for (int id = 0; id < graph.getNumberOfNodes(); id++) {
			if (Math.random() < randomFactor) {
				graph.setNodeCategory(id, categoryId);
				numberPois++;
			}
		}
		log.info("Generate "+numberPois+" pois.");
	}

}
