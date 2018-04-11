package org.ovgu.de.fiction.search;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.ovgu.de.fiction.feature.extraction.FeatureExtractorUtility;
import org.ovgu.de.fiction.model.TopKResults;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRGeneralUtils;

import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.NumericToNominal;


public class InterpretSearchResults {

	public static  double SIMIL_TOPMATCH  = 0.70;//Deafult_VALUES_that_get_changed
	public static  double SIMIL_FAIRMATCH = 0.40;
	final static Logger LOG = Logger.getLogger(InterpretSearchResults.class);
	
	public InterpretSearchResults() {
		
	}
/**
 * @see - Create a instance-feature space with binning of values to get 5 'pseudo class lables' or 'pseudo clusters' 
 *  
 * Find out over only the result space:
 *  1) Features that are highly correlated with the class labels
 *  2) Feature Selection - Rank features in their discriminating power with regard to class labels, using Mutual Information, Entropy
 *  3) 
 *  
 *  Find out over entire space:
 *  3) Perform PCA of the entire Feature-Instance space to rank features
 * @param topKResults
 * @throws Exception 
 */
	public Map<String,Map<String,String>> performStatiscalAnalysis(TopKResults topKResults) throws Exception {
		Map<String, Map<String, double[]>> books = topKResults.getBooks();
		SortedMap<Double, String> results_topK = topKResults.getResults_topK();
		
		Map<Integer , TopKResults> searched_result_bins = createBinsModified(books,results_topK); // createBins(books,results_topK);
		writeBinsToFiles(searched_result_bins);
		Map<String,Map<String,String>> stats = getStatistics(FRGeneralUtils.getPropertyVal("file.results.arff"));
		//rankFeatures(FRGeneralUtils.getPropertyVal("file.results.arff"));
		return stats;
		
	}
	
	private Map<String,Map<String,String>> getStatistics(String ARFF_RESULTS_FILE) throws Exception {
		Map<String,Map<String,String>> stats = new HashMap<>();
		DataSource source = new DataSource(ARFF_RESULTS_FILE);
		Instances instances = source.getDataSet();
		instances.setClassIndex(instances.numAttributes()-1);
		
		NumericToNominal convert= new NumericToNominal();
        String[] options= new String[2];
        options[0]="-R";
        options[1]="first-last";  //range of variables to make numeric

        convert.setOptions(options);
        convert.setInputFormat(instances);

        Instances newData=Filter.useFilter(instances, convert);
        LOG.debug(newData.toSummaryString());

        
        //Map<String,String> correlations = findCorrelations(instances); //Key=Attribute, Value = Correlation with Class labels
        Map<String,String> important_features= featureSelection(newData);//FE1,FE2,FE3, ACCURACY
        stats.put("CORR", null); // this is not used in jsf
        stats.put("FEAT", important_features);
        return stats;
		
	
}
	private Map<String,String> featureSelection(Instances instances) throws Exception {
		Map<String,String> reduced_features = new HashMap<>();
		instances.deleteAttributeAt(0); //delete primary key
		
		/*
		AttributeSelection filter1 = new AttributeSelection(); // create and initiate a new AttributeSelection instance
		CfsSubsetEval eval1 = new CfsSubsetEval();
		GreedyStepwise search1 = new GreedyStepwise();
		search1.setNumToSelect(5);
		search1.setSearchBackwards(true);
		filter1.setEvaluator(eval1);
		filter1.setSearch(search1);
		filter1.setInputFormat(instances);
		Instances newData1 = Filter.useFilter(instances, filter1);
		System.out.println("Reduced Dimensionality 1 ="+newData1.toSummaryString());
		*/
		
		AttributeSelection filter2 = new AttributeSelection(); // create and initiate a new AttributeSelection instance
		InfoGainAttributeEval eval2 = new InfoGainAttributeEval();
		Ranker search2 = new Ranker();
		search2.setNumToSelect(4);
		search2.setGenerateRanking(true);
		//search2.getThreshold();
		//search2.setThreshold(0.1);
		filter2.setEvaluator(eval2);
		filter2.setSearch(search2);
		filter2.setInputFormat(instances);
	    Instances newData2 = Filter.useFilter(instances, filter2);
	    LOG.debug("** Printing Top Features ***************");
	    
	    LOG.debug("1st = "+newData2.attribute(0).toString().split(" ")[1]);
	    LOG.debug("2nd = "+newData2.attribute(1).toString().split(" ")[1]);
	    LOG.debug("3rd = "+newData2.attribute(2).toString().split(" ")[1]);
	    
	   
	    reduced_features.put("Feature1", newData2.attribute(0).toString().split(" ")[1]);
	    reduced_features.put("Feature2", newData2.attribute(1).toString().split(" ")[1]);
	    reduced_features.put("Feature3", newData2.attribute(2).toString().split(" ")[1]);
	   
		return reduced_features;
		
		
	}
	@SuppressWarnings("unused")
	private Map<String,String> findCorrelations(Instances instances) throws Exception {
		//check correlation of each attribute with class label
		Map<String,String> correlations = new HashMap<>(); //Key=Attribute, Value = Correlation
				for(int k=1;k<instances.numAttributes()-1;k++){ //leave the first attribute = primary key
					double correlation_val=0;
					CorrelationAttributeEval corr = new CorrelationAttributeEval();
					corr.buildEvaluator(instances);
					correlation_val = corr.evaluateAttribute(k);
					if(correlation_val>=0.5 || correlation_val<=-0.5){
					//System.out.println("Correl ="+Math.round(correlation_val*1000.000)/1000.000+" for attrib ="+instances.attribute(k));
					correlations.put(String.valueOf(instances.attribute(k).toString().split(" ")[1]), String.valueOf(Math.round(correlation_val*1000.000)/1000.000));
					}
				}
		return correlations;
		
	}
	
