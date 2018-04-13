package org.ovgu.de.fiction.web;

import java.io.Serializable;

import org.primefaces.model.StreamedContent;

public class BookUI implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6721562225650870310L;
	private String id;
	private String name;
	private String genre;
	private int rank;
	private String score;
	private String author;
	private String epubPath;
	private String htmlPath;

	public BookUI() {
		super();
	}

	public BookUI(String id, String name, String genre, int rank, String score, String author, String epubPath,
			String htmlPath) {
		super();
		this.id = id;
		this.name = name;
		this.genre = genre;
		this.rank = rank;
		this.score = score;
		this.author = author;
		this.epubPath = epubPath;
		this.htmlPath = htmlPath;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getEpubPath() {
		return epubPath;
	}

	public void setEpubPath(String epubPath) {
		this.epubPath = epubPath;
	}

	public String getHtmlPath() {
		return htmlPath;
	}

	public void setHtmlPath(String htmlPath) {
		this.htmlPath = htmlPath;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

}
