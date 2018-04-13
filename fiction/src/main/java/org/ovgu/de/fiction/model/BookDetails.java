package org.ovgu.de.fiction.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import nl.siegmann.epublib.domain.Metadata;

/**
 * @author Suhita
 */
public class BookDetails implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5335435328859228630L;
	private String bookId;
	private List<Chunk> chunks;
	private Metadata metadata;
	private String content;
	private BigDecimal averageTTR;
	
	private int numOfChars; // added by Sayantan, at Book level

	
	/**
	 * @param bookId
	 * @param chunks
	 * @param metadata
	 * @param feature
	 */
	public BookDetails(String bookId, List<Chunk> chunks, Metadata metadata, String content, int numOfChars) {
		super();
		this.bookId = bookId;
		this.chunks = chunks;
		this.metadata = metadata;
		this.content = content;
		this.numOfChars = numOfChars;
	}

	/**
	 * 
	 */
	public BookDetails() {
		super();
	}

	/**
	 * @return the bookId
	 */
	public String getBookId() {
		return bookId;
	}

	/**
	 * @param bookId
	 *            the bookId to set
	 */
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	/**
	 * @return the chunks
	 */
	public List<Chunk> getChunks() {
		return chunks;
	}

	/**
	 * @param chunks
	 *            the chunks to set
	 */
	public void setChunks(List<Chunk> chunks) {
		this.chunks = chunks;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the metadata
	 */
	public Metadata getMetadata() {
		return metadata;
	}

	/**
	 * @param metadata
	 *            the metadata to set
	 */
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	/**
	 * @return the averageTTR
	 */
	public BigDecimal getAverageTTR() {
		return averageTTR;
	}

	/**
	 * @param averageTTR
	 *            the averageTTR to set
	 */
	public void setAverageTTR(BigDecimal averageTTR) {
		this.averageTTR = averageTTR;
	}
	
	

	public int getNumOfChars() {
		return numOfChars;
	}

	public void setNumOfChars(int numOfChars) {
		this.numOfChars = numOfChars;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Book [bookId=" + bookId + " having " + chunks.size() + " chunks => " + chunks + ", metadata=" + metadata.getFirstTitle()
				+ ", averageTTR=" + averageTTR + ", num of Chars="+numOfChars+ "]";
	}

}