	/**
	 * @about This method will simply write the instance-feature space to arff files for machine learning
	 */
	private void writeBinsToFiles(Map<Integer, TopKResults> searched_result_bins) throws IOException {
	
		double dummy = 10000.0000;
		String RESULTS_CSV_FILE = FRGeneralUtils.getPropertyVal("file.results.csv");
		String RESULTS_ARFF_FILE = FRGeneralUtils.getPropertyVal("file.results.arff");
		try (FileWriter fileWriter = new FileWriter(RESULTS_CSV_FILE);) {

			fileWriter.append(FRConstants.FILE_HEADER_RES_CSV.toString());
			fileWriter.append(FRConstants.NEW_LINE);
			
			for (Map.Entry<Integer, TopKResults> book_features : searched_result_bins.entrySet()) {
				TopKResults topResults = book_features.getValue();
				int rank = book_features.getKey();
				fileWriter.append(topResults.getBookName()+"-"+String.valueOf(rank) + FRConstants.COMMA); //bookID-row_num
				double[] book_vector = topResults.getBookGlobalFeatureVector();
				  for(int k=0;k<book_vector.length;k++){
					  fileWriter.append(String.format("%.4f", Math.round((book_vector[k])* dummy) / dummy) + FRConstants.COMMA);
					  }
				fileWriter.append(String.format("%.4f", Math.round((topResults.getBookClassLabel())* dummy) / dummy) + FRConstants.NEW_LINE);
				  }
			}
		
		FeatureExtractorUtility.writeCSVtoARFF(RESULTS_CSV_FILE,RESULTS_ARFF_FILE);
		}
	

