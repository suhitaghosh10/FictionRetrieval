package org.ovgu.de.fiction.model;

public class Similarity {
	
	private String bookId;
	private double cosineSim;
	private double L1Sim;
	private double L2Sim; // usual Euclidean Sim

	public Similarity() {
		this.bookId="";
		this.cosineSim=0.0;
		this.L1Sim=0.0;
		this.L2Sim=0.0;
	}
	
	

	public String getBookId() {
		return bookId;
	}



	public void setBookId(String bookId) {
		this.bookId = bookId;
	}



	public double getCosineSim() {
		return cosineSim;
	}

	public void setCosineSim(double cosineSim) {
		this.cosineSim = cosineSim;
	}

	public double getL1Sim() {
		return L1Sim;
	}

	public void setL1Sim(double l1Sim) {
		L1Sim = l1Sim;
	}

	public double getL2Sim() {
		return L2Sim;
	}

	public void setL2Sim(double l2Sim) {
		L2Sim = l2Sim;
	}
	
	

}
