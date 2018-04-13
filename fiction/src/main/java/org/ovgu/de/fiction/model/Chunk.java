package org.ovgu.de.fiction.model;

import java.io.Serializable;
import java.util.List;

import org.ovgu.de.fiction.utils.FRConstants;

/**
 * @author Suhita
 */
public class Chunk implements Serializable {

	private static final long serialVersionUID = 8973197663800309362L;
	private Integer chunkNo;
	private List<String> tokenListWithoutStopwordAndPunctuation;
	private String chunkFileLocation;

	private Feature feature;

	/**
	 * @return the chunkNo
	 */
	public Integer getChunkNo() {
		return chunkNo;
	}

	/**
	 * @param chunkNo
	 *            the chunkNo to set
	 */
	public void setChunkNo(Integer chunkNo) {
		this.chunkNo = chunkNo;
	}

	/**
	 * @return the tokenListWithoutStopwordAndPunctuation
	 */
	public List<String> getTokenListWithoutStopwordAndPunctuation() {
		return tokenListWithoutStopwordAndPunctuation;
	}

	/**
	 * @param tokenListWithoutStopwordAndPunctuation
	 *            the tokenListWithoutStopwordAndPunctuation to set
	 */
	public void setTokenListWithoutStopwordAndPunctuation(List<String> tokenListWithoutStopwordAndPunctuation) {
		this.tokenListWithoutStopwordAndPunctuation = tokenListWithoutStopwordAndPunctuation;
	}

	/**
	 * @return the chunkFileLocation
	 */
	public String getChunkFileLocation() {
		return chunkFileLocation;
	}

	/**
	 * @param chunkFileLocation
	 *            the chunkFileLocation to set
	 */
	public void setChunkFileLocation(String chunkFileLocation) {
		this.chunkFileLocation = chunkFileLocation;
	}

	/**
	 * @return the feature
	 */
	public Feature getFeature() {
		return feature;
	}

	/**
	 * @param feature
	 *            the feature to set
	 */
	public void setFeature(Feature feature) {
		this.feature = feature;
	}

	/**
	 * @return the textFormat
	 */
	public static String getOriginalText(List<Word> tokens) {
		StringBuffer sbf = new StringBuffer();
		tokens.forEach(t -> sbf.append(t.getOriginal()).append(FRConstants.SPACE));
		return sbf.toString();
	}

	/**
	 * @return the textFormat
	 */
	public static String listToString(List<String> tokens) {
		StringBuffer sbf = new StringBuffer();
		tokens.forEach(t -> sbf.append(t).append(FRConstants.SPACE));
		return sbf.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Chunk [chunkNo=" + chunkNo + ", chunkFileLocation=" + chunkFileLocation + ", feature=" + feature + "]";
	}

}