	/**
	 *  three fixed bins are created with dynamic bounds based on similarity weight data distribution (skew center, left or right)
	 * @param books
	 * @param results_topK
	 * @return 
	 */
	private Map<Integer, TopKResults> createBins(Map<String, Map<String, double[]>> books, SortedMap<Double, String> results_topK) {
	TopKResults staging_results = null;
	Map<Integer , TopKResults> searched_result_bins = new HashMap<>();
	int rank=0;
	
	
	String distrib = checkNaiveDataDistribution(results_topK);
	LOG.debug("Weight Data Distrib = "+distrib);
	
		for(Map.Entry<Double, String> search_results:results_topK.entrySet()){
			rank++;
			String bookName = search_results.getValue();
			double bookWt   = search_results.getKey();
			staging_results = new TopKResults();
			double [] book_global_feature_vector = new double[FRConstants.FEATURE_NUMBER];
			int num_Of_chunks=0;
			 for(Map.Entry<String, Map<String, double[]>> corpus: books.entrySet()){
				 if(corpus.getKey().equals(bookName)){
					 	for(Map.Entry<String, double[]> chunks: corpus.getValue().entrySet()){//loop_over_chunks
					 		num_Of_chunks++; // to get average out all chunks of this book
					 		double [] chunk_vector = chunks.getValue();
					 		 for(int i=0;i<chunk_vector.length;i++){
					 			book_global_feature_vector[i] = book_global_feature_vector[i]+chunk_vector[i];
					 		 }
					 	}
				 }
			 }
			 for(int j=0;j<book_global_feature_vector.length;j++){//average out over number of chunks
				 book_global_feature_vector[j]=book_global_feature_vector[j]/num_Of_chunks;
			 }
			 if(bookWt>=SIMIL_TOPMATCH)// based_on_similarity_weight_distribution
			 staging_results.setBookClassLabel(FRConstants.SIMIL_TOPMATCH_CLASS);//assign class label
			 
			 if(bookWt>=SIMIL_FAIRMATCH && bookWt<SIMIL_TOPMATCH)
				 staging_results.setBookClassLabel(FRConstants.SIMIL_FAIRMATCH_CLASS);
			 
			 if(bookWt<SIMIL_FAIRMATCH)
				 staging_results.setBookClassLabel(FRConstants.SIMIL_POORMATCH_CLASS);
			 
			 staging_results.setBookGlobalFeatureVector(book_global_feature_vector);
			 staging_results.setBookName(bookName);
			 searched_result_bins.put(rank, staging_results);
			 
		}
		
		return searched_result_bins;
	
}
	private Map<Integer, TopKResults> createBinsModified(Map<String, Map<String, double[]>> books, SortedMap<Double, String> results_topK) {
	TopKResults staging_results = null;
	Map<Integer , TopKResults> searched_result_bins = new HashMap<>();
	int rank=0;
	
	//create global book vector and add class labels - for top k results
	 for(Map.Entry<String, Map<String, double[]>> corpus: books.entrySet()){ // loop over all books of corpus
		 double [] book_global_feature_vector = new double[FRConstants.FEATURE_NUMBER]; // create a global feature vector for a single book
		 String bookName = corpus.getKey();
		 rank++;
		 staging_results = new TopKResults();
		 Map<String, double[]> bookChunks =  corpus.getValue(); // get all chunks of a given book
			 	for(Map.Entry<String, double[]> chunks: bookChunks.entrySet()){//loop_over_all_chunks_of_a_given_book
			 		double [] chunk_vector = chunks.getValue();
			 		 for(int i=0;i<chunk_vector.length;i++){
			 			book_global_feature_vector[i] = book_global_feature_vector[i]+chunk_vector[i];
			 		 }
			 	}
			 	for(int j=0;j<book_global_feature_vector.length;j++){//average out over number of chunks
					 book_global_feature_vector[j]=book_global_feature_vector[j]/bookChunks.entrySet().size();
				 }
		 if((results_topK.containsValue(bookName)))	
		 staging_results.setBookClassLabel(FRConstants.SIMIL_TOPMATCH_CLASS);//assign class label_for_topK
		 else
		 staging_results.setBookClassLabel(FRConstants.SIMIL_POORMATCH_CLASS);//assign class label_non_match	
		 
		 staging_results.setBookGlobalFeatureVector(book_global_feature_vector);
		 staging_results.setBookName(bookName);
		 searched_result_bins.put(rank, staging_results);
		 
	 }
		
		return searched_result_bins;
	
}
	
