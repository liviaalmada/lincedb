package org.graphast.query.astarrnn;

import java.util.List;

import org.graphast.query.knn.NearestNeighbor;

/**
 * This interface is responsible by select a set of POIs that are candidates to be the k reverse nearest neighbors. 
 * @author Lívia
 *
 */
public interface RNNCandidatesSelector{
	
	public List<NearestNeighbor> search(Long idSource, int k);
	public List<NearestNeighbor> search(Long idSource, double range);

}
