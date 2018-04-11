package org.ovgu.de.fiction.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.ovgu.de.fiction.model.Similarity;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/*
 * Author: Sayantan
 * Simialrity function implementations
 */
public class FRSimilarityUtils {

	final static Logger LOG = Logger.getLogger(FRSimilarityUtils.class);
	
	public Map<String, Similarity> getNaiveSimilarityBtwnQueryAndCorpus(Map<String, double[]> corpus, String queryBookId) {
		Map<String, Similarity> result_Map = new HashMap<>(); // key=cosineSimVal, value=BookId

		double[] qry_vector = corpus.get(queryBookId);
		double[] book_vector = null;
		double dotProduct = 0.0;
		double norm_qry = 0.0;
		double L1_dist = 0.0;
		double L2_dist = 0.0;

		for (int j = 0; j < qry_vector.length; j++) {
			norm_qry = norm_qry + (qry_vector[j] * qry_vector[j]);
		}
		norm_qry = Math.sqrt(norm_qry);

		double norm_book = 0.0;
		double cosineSim = 0.0;
		Similarity docSims = null;

		for (Map.Entry<String, double[]> corpus_input : corpus.entrySet()) {

			if (!queryBookId.equals(corpus_input.getKey())) { /* check all vectors, except Query itself! which is also present!*/
				book_vector = corpus_input.getValue();
				dotProduct = 0.0; // reset dot product value for each new book of a corpus
				norm_book = 0.0;
				docSims = new Similarity();
				for (int i = 0; i < qry_vector.length; i++) {
					dotProduct = dotProduct + (qry_vector[i] * book_vector[i]);
					norm_book = norm_book + (book_vector[i] * book_vector[i]);
					L1_dist = L1_dist + Math.abs(qry_vector[i] - book_vector[i]);
					L2_dist = L2_dist + Math.pow(qry_vector[i] - book_vector[i], 2);
				}

				if (norm_book > 0 && norm_qry > 0) {
					cosineSim = dotProduct / (Math.sqrt(norm_book) * norm_qry); // Nr/Dr; Dr =
																				 // Normalised qry
																				 // vec * given vec
					L2_dist = L2_dist / (Math.sqrt(norm_book) * norm_qry); // normalize
					L2_dist = 1 / (1 + L2_dist);// sim = 1/(1+dist);
					L1_dist = 1 / (1 + L1_dist);// sim = 1/(1+dist);
				}

				cosineSim = Math.round(cosineSim * 1000.000) / 1000.000;
				L1_dist = Math.round(L1_dist * 1000.000) / 1000.000;
				L2_dist = Math.round(L2_dist * 1000.000) / 1000.000;

				docSims.setCosineSim(cosineSim);
				docSims.setL1Sim(L1_dist);
				docSims.setL2Sim(L2_dist);

				result_Map.put(corpus_input.getKey(), docSims);
				LOG.debug("Key = " + corpus_input.getKey() + " cosine  sim = " + cosineSim);
				LOG.debug("Key = " + corpus_input.getKey() + " L1      sim = " + L1_dist);
				LOG.debug("Key = " + corpus_input.getKey() + " L2-Eucl sim = " + L2_dist);
			}
		}

		return result_Map;

	}

	/**
	 * @param qryBookId
	 * @param bookChunkFeatureMap
	 * @param queryBookId
	 * @param topkRequested
	 * @param simType
	 * @return Map of results
	 * @throws IOException
	 * @author Sayantan
	 * @category - Input: Corpus plus the chunks of a corpus
	 */

