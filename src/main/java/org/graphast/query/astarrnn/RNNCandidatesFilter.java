package org.graphast.query.astarrnn;

import java.util.Date;
import java.util.List;

import org.graphast.query.knn.NearestNeighbor;
import org.graphast.query.route.shortestpath.model.Path;

/**
 * This interface is responsible to filter a set of POIs that are candidates to be reverse nearest neighbors.
 * @author Lívia
 *
 */
public interface RNNCandidatesFilter {
	Path filter(Long idQuery, Date startTime, List<NearestNeighbor> candidates);
}
