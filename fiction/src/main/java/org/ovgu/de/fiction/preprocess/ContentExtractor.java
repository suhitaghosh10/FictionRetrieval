package org.ovgu.de.fiction.preprocess;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRGeneralUtils;
import org.ovgu.de.fiction.utils.FRFileOperationUtils;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;

/**
 * @author Suhita 
 */

/**
 * This is a helper class for preprocessing of epub files' content.
 */
public class ContentExtractor {

	final static Logger LOG = Logger.getLogger(ContentExtractor.class);

	private static String OUT_FOLDER;

	/**
	 * @throws IOException
	 *             The method initialises parameters needed for generation of content
	 */
	public static void init() throws IOException {
		OUT_FOLDER = FRGeneralUtils.getPropertyVal(FRConstants.OUT_FOLDER_LOCATION);
	}

	/**
	 * @throws IOException
	 *             The method generates the content from all epubs in corpus
	 */
	public static void generateContentFromAllEpubs() throws IOException {

		init();
		String outFolder = FRGeneralUtils.getPropertyVal(FRConstants.EPUB_FOLDER);
		FRFileOperationUtils.getFileNames(outFolder).parallelStream().forEach(file -> {
			try {
				generateContentFromEpub(file);
			} catch (IOException e) {
				LOG.error("Error processing Epubs -" + e.getMessage());
			}

		});
	}

	/**
	 * @param path
	 * @return The method preprocess the epub content and generates the final content for further
	 *         analysis. Two output file generated.One - <epubfilename>-content.html contains the
	 *         preprocessed content. Second - <epubfilename>-FULL.html contains the whole text in
	 *         html format.
	 * @throws IOException
	 */
	public static String generateContentFromEpub(Path path) throws IOException {
		init();
		EpubReader epubReader = new EpubReader();
		Book book;
		book = epubReader.readEpub(new FileInputStream(path.toString()));
		String fileName = path.getFileName().toString().replace(FRConstants.EPUB_EXTN, "");
		String content = generateTotalHtmlContent(book, fileName);
		return generateContentToBeAnalysed(book, fileName, content);
	}