	public Map<Double, String> getSingleNaiveSimilarity(Map<String, Map<String, double[]>> books, String qryBookId,
			Entry<String, double[]> query, String simType, int topKRes, int LEAVE_LAST_K_ELEMENTS_OF_FEATURE) throws IOException {

		int topkRequested = topKRes*3;//FRConstants.TOP_K_RESULTS * 3;

		if (books == null || qryBookId == null || query == null || simType == null)
			return null;

		SortedMap<Double, String> results = new TreeMap<Double, String>(Collections.reverseOrder());
		SortedMap<Double, String> topK_results = new TreeMap<Double, String>(Collections.reverseOrder());

		Multimap<Double, String> multimap = ArrayListMultimap.create();

		double[] book_vector = null;
		double dist_meas_values = 0.0; // this will hold cosine or L1 or L2 measures, depending upon
										 // user choice
		double norm_qry = 0.0;

		// find normalised qry vector value ONLY once
		double[] queryFeature = query.getValue();
		for (int j = 0; j < queryFeature.length-LEAVE_LAST_K_ELEMENTS_OF_FEATURE; j++) { //this_loop_excludes_last_2_elements_of_feature_vector
			norm_qry = norm_qry + (queryFeature[j] * queryFeature[j]);
		}
		norm_qry = Math.sqrt(norm_qry);

		double norm_book = 0.0;

		for (Entry<String, Map<String, double[]>> book : books.entrySet()) {

			for (Entry<String, double[]> chunk : book.getValue().entrySet()) {
				// check all vectors, exceptQuery itself! which is also present!
				if (!(qryBookId.equals(book.getKey()) && query.getKey().equals(chunk.getKey()))) {
					book_vector = chunk.getValue();
					dist_meas_values = 0.0;
					norm_book = 0.0;
					for (int i = 0; i < queryFeature.length-LEAVE_LAST_K_ELEMENTS_OF_FEATURE; i++) {//this_loop_excludes_last_2_elements_of_feature_vector
						if (simType.equals(FRConstants.SIMILARITY_COSINE))
							dist_meas_values = dist_meas_values + (queryFeature[i] * book_vector[i]);
						else if (simType.equals(FRConstants.SIMILARITY_L1))
							dist_meas_values = dist_meas_values + Math.abs(queryFeature[i] - book_vector[i]); // |x1-x2|
						else if (simType.equals(FRConstants.SIMILARITY_L2))
							dist_meas_values = dist_meas_values + Math.pow(queryFeature[i] - book_vector[i], 2); // (x1-x2)^2

						norm_book = norm_book + (book_vector[i] * book_vector[i]);
					}

					// only if Dr>0 & Normalise ONLY for Cosine and L2, not L1!
					if (norm_book > 0 && norm_qry > 0 && simType.equals(FRConstants.SIMILARITY_COSINE)) {
						dist_meas_values = dist_meas_values / (Math.sqrt(norm_book) * norm_qry);
					} else if (norm_book > 0 && norm_qry > 0 && simType.equals(FRConstants.SIMILARITY_L2)) {
						dist_meas_values = Math.sqrt(dist_meas_values);
						dist_meas_values = 1 / (1 + dist_meas_values);
					} else if (norm_book > 0 && norm_qry > 0 && simType.equals(FRConstants.SIMILARITY_L1)) {
						// no normalization
						dist_meas_values = 1 / (1 + dist_meas_values);
					}

					dist_meas_values = Math.round(dist_meas_values * 10000.0000) / 10000.0000;
					if (dist_meas_values > FRConstants.SIMILARITY_CUTOFF) {
						multimap.put(dist_meas_values, book.getKey() + "-" + chunk.getKey());
					}
				}
			}

		}

		// below is just for sorting from multimap
		for (

		Map.Entry<Double, String> all_sim_vals : multimap.entries()) {
			Double d1 = all_sim_vals.getKey(); // cosine sim value, L1, L2
			String book = !results.containsKey(d1) ? all_sim_vals.getValue() : results.get(d1) + " " + all_sim_vals.getValue();
			results.put(d1, book); // this is a reverse sorted tree , by decreasing relevance rank,
			// 0.987-> book6, book67 -> top
			// 0.851-> book5
			// 0.451-> book9, book89 -> lowest
		}

		// get top K results from sorted Results Map
		int topkCount = 0;
		for (Map.Entry<Double, String> topRes_staging : results.entrySet()) {
			if (topkCount < topkRequested)
				topK_results.put(topRes_staging.getKey(), topRes_staging.getValue());
			else
				break;

			topkCount++;
		}
		return topK_results;

	}
	
