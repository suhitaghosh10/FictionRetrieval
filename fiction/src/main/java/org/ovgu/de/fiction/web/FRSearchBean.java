package org.ovgu.de.fiction.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.maltparser.core.helper.HashSet;
import org.ovgu.de.fiction.model.TopKResults;
import org.ovgu.de.fiction.preprocess.ContentExtractor;
import org.ovgu.de.fiction.search.FictionRetrievalSearch;
import org.ovgu.de.fiction.search.InterpretSearchResults;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRGeneralUtils;
import org.ovgu.de.fiction.utils.FRWebUtils;

@ManagedBean
@ViewScoped
public class FRSearchBean implements Serializable {
	private static final long serialVersionUID = 462006850003220169L;
	
	private static final String WEB_CONTEXT_PATH = "web.ctx.path";
	final static Logger LOG = Logger.getLogger(ContentExtractor.class);
	private Map<String, List<String>> data = new HashMap<String, List<String>>();
	private String genre;
	private String book;
	private String txt;
	private String selBook;
	private Map<String, String> genreMap;
	private List<String> books;
	private boolean shallShowTable;
	private List<BookUI> simBooks;

	/**
	 * The method generates the Genre dropdown on page load
	 */
	@PostConstruct
	public void init() {
		LOG.info("Init !!! ");

		shallShowTable = false;
		genreMap = new HashMap<String, String>();
		genreMap.put("All", "All");
		genreMap.put("Allegories", "Allegories");
		genreMap.put("Christmas Stories", "Christmas Stories");
		genreMap.put("Detective and Mystery", "Detective and Mystery");
		genreMap.put("Ghost and Horror", "Ghost and Horror");
		genreMap.put("Humorous , Wit and Satire", "Humorous , Wit and Satire");
		genreMap.put("Literary", "Literary");
		genreMap.put("Love and Romance", "Love and Romance");
		genreMap.put("Sea and Adventure", "Sea and Adventure");
		genreMap.put("Western Stories", "Western Stories");

		GenrePopulator gp = new GenrePopulator();
		data.put("Literary", gp.generateLiteraryMap());
		data.put("Detective and Mystery", gp.generateDetectiveMap());
		data.put("Western Stories", gp.generateWesternMap());
		data.put("Ghost and Horror", gp.generateGhostMap());
		data.put("Humorous , Wit and Satire", gp.generateHumourMap());
		data.put("Christmas Stories", gp.generateChristmasMap());
		data.put("Love and Romance", gp.generateLoveMap());
		data.put("Sea and Adventure", gp.generateSeaMap());
		data.put("Allegories", gp.generateAllegoryMap());
		data.put("All", gp.generateAll());
	}

	/**
	 * The method generates book dropdown ,after the genre is selected from dropdown
	 */
	public void onGenreChange() {
		if (genre != null && !genre.equals(""))
			books = data.get(genre);
		else
			books = data.get("All");

		this.book = null;
	}