	private String checkNaiveDataDistribution(SortedMap<Double, String> results_topK) {
		String data_distrib = FRConstants.DATA_DISTRIB_AT_CENTR;
		double maxWt = results_topK.firstKey();
		double minWt = results_topK.lastKey();
		double data_points = results_topK.size();
		double stand_Dev =0.0;
		double mean =0.0;
		double median=0;
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for( Map.Entry<Double, String> res: results_topK.entrySet()) {
	        stats.addValue(res.getKey());
		}
		stand_Dev = stats.getStandardDeviation();
		mean = stats.getMean();
		median = stats.getPercentile(50);

		
		double center_skew_count=0;
		double skew_1_count=0;
		double skew_0_count=0;
		
		for(Map.Entry<Double, String> res: results_topK.entrySet()){
			double val = res.getKey();
			if(median-(stand_Dev*0.5)<=val && val<=median+(0.5*stand_Dev)){
				center_skew_count++;
			}
			if(maxWt-(0.5*stand_Dev)<=val && val<=maxWt){
				skew_1_count++;
			}
			if(minWt<=val && val<=minWt+(0.5*stand_Dev)){
				skew_0_count++;
			}
		}
		LOG.debug("center skew count ="+center_skew_count+" skew1 count ="+skew_1_count+" skew_0_count ="+skew_0_count+" StD_DEV ="+stand_Dev+" mean ="+mean+" median ="+median);
		
		
		//case1: MAX SKEW: around 40% data points around 2 SD of MAX, set data_distrib = MAX, bins {TOP>0.9, FAIR between 0.7-0.9, POOR below 0.7}
		if((Math.abs(skew_1_count/data_points)-FRConstants.DATA_DISTRIB_40_PERCENT)>FRConstants.DATA_DISTRIB_DIFFER_CUTOFF){
			SIMIL_TOPMATCH  = 0.90;//0.9 and above is top
			SIMIL_FAIRMATCH = 0.70;//0.7-0.9 is fair
			data_distrib = FRConstants.DATA_DISTRIB_SKEW_1;
			return data_distrib;
		}
		//case2: MIN SKEW: around 40-50% data points around 2 SD of MIN, set data_distrib = MIN, bins {TOP>0.70, FAIR between 0.15-0.70, POOR below 0.15}
		if((Math.abs(skew_0_count/data_points)-FRConstants.DATA_DISTRIB_40_PERCENT)>FRConstants.DATA_DISTRIB_DIFFER_CUTOFF){
			SIMIL_TOPMATCH  = 0.90;//0.75 and above is top
			SIMIL_FAIRMATCH = 0.70;//0.15-0.75 is fair
			data_distrib = FRConstants.DATA_DISTRIB_SKEW_0;
			return data_distrib;
		}
		else{//the deafult case, we do not need to check!
			//case3: CENTER SKEW: around 50% data points around 2 SD of mean, set data_distrib = center, bins {TOP>0.75, FAIR between 0.25-0.75, POOR below 0.25}
			//if((Math.abs(center_skew_count/data_points)-FRConstants.DATA_DISTRIB_50_PERCENT)>FRConstants.DATA_DISTRIB_DIFFER_CUTOFF){
			SIMIL_TOPMATCH  = 0.90;//0.75 and above is top
			SIMIL_FAIRMATCH = 0.70;//0.25-0.75 is fair
			data_distrib = FRConstants.DATA_DISTRIB_AT_CENTR;
			return data_distrib;
		}
	}
	
	
	public void loadAndPrintRawData() throws Exception{
		String FEATURE_ARFF_FILE = FRGeneralUtils.getPropertyVal("file.wekafeature");
		DataSource source = new DataSource(FEATURE_ARFF_FILE);
		Instances instances = source.getDataSet();
		instances.setClassIndex(instances.numAttributes()-1);
		LOG.debug(instances.toSummaryString());
	}

}