	/**
	 * 
	 * @param corpus
	 * @param queryBookId
	 * @param topkRequested
	 * @param simType
	 * @return
	 * @throws IOException
	 * @author Sayantan
	 * @category - Difference with 'getSingleNaiveSimilarity' is that, here it just accepts corpus and a single qry string (*NOT* the chunks of a book)
	 */
	public SortedMap<Double, String> getSingleNaiveSimilarityDummy(Map<String, double[]> corpus, String queryBookId, int topkRequested, String simType) throws IOException {
        // usually SortedMap - TreeMap would have sorted in low to high number value, but we use 'Collections.reverseOrder()'
		SortedMap<Double, String> results = new TreeMap<Double, String>(Collections.reverseOrder());
		SortedMap<Double, String> topK_results = new TreeMap<Double, String>(Collections.reverseOrder());
		
		Multimap<Double, String> multimap = ArrayListMultimap.create();
		
		double[] qry_vector = corpus.get(queryBookId);
		double[] book_vector=null;
		double dist_meas_values = 0.0; // this will hold cosine or L1 or L2 measures, depending upon user choice
		double norm_qry = 0.0;
		
		//find normalised qry vector value ONLY once
		for(int j=0;j<qry_vector.length;j++){
			norm_qry = norm_qry+(qry_vector[j]*qry_vector[j]);
		}
		norm_qry = Math.sqrt(norm_qry);
		
		double norm_book  = 0.0;
		
		
		
		for(Map.Entry<String, double[]> corpus_input : corpus.entrySet()){
			
			if(!queryBookId.equals(corpus_input.getKey()))
			{ // check all vectors, except Query itself! which is also  present!
			book_vector = corpus_input.getValue();
			dist_meas_values = 0.0 ; //reset dot product or L1/L2 value for each new book of a corpus
			norm_book = 0.0;
			  for(int i=0;i<qry_vector.length;i++){
				  if(simType.equals(FRConstants.SIMILARITY_COSINE))
					  dist_meas_values = dist_meas_values + (qry_vector[i]*book_vector[i]);
				  else if(simType.equals(FRConstants.SIMILARITY_L1))
					  dist_meas_values = dist_meas_values + Math.abs(qry_vector[i]-book_vector[i]); //  |x1-x2|
				  else if(simType.equals(FRConstants.SIMILARITY_L2))
					  dist_meas_values = dist_meas_values + Math.pow(qry_vector[i]-book_vector[i],2); // (x1-x2)^2 
				  
				  norm_book = norm_book + (book_vector[i]*book_vector[i]);
		   }
			  
			// only if Dr>0 & Normalise ONLY for Cosine and L2, not L1!
			   if(norm_book>0 && norm_qry>0 && simType.equals(FRConstants.SIMILARITY_COSINE)){ 
				   // for cosine sim, similarity = measure_values 
				   dist_meas_values = dist_meas_values/(Math.sqrt(norm_book)*norm_qry); // Nr/Dr; Dr = Normalised qry vec * given vec
			  }else if(norm_book>0 && norm_qry>0 && simType.equals(FRConstants.SIMILARITY_L2)){
				  // for Euclidean L2 similarity, distance = Sigma(x2-x1)^2/Normalise;
				  dist_meas_values = Math.sqrt(dist_meas_values); // normalize for L2-? : dist_meas_values = dist_meas_values/(Math.sqrt(norm_book)*norm_qry);
				  dist_meas_values = 1/(1+dist_meas_values); //similarity = 1/(1+normalized L2 distance)
				  
			  }else if(norm_book>0 && norm_qry>0 && simType.equals(FRConstants.SIMILARITY_L1)){
				  // no normalization
				  dist_meas_values = 1/(1+dist_meas_values); // similarity = 1/(1+un normalized L1 distance)
			  }
			   
			   dist_meas_values = Math.round(dist_meas_values * 10000.0000)/10000.0000; 
			   multimap.put(dist_meas_values, corpus_input.getKey());
			  // System.out.println("Key = "+corpus_input.getKey()+" cosine  sim = "+cosineSim);
			} 
		}
		
		//below is just for sorting from multimap
		for(Map.Entry<Double, String> all_sim_vals: multimap.entries()){
			Double d1 = all_sim_vals.getKey(); // cosine sim value, L1, L2
			String book = !results.containsKey(d1) ? all_sim_vals.getValue() : results.get(d1)+" "+all_sim_vals.getValue();
			results.put( d1, book); // this is a reverse sorted tree , by decreasing relevance rank, 
			// 0.987-> book6, book67 -> top
			// 0.851-> book5
			// 0.451-> book9, book89 -> lowest
		}
		
		//get top K results from sorted Results Map
		int topkCount=0;
		for(Map.Entry<Double, String> topRes_staging: results.entrySet()){
			if(topkCount<topkRequested)
			topK_results.put(topRes_staging.getKey(), topRes_staging.getValue());
			else
				break;
			
			topkCount++;
		}
		
		return topK_results; //return results;
		
	}

}