	/**
	 * @throws Exception
	 */
	public void displayBook() throws Exception {

		if (genre == null || genre.trim().equals("")) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, null, "Please select a Genre"));
			return;
		}

		if (book == null || book.trim().equals("")) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, null, "Please select a Book"));
			return;
		}

		if (book != null && genre != null) {
			shallShowTable = true;

			String similarity = "L2";

			FRWebUtils utils = new FRWebUtils();
			Map<String, String> book_master = utils.getAllMasterBooks(); // key = bookId, Value = Book_Name
			String qryBookId = utils.getMasterBookId(book_master, book);
			String FEATURE_CSV_FILE = FRGeneralUtils.getPropertyVal("file.feature");

			Map<String, Map<String, String>> stats_Of_results = new HashMap<>();

			if (!qryBookId.equals("")) {
				int TOP_K = Integer.parseInt(FRGeneralUtils.getPropertyVal("topk.count"));
				TopKResults topKResults = FictionRetrievalSearch.findRelevantBooks(qryBookId, FEATURE_CSV_FILE,
						FRConstants.SIMI_PENALISE_BY_NOTHING, FRConstants.SIMI_ROLLUP_BY_ADDTN,
						FRConstants.SIMI_EXCLUDE_TTR_NUMCHARS, TOP_K, similarity);

				InterpretSearchResults interp = new InterpretSearchResults();

				try {
					stats_Of_results = interp.performStatiscalAnalysis(topKResults);
				} catch (Exception e) {
					throw new Exception("analysis cannot be done!");
				}

				int rank = 0;
				simBooks = new ArrayList<>();
				for (Map.Entry<Double, String> res : topKResults.getResults_topK().entrySet()) {

					String[] bookArr = utils.getMasterBookName(book_master, String.valueOf(res.getValue())).split("#");
					if (bookArr.length < 2)
						continue;

					String bookName = bookArr[0];
					bookName = bookName.contains("|") ? bookName.substring(bookName.indexOf("#") + 1).replace("|", ",")
							: bookName.substring(bookName.indexOf("#") + 1);
					String bookId = utils.getMasterBookId(book_master, bookName);

					if (bookId.equals(qryBookId))
						continue;

					rank++;

					if (rank == TOP_K + 1)
						break;
					String authName = bookArr[1].contains("|") ? bookArr[1].replace("|", ",") : bookArr[1];

					BookUI book = new BookUI();
					book.setId(bookId);
					book.setName(bookName);
					book.setAuthor(authName);
					book.setRank(rank);
					StringBuffer sbf = new StringBuffer(FRGeneralUtils.getPropertyVal(WEB_CONTEXT_PATH));
					sbf.append("epub/").append(bookId).append(".epub");
					book.setEpubPath(sbf.toString());
					book.setScore(String.valueOf(res.getKey()));

					simBooks.add(book);

				}

				LOG.debug("books added " + simBooks.size());

				if (stats_Of_results.size() > 0) {
					Map<String, String> reduced_features = new HashMap<>();

					reduced_features = stats_Of_results.get("FEAT");
					StringBuffer reducedFe = new StringBuffer(
							"Some important factors responsible for the list obtained below : ");

					Set<String> ftrSet = new HashSet<>();
					if (reduced_features.size() > 0) {
						for (Map.Entry<String, String> reduced_fe : reduced_features.entrySet()) {
							if (reduced_fe.getKey().startsWith("Feature")) {
								ftrSet.addAll(FRWebUtils.getFeatureHighLevelName(reduced_fe.getValue()));
							}
						}
					}

					for (String s : ftrSet) {
						reducedFe.append(s).append(" ,");
					}
					reducedFe.deleteCharAt(reducedFe.length() - 1);
					FacesMessage msg = new FacesMessage("Analysis could not be done");

					if (reducedFe != null) {
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, null, reducedFe.toString());
					}
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}

		}
	}

	/**
	 * @param query
	 * @return
	 * 
	 * 		The method for completion of text for book dropdown
	 */
	public List<String> completeText(String query) {
		LOG.debug("query" + query);
		List<String> allBooks = books;
		List<String> filtered = new ArrayList<>();
		for (String val : books) {
			if (val.toLowerCase().contains(query.toLowerCase())) {
				filtered.add(val);
			}
		}
		LOG.debug(filtered.size() + " filtered");
		return filtered;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getBook() {
		return book;
	}

	public void setBook(String book) {
		this.book = book;
	}

	public Map<String, String> getGenreMap() {
		return genreMap;
	}

	public void setGenreMap(Map<String, String> genreMap) {
		this.genreMap = genreMap;
	}

	public List<String> getBooks() {
		return books;
	}

	public void setBooks(List<String> books) {
		this.books = books;
	}

	public void setData(Map<String, List<String>> data) {
		this.data = data;
	}

	public String getTxt() {
		return txt;
	}

	public void setTxt(String txt) {
		this.txt = txt;
	}

	public String getSelBook() {
		return selBook;
	}

	public void setSelBook(String selBook) {
		this.selBook = selBook;
	}

	public boolean isShallShowTable() {
		return shallShowTable;
	}

	public void setShallShowTable(boolean shallShowTable) {
		this.shallShowTable = shallShowTable;
	}

	public List<BookUI> getSimBooks() {
		return simBooks;
	}

	public void setSimBooks(List<BookUI> simBooks) {
		this.simBooks = simBooks;
	}

	public Map<String, List<String>> getData() {
		return data;
	}

}