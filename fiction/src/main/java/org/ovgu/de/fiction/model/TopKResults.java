package org.ovgu.de.fiction.model;

import java.util.Map;
import java.util.SortedMap;
/**
 * @see - Just a holder class to combine various maps, string, double values that a method can return
 * @author Sayantan
 *
 */
public class TopKResults {

	/**
	 * books and  results_topK - for use in returning back search results
	 */
	public Map<String, Map<String, double[]>> books;
	SortedMap<Double, String> results_topK;
	
	/**
	 * Book Name, Book Global Feature Vector, Book Class label, for results interpretation
	 */
	public String bookName;
	public double bookClassLabel;
	public double [] bookGlobalFeatureVector;
	
	public TopKResults() {
		// TODO Auto-generated constructor stub
	}

	public Map<String, Map<String, double[]>> getBooks() {
		return books;
	}

	public void setBooks(Map<String, Map<String, double[]>> books) {
		this.books = books;
	}

	public SortedMap<Double, String> getResults_topK() {
		return results_topK;
	}

	public void setResults_topK(SortedMap<Double, String> results_topK) {
		this.results_topK = results_topK;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public double getBookClassLabel() {
		return bookClassLabel;
	}

	public void setBookClassLabel(double bookClassLabel) {
		this.bookClassLabel = bookClassLabel;
	}

	public double[] getBookGlobalFeatureVector() {
		return bookGlobalFeatureVector;
	}

	public void setBookGlobalFeatureVector(double[] bookGlobalFeatureVector) {
		this.bookGlobalFeatureVector = bookGlobalFeatureVector;
	}
	
	

}
