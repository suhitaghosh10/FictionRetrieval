package org.ovgu.de.fiction.search;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.ovgu.de.fiction.feature.extraction.ChunkDetailsGenerator;
import org.ovgu.de.fiction.model.TopKResults;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRSimilarityUtils;

/**
 * @author Suhita,Sayantan
 */

public class FictionRetrievalSearch {
	final static Logger LOG = Logger.getLogger(ChunkDetailsGenerator.class);
	public static TopKResults findRelevantBooks(String qryBookNum, String featureCsvFile, String PENALISE, String ROLLUP, 
			String TTR_CHARS,int topKRes,String similarity) throws IOException {
		Map<String, Map<String, double[]>> books = getChunkFeatureMapForAllBooks(featureCsvFile);

		SortedMap<Double, String> results_topK = compareQueryBookWithCorpus(qryBookNum, books, PENALISE, ROLLUP, TTR_CHARS,topKRes,similarity);
		
		TopKResults topK = new TopKResults();
		topK.setBooks(books);
		topK.setResults_topK(results_topK);
		return topK;
	}

	private static Map<String, Map<String, double[]>> getChunkFeatureMapForAllBooks(String featureCsvFile)
			throws IOException, FileNotFoundException {
		String line = "";
		Map<String, Map<String, double[]>> books = new HashMap<>();
		int csvRow = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(featureCsvFile));) {

			while ((line = br.readLine()) != null) {
				if (csvRow > 0) { // ignore headers
					String[] csvElemArr = line.split(",");// array of 21 elements, 0th=
															// book-chunkNum, 1-20 = feature vector
															// elements
					books = generateChunkFeatureMapForAChunk(csvElemArr, books);
				}
				csvRow = 1;

			}
		}
		return books;
	}

	private static SortedMap<Double, String> compareQueryBookWithCorpus(String qryBookId, Map<String, Map<String, double[]>> books, 
			String PENALISE, String ROLLUP, String TTR_CHARS,int topKRes, String similarity)
			throws IOException {
		// Step: Send the query book chunk wise and find relevance rank with corpus
		FRSimilarityUtils simUtils = new FRSimilarityUtils();
		String simType = FRConstants.SIMILARITY_L2;
		if(similarity!=null)
		simType = similarity;//FRConstants.SIMILARITY_L2;
		Map<String, Map<Double, String>> staging_results = new HashMap<>();

		/*
		 * chunk_results = Top 20 results Map with => Key = bookId_ChunkNUM , Value = Similarity
		 * normalized
		 */
		Map<String, double[]> queryChunkMap = books.get(qryBookId);

		for (Map.Entry<String, double[]> queryChunk : queryChunkMap.entrySet()) { // loop over
																					 // corpus and
																					 // find those
																					 // elements
																					 // that *do*
																					 // match the
																					 // query book
            // Important: LEAVE_LAST_K_ELEMENTS_OF_FEATURE = from similarity computation
			Map<Double, String> chunkSimResults = simUtils.getSingleNaiveSimilarity(books, qryBookId, queryChunk, simType,topKRes, FRConstants.LEAVE_LAST_K_ELEMENTS_OF_FEATURE);
			LOG.debug("for qry chunk = " + qryBookId + " - " + queryChunk.getKey() + " Similar Book chunks are");
			LOG.debug(chunkSimResults);
			staging_results.put(qryBookId + "-" + queryChunk.getKey(), chunkSimResults);// this will
																						// always
																						// return 20
																						// or
			// 10 results per query chunk
		}

		LOG.debug("stg results size =" + staging_results.size());// size = no of chunks of
																			// query book
		// loop over the staging results to create a final weighted result map
		SortedMap<Double, String> sorted_results_wo_TTR = new TreeMap<Double, String>(Collections.reverseOrder());// final DS to hold sorted ranks
		SortedMap<Double, String> sorted_results_mit_TTR = new TreeMap<Double, String>(Collections.reverseOrder());
		
		// Multimap<Double, String> multimap_results = ArrayListMultimap.create();//useful to
		// combine many values for same similarity weight
		Map<String, Double> chunk_results = new TreeMap<>();// useful, this has chunks rolled up,
															// i.e. all occurrences of 'pg547-1'
															// clubbed

		// outer for loop: key = q1, Val = [Map of similar chunks], Key = q2, Val =[Map of similar
		// chunks]
		for (Map.Entry<String, Map<Double, String>> stg_results : staging_results.entrySet()) {
			Map<Double, String> chunk_res = stg_results.getValue(); // this has relevance weights
																	 // for a query chunk
			// below for loop over a specific query chunk: q1
			for (Map.Entry<Double, String> res : chunk_res.entrySet()) {
				// add relevant output in a final results map, Key ="Corpus_Chunk" = bookId_ChunkId,
				// Value = Cumulative_Weights
				if (!chunk_results.containsKey(res.getValue())) // new chunk ('pg547-1') item, just add
					chunk_results.put(res.getValue(), res.getKey());
				else {
					double temp = 0.00;
					temp = chunk_results.get(res.getValue());// get current sim. weight
					if(ROLLUP.equals(FRConstants.SIMI_ROLLUP_BY_ADDTN))
					chunk_results.put(res.getValue(), Math.round((temp + res.getKey()) * 10000.0000) / 10000.0000);// if key present, update the weight
					if(ROLLUP.equals(FRConstants.SIMI_ROLLUP_BY_MULPN))
					chunk_results.put(res.getValue(), Math.round((temp * res.getKey()) * 10000.0000) / 10000.0000);// if key present, update the weight
						
				}
			}
		}

		LOG.debug("chunk results = " + chunk_results); // this will break the number
																 // topK=top20, when it will combine
																 // result chunks

		Map<String, Double> book_results = new TreeMap<>(); // rolled up values per book!
		// roll up from chunks to a corpus book level, i.e. 'pg547-1' , 'pg547-2' ... all clubbed to
		// 'pg547'
		for (Map.Entry<String, Double> stg1 : chunk_results.entrySet()) {
			String book_chunk = stg1.getKey(); // 'pg547-1'
			String bookId = book_chunk.split("-")[0]; // 'pg547'
			double book_weight = 0.00;
			for (Map.Entry<String, Double> stg2 : chunk_results.entrySet()) {
				if (bookId.equals(stg2.getKey().split("-")[0])) { // compare the first part of
																	 // 'pg547-1', i.e. 'pg547'
					book_weight = book_weight + stg2.getValue();// accumulate weights
				}
			}// end of a chunk rolling here
			double noOfChunks = books.get(bookId).size();
			if(PENALISE.equals(FRConstants.SIMI_PENALISE_BY_NOTHING))
				book_results.put(bookId, Math.round((book_weight) * 10000.0000) / 10000.0000);
			if(PENALISE.equals(FRConstants.SIMI_PENALISE_BY_CHUNK_NUMS))
				book_results.put(bookId, Math.round((book_weight/noOfChunks) * 10000.0000) / 10000.0000);
			if(PENALISE.equals(FRConstants.SIMI_PENALISE_BY_CHUNK_SQR_ROOT))
				book_results.put(bookId, Math.round((book_weight/(Math.sqrt(noOfChunks))) * 10000.0000) / 10000.0000);
			//book_results.put(bookId, Math.round((book_weight /noOfChunks) * 10000.0000) / 10000.0000);
		}

		LOG.debug("book results = " + book_results);

		for (Map.Entry<String, Double> unranked_weights : book_results.entrySet()) {
			sorted_results_wo_TTR.put(unranked_weights.getValue(), unranked_weights.getKey()); // this is a reverse sorted tree , by decreasing relevance rank,
			// 3.987-> book6, book67 -> top
			// 2.851-> book5
			// 1.451-> book9, book89 -> lowest rank
		}
		
		if(TTR_CHARS.equals(FRConstants.SIMI_EXCLUDE_TTR_NUMCHARS)){
			LOG.debug("For Chunk based Similarity, QBE Book = " + qryBookId + " printing top " + FRConstants.TOP_K_RESULTS + " results");
			sorted_results_wo_TTR = printTopKResults(sorted_results_wo_TTR);
			return sorted_results_wo_TTR;
		
		}
		else{//i.e. include TTR and Num of characters
			//compose a feature array with 3 elements
			//0. Similarity Relevance score - weight 0.85
			//1. TTR - weight 0.10
			//2. Numbr of Chars - weight 0.05
			
			//Compose a corpus of all books (not chunks) with above 3 dimensional vector
			// find L2 similarity and rank results
			
			Map<String, double[]> global_corpus = new TreeMap<>();
			//create feature vectors below
			for(Map.Entry<Double, String> global_books:sorted_results_wo_TTR.entrySet()){
				double [] global_feature = new double[FRConstants.FEATURE_NUMBER_GLOBAL];
				global_feature[0] = global_books.getKey()*FRConstants.FEATURE_WEIGHT_MORE;
				   for(Map.Entry<String, Map<String, double[]>> input_books: books.entrySet()){
					   if(global_books.getValue().equals(input_books.getKey())){ // match bookId with bookId 
						   Map<String, double[]> chunk_map = input_books.getValue();
						     for(Map.Entry<String, double[]> temp_chunk: chunk_map.entrySet()){
						    	 	 global_feature[1] = temp_chunk.getValue()[FRConstants.TTR_21]*FRConstants.FEATURE_WEIGHT_LESS;
						    	 	 global_feature[2] = temp_chunk.getValue()[FRConstants.NUM_CHARS_20]*FRConstants.FEATURE_WEIGHT_LEAST;
						     }
						   
					   }
				   }
				if(!global_books.getValue().equals(qryBookId))// dont_add_query_vector_which_is_specially_created
				global_corpus.put(global_books.getValue(),global_feature);
			}
			
			//qry_vector = [0.85, 0.10, 0.05]
			double [] global_qry_vector = new double[FRConstants.LEAVE_LAST_K_ELEMENTS_OF_FEATURE+1];
			global_qry_vector[0] = FRConstants.FEATURE_WEIGHT_MORE;
			global_qry_vector[1] = FRConstants.FEATURE_WEIGHT_LESS;
			global_qry_vector[2] = FRConstants.FEATURE_WEIGHT_LEAST;
			
			global_corpus.put(qryBookId, global_qry_vector); // add the global_query to corpus
			
			
			sorted_results_mit_TTR = simUtils.getSingleNaiveSimilarityDummy(global_corpus, qryBookId, FRConstants.TOP_K_RESULTS, FRConstants.SIMILARITY_L2);
			
			LOG.debug("For Global Feature based Similarity, QBE Book = " + qryBookId + " printing top " + FRConstants.TOP_K_RESULTS + " results");
			sorted_results_mit_TTR = printTopKResults(sorted_results_mit_TTR);
			return sorted_results_mit_TTR;
			
		}
		
		
		
		
	}
	
	private static SortedMap<Double, String> printTopKResults(SortedMap<Double, String> sorted_results){
		int count = 0;
		double topWeight = 0;
		SortedMap<Double, String> printed_results = new TreeMap<Double, String>(Collections.reverseOrder());
			for (Map.Entry<Double, String> print_res : sorted_results.entrySet()) {
				count++;
				if (count == 1)
					topWeight = print_res.getKey();
				if (count <= FRConstants.TOP_K_RESULTS) {
					if (count == 1){
						LOG.debug("Rank " + count + " is  Book = " + print_res.getValue() + " weight = 1 ");
						printed_results.put(1.00,  print_res.getValue());
					}
					else{
						LOG.debug("Rank " + count + " is  Book = " + print_res.getValue() + " weight = "
								+ Math.round((print_res.getKey() / topWeight) * 1000.000) / 1000.000);
						printed_results.put(Math.round((print_res.getKey() / topWeight) * 1000.000) / 1000.000,  print_res.getValue());
					}
				}
			}
			return printed_results;
	}

	private static Map<String, Map<String, double[]>> generateChunkFeatureMapForAChunk(String[] instances,
			Map<String, Map<String, double[]>> bookFeatureMap) {
		String bookName = instances[0].split("-")[0];
		String chunkNo = instances[0].split("-")[1];
		double[] feature_array = new double[FRConstants.FEATURE_NUMBER];

		for (int j = 1; j < instances.length; j++) {// start from index 1, skip chunk
													// num
			feature_array[j - 1] = Double.parseDouble(instances[j]);
		}

		Map<String, double[]> chunkFeatureMap = bookFeatureMap.containsKey(bookName) ? bookFeatureMap.get(bookName) : new HashMap<>();
		chunkFeatureMap.put(chunkNo, feature_array);
		bookFeatureMap.put(bookName, chunkFeatureMap);
		return bookFeatureMap;
	}
}
