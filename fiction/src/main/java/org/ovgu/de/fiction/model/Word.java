package org.ovgu.de.fiction.model;

import java.io.Serializable;

/**
 * @author Suhita
 */
public class Word implements Serializable {

	private static final long serialVersionUID = 4798804135515417547L;
	private String original;
	private String lemma;
	private String pos;
	private String ner;
	private int numOfSyllables;

	/**
	 * @param original
	 * @param lemma
	 * @param pos
	 * @param ner
	 */
	public Word(String original, String lemma, String pos, String ner,int numOfSylbles) {
		super();
		this.original = original;
		this.lemma = lemma;
		this.pos = pos;
		this.ner = ner;
		this.numOfSyllables = numOfSylbles;
	}

	/**
	 * @return the lemma
	 */
	public String getLemma() {
		return lemma;
	}

	/**
	 * @param lemma
	 *            the lemma to set
	 */
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	/**
	 * @return the pos
	 */
	public String getPos() {
		return pos;
	}

	/**
	 * @param pos
	 *            the pos to set
	 */
	public void setPos(String pos) {
		this.pos = pos;
	}

	/**
	 * @return the ner
	 */
	public String getNer() {
		return ner;
	}

	/**
	 * @param ner
	 *            the ner to set
	 */
	public void setNer(String ner) {
		this.ner = ner;
	}

	/**
	 * @return the original
	 */
	public String getOriginal() {
		return original;
	}

	/**
	 * @param original
	 *            the original to set
	 */
	public void setOriginal(String original) {
		this.original = original;
	}
	

	public int getNumOfSyllables() {
		return numOfSyllables;
	}

	public void setNumOfSyllables(int numOfSyllables) {
		this.numOfSyllables = numOfSyllables;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Word [original=" + original + ", lemma=" + lemma + ", pos=" + pos + ", ner=" + ner +" num of Syllables "+numOfSyllables+ "]";
	}

}