	/**
	 * @param book
	 * @param fileName
	 * @param content
	 * @return
	 * @throws IOException
	 */
	private static String generateContentToBeAnalysed(Book book, String fileName, String content) throws IOException {

		StringBuffer finalContent = new StringBuffer();
		try (Writer contentWtr = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(OUT_FOLDER + fileName + FRConstants.CONTENT_FILE)));) {
			Document doc = Jsoup.parse(content);
			LOG.debug("Generating content for - " + book.getTitle());

			/* The actual content is inside <p> tags */
			List<Element> p = doc.getElementsByTag("p");
			p.remove(!p.isEmpty());

			/* The title,contents might be present inside <h> tags or <td> */
			List<String> headerList = getHeaders(fileName, doc);

			out: for (Element elem : p) {

				String text = elem.text();

				/* break if the text has reached end of story */
				if (Pattern.matches(FRConstants.REGEX_GUTEN_END, text.toLowerCase()) || text.toLowerCase().contains(FRConstants.GUTEN_14) || text.toLowerCase().startsWith("the end")
						|| text.toLowerCase().equals("footnotes")) {
					break out;
				}

				if (isValidContent(elem)) {
					int length = text.split(FRConstants.REGEX_SPLIT_ON_PERIOD).length;

					/* check if the text is not a header or metadata */
					if (!elem.text().contains(FRConstants.CHAPTER_CAPS) || (length<5 && !isContentContainedInHeaderAndMetadata(elem, headerList, book.getMetadata()))) {
						contentWtr.write(FRConstants.P_TAG + elem.text());
						contentWtr.write(FRConstants.NEW_LINE);
						finalContent.append(elem.text()).append(FRConstants.NEW_LINE);
					}
				}
			}
		} catch (Exception excp) {
			LOG.error("Exception occurred while generating content for " + fileName + " - " + excp.getMessage());
		}
		String finalContentStr = finalContent.toString();
		//writeToFile(fileName, finalContentStr);
		return finalContentStr;
	}

	private static String generateTotalHtmlContent(Book book, String fileName) {
		StringBuffer content = new StringBuffer();

		book.getSpine().getSpineReferences().forEach(s -> {
			try {
				content.append(new String(s.getResource().getData())).append(FRConstants.NEW_LINE);
				if (FRGeneralUtils.getPropertyVal(FRConstants.WRITE_TOTAL_CONTENT_CONFIG).equals(FRConstants.TRUE))
					writeTotalHtmlContent(s, fileName);
			} catch (IOException e) {
				LOG.error("Exception while writing full content");
			}
		});
		return content.toString().replace("—", " - ").replaceAll("[“”]", "\"").replaceAll("[()]", " ").replaceAll("[`‘’]", "'");
	}

	/**
	 * @param sref
	 * @param fileName
	 *            The method writes the entire content in HTML format to a file
	 */
	private static void writeTotalHtmlContent(SpineReference sref, String fileName) {
		try (Writer fullContentHTML = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(OUT_FOLDER + fileName + FRConstants.FULL_HTML)));) {
			fullContentHTML.write(new String(sref.getResource().getData()));
			fullContentHTML.write(FRConstants.NEW_LINE);
		} catch (IOException e1) {
			LOG.error("File could not be written");
		}
	}

	/**
	 * @param invalidContent
	 * @param elem
	 * @return The method check whether the text is valid,by- i) checking the style associated with
	 *         the <p> tags ii) if the content inside the <p> was nested inside other tags like
	 *         -<div> iii) metadata,footnote,Gutenberg associated content and others
	 */
	private static boolean isValidContent(Element elem) {
		String lowerCase = elem.text().toLowerCase();
		if (lowerCase.isEmpty())
			return false;

		return !(elem.hasClass("t0") || elem.hasClass("l") || elem.hasClass("tw") || elem.hasClass("t") || elem.hasClass("t2")
				|| elem.hasClass("t3") || elem.hasClass("t4") || elem.hasClass("t5") || elem.hasClass("t6") || elem.hasClass("t7")
				|| elem.hasClass("t8") || elem.hasClass("t9") || elem.hasClass("t10") || elem.hasClass("t11") || elem.hasClass("t12")
				|| elem.hasClass("t13") || elem.hasClass("t14") || elem.hasClass("t15") || elem.hasClass("csmaller")
				|| elem.hasClass("bkad") || elem.hasClass("bkpr") || elem.hasClass("bkrv") || elem.hasClass("bq") || elem.hasClass("tb")
				|| elem.hasClass("noindent") || elem.hasClass("tbcenter") || elem.hasClass("pb") || elem.hasClass("fncont")
				|| elem.hasClass("fnbq") || elem.hasClass("caption") || elem.hasClass("center") || elem.hasClass("titlepg")
				|| elem.hasClass("dbox") || elem.hasClass("nbox") || elem.hasClass("small") || elem.hasClass("smaller")
				|| elem.hasClass("smallest") || elem.hasClass("larger") || elem.hasClass("larger") || elem.hasClass("large")
				|| elem.hasClass("gs") || elem.hasClass("gs3") || elem.hasClass("gslarge") || elem.hasClass("sc") || elem.hasClass("ss")
				|| elem.hasClass("rubric") || elem.hasClass("biblio") || elem.hasClass("c") || elem.hasClass("cb") || elem.hasClass("hang2")
				|| elem.hasClass("foot") || elem.hasClass("hang1") || elem.hasClass("pginternal") || elem.hasClass("illustration")
				|| elem.hasClass("chapter") || elem.hasClass("pbooklist") || elem.hasClass("toc"))
				&& !Character.isDigit(lowerCase.charAt(0)) && !lowerCase.contains(FRConstants.TRANSCRIBE)
				&& !lowerCase.contains(FRConstants.GUTEN_EBOOK) && !lowerCase.contains(FRConstants.GUTEN_PRJCT)
				&& !lowerCase.contains(FRConstants.GUTEN_LICENSE) && !lowerCase.contains(FRConstants.REPRINTED)
				&& !lowerCase.contains(FRConstants.COPYWRIGHT) && !lowerCase.contains(FRConstants.PERMISSION_TEXT)
				&& !lowerCase.contains(FRConstants.RSRV_TXT) && !lowerCase.contains(FRConstants.METADATA_TITLE)
				&& !lowerCase.contains(FRConstants.METADATA_AUTHR) && !lowerCase.contains(FRConstants.METADATA_CE)
				&& !lowerCase.contains(FRConstants.METADATA_DATE) && !lowerCase.contains(FRConstants.METADATA_LNG)
				&& !lowerCase.contains(FRConstants.METADATA_1ST_RLS) && !lowerCase.contains(FRConstants.EDITION)
				&& !lowerCase.startsWith(FRConstants.AUTHOR_OF) && !lowerCase.startsWith(FRConstants.PRINTED)
				&& !lowerCase.contains(FRConstants.EMAIL_CHAR) && !lowerCase.contains(FRConstants.ORG)
				&& !lowerCase.contains(FRConstants.STAR_CHAR) && !lowerCase.startsWith(FRConstants.GUTEN_1)
				&& !lowerCase.startsWith(FRConstants.GUTEN_2) && !lowerCase.startsWith(FRConstants.GUTEN_3)
				&& !lowerCase.startsWith(FRConstants.GUTEN_4) && !lowerCase.contains(FRConstants.GUTEN_5)
				&& !lowerCase.startsWith(FRConstants.GUTEN_6) && !lowerCase.startsWith(FRConstants.GUTEN_7)
				&& !lowerCase.startsWith(FRConstants.GUTEN_8) && !lowerCase.startsWith(FRConstants.GUTEN_9)
				&& !lowerCase.startsWith(FRConstants.GUTEN_10) && !lowerCase.startsWith(FRConstants.PREFACE)
				&& !lowerCase.startsWith(FRConstants.GUTEN_11) && !lowerCase.startsWith(FRConstants.GUTEN_12)
				&& !lowerCase.startsWith(FRConstants.GUTEN_13) && !lowerCase.equals(FRConstants.TO)
				&& !elem.text().contains(FRConstants.ILLUSTRATED) && !elem.text().contains(FRConstants.TRANSCRIBER_NOTES)
				&& !Pattern.matches(FRConstants.REGEX_FOOTNOTES, lowerCase) && !(lowerCase.contains("[") && lowerCase.contains("]"))
				&& !FRGeneralUtils.isUpperCase(elem.text());
	}

	/**
	 * @param doc
	 * @return
	 * @throws IOException
	 *             The method returns content inside header <h> and table <td> tags
	 */
	private static List<String> getHeaders(String fileName, Document doc) throws IOException {

		LOG.debug("Headers for -" + fileName);
		Elements headers = doc.getElementsByTag("h1");
		headers.addAll(doc.getElementsByTag("h2"));
		headers.addAll(doc.getElementsByTag("h3"));

		List<String> headerList = new ArrayList<>();

		headers.forEach(h -> {
			headerList.add(h.text());
			LOG.debug(h.text());
		});
		return headerList;
	}

	/**
	 * @param text
	 * @param headerList
	 * @param metadata
	 * @return The method checks whether the content inside <p> is contained in header and other
	 *         metadata.If so then it wont be included in the content
	 */
	private static boolean isContentContainedInHeaderAndMetadata(Element elem, List<String> headerList, Metadata metadata) {
		String txt = elem.text().toLowerCase();
		Author author = metadata.getAuthors().size() > 0 ? metadata.getAuthors().get(0) : null;

		if (author != null && (txt.contains(author.getFirstname().toLowerCase()) && txt.contains(author.getLastname().toLowerCase())))
			return true;
		else if (txt.contains(metadata.getFirstTitle().toLowerCase()))
			return true;
		else if (elem.children().toString().contains(FRConstants.A_HREF) && !elem.children().toString().contains(FRConstants.A_ID))
			return true;
		else if (elem.text().startsWith(FRConstants.CHAPTER) || txt.equals(FRConstants.CONTENTS))
			return true;
		return headerList.parallelStream().anyMatch(h -> h.toLowerCase().contains(txt));
	}

}