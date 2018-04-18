package org.ovgu.de.fiction.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FRWebUtils {

	private static final String VOCABULARY_RICHNESS = "lexical richness";
	private static final String PLOT_OF_COMPLEXITY = "complexity of plot";
	private static final String EASE_OF_READABILITY = "ease of readability";
	private static final String OVERALL_SENTIMENT = "overall sentiment of the story";
	private static final String RURAL_AND_URBAN_SETTING = "rural and urban setting";
	private static final String SENTENCE_COMPLEXITY = "sentence complexity";
	private static final String MALE_ORIENTED = "male oriented";
	private static final String FEMALE_ORIENTED = "female oriented";
	private static final String WRITING_STYLE = "writing style";

	public Map<Integer, String> getAllMasterGenres() throws IOException {

		String line = "";
		Map<Integer, String> genres = new HashMap<>();
		int csvRow = 0;
		try (BufferedReader br = new BufferedReader(
				new FileReader(FRGeneralUtils.getPropertyVal(FRConstants.GENRE_CSV)));) {

			while ((line = br.readLine()) != null) {
				csvRow++;
				line.replace("Fiction||", "");
				line.replace("Fiction", "");
				genres.put(csvRow, line); // no header in genre csv
			}

		}

		return genres;
	}

	public Map<String, String> getAllMasterBooks() throws IOException {

		String BookmasterCSVFile = FRGeneralUtils.getPropertyVal("book.list.csv.loc");
		String line = "";
		Map<String, String> book_map = new HashMap<>();
		int csvRow = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(BookmasterCSVFile));) {
			while ((line = br.readLine()) != null) {
				if (csvRow > 0) {
					String[] bookName = line.split(";pg");
					int bookIDStart = line.indexOf(";pg");
					int bookIDEnd = line.indexOf(".epub");
					int auth_start_index = line.lastIndexOf(";");
					String bookID = line.substring(bookIDStart + 1, bookIDEnd);
					String author = line.substring(auth_start_index + 1);
					// ignore header in csv
					book_map.put(bookID, bookName[0] + "#" + author);
				}
				csvRow++;
			}
		}
		return book_map;
	}

	public String getMasterBookName(Map<String, String> book_master, String bookId) {
		String bookName = "";
		for (Map.Entry<String, String> res : book_master.entrySet()) {
			if (res.getKey().equalsIgnoreCase(bookId)) {
				bookName = res.getValue();
				return bookName;
			}
		}
		return bookName;
	}

	public String getMasterBookId(Map<String, String> book_master, String bookName) {
		String book_Id = "";
		for (Map.Entry<String, String> res : book_master.entrySet()) {
			if (res.getValue().split("#")[0].equalsIgnoreCase(bookName)) {
				book_Id = res.getKey();
				return book_Id;
			}
		}
		return book_Id;
	}

	public static String getHighLevelFeatures(Map<String, String> reduced_features) {
		Set<String> ftrSet = new HashSet<>();
		if (reduced_features.size() > 0) {
			for (Map.Entry<String, String> reduced_fe : reduced_features.entrySet()) {
				if (reduced_fe.getKey().startsWith("Feature")) {
					ftrSet.add(getFeatureHighLevelName(reduced_fe.getValue()));
					System.out.println(reduced_fe.getValue());
				}
			}
		}
		System.out.println(ftrSet.size());
		if(ftrSet.contains(MALE_ORIENTED) && ftrSet.contains(FEMALE_ORIENTED)) {
			ftrSet.remove(MALE_ORIENTED);
			ftrSet.remove(FEMALE_ORIENTED);
			ftrSet.add(PLOT_OF_COMPLEXITY);
		}
		StringBuffer reducedFe = new StringBuffer();
		for (String s : ftrSet) {
			reducedFe.append(s).append(" ,");
		}
		reducedFe.deleteCharAt(reducedFe.length() - 1);
		return reducedFe.toString();

	}

	private static String getFeatureHighLevelName(String featureId) {
		StringBuffer featureName = new StringBuffer();
		System.out.println("1");
		if (featureId.equals("F0")) // writing style
			featureName.append(WRITING_STYLE);
		if (featureId.equals("F1")) // female oriented
			featureName.append(FEMALE_ORIENTED);
		if (featureId.equals("F2")) // male oriented
			featureName.append(MALE_ORIENTED);
		if (featureId.equals("F3")) // writing style
			featureName.append(WRITING_STYLE);
		if (featureId.equals("F4")) // writing style
			featureName.append(WRITING_STYLE);
		if (featureId.equals("F5")) // writing style
			featureName.append(WRITING_STYLE);
		if (featureId.equals("F6")) // sentence complexity
			featureName.append(SENTENCE_COMPLEXITY);
		if (featureId.equals("F7")) // sentence complexity
			featureName.append(SENTENCE_COMPLEXITY);
		if (featureId.equals("F8")) // sentence complexity
			featureName.append(SENTENCE_COMPLEXITY);
		if (featureId.equals("F9")) // writing style
			featureName.append(WRITING_STYLE);
		if (featureId.equals("F10")) // writing style
			featureName.append(WRITING_STYLE);
		if (featureId.equals("F11")) // writing style
			featureName.append(WRITING_STYLE);
		if (featureId.equals("F12")) // writing style
			featureName.append(WRITING_STYLE);
		if (featureId.equals("F13")) // sentence complexity and writing style
		{
			featureName.append(SENTENCE_COMPLEXITY);
			featureName.append(WRITING_STYLE);
		}
		if (featureId.equals("F14")) // sentence complexity and writing style
		{
			featureName.append(WRITING_STYLE);
			featureName.append(SENTENCE_COMPLEXITY);
		}
		if (featureId.equals("F15")) // rural and urban setting
			featureName.append(RURAL_AND_URBAN_SETTING);
		if (featureId.equals("F16")) // overall sentiment
			featureName.append(OVERALL_SENTIMENT);
		if (featureId.equals("F17")) // overall sentiment
			featureName.append(OVERALL_SENTIMENT);
		if (featureId.equals("F18")) // overall sentiment
			featureName.append(OVERALL_SENTIMENT);
		if (featureId.equals("F19")) // ease of readability
			featureName.append(EASE_OF_READABILITY);
		if (featureId.equals("F20")) // plot complexity
			featureName.append(PLOT_OF_COMPLEXITY);
		if (featureId.equals("F21")) // vocabulary richness
			featureName.append(VOCABULARY_RICHNESS);

		return featureName.toString();
	}

	public static String getFeatureName(String featureId) {
		String featureName = "";
		if (featureId.equals("F0")) // writing style
			featureName = "Paragraph Count";
		if (featureId.equals("F1")) // female oriented
			featureName = "Female Pronouns";
		if (featureId.equals("F2")) // male oriented
			featureName = "Male Pronouns";
		if (featureId.equals("F3")) // writing style
			featureName = "Personal Pronouns";
		if (featureId.equals("F4")) // writing style
			featureName = "Possesive Pronouns";
		if (featureId.equals("F5")) // writing style
			featureName = "Prepositions";
		if (featureId.equals("F6")) // sentence complexity
			featureName = "Conjunctions";
		if (featureId.equals("F7")) // sentence complexity
			featureName = "Comma";
		if (featureId.equals("F8")) // sentence complexity
			featureName = "Period";
		if (featureId.equals("F9")) // writing style
			featureName = "Colon";
		if (featureId.equals("F10")) // writing style
			featureName = "Semicolon";
		if (featureId.equals("F11")) // writing style
			featureName = "Hyphen";
		if (featureId.equals("F12")) // writing style
			featureName = "Interjection";
		if (featureId.equals("F13")) // sentence complexity and writing style
			featureName = "Conjunction Punctuation";
		if (featureId.equals("F14")) // sentence complexity and writing style
			featureName = "Sentence Length";
		if (featureId.equals("F15")) // rural and urban setting
			featureName = "Quotes";
		if (featureId.equals("F16")) // overall sentiment
			featureName = "Negetive Sentiment";
		if (featureId.equals("F17")) // overall sentiment
			featureName = "Positive Sentiment";
		if (featureId.equals("F18")) // overall sentiment
			featureName = "Neutral Sentiment";
		if (featureId.equals("F19")) // ease of readability
			featureName = "Flesh Reading Scores";
		if (featureId.equals("F20")) // plot complexity
			featureName = "Num. of Story Characters";
		if (featureId.equals("F21")) // vocabulary richness
			featureName = "Type Token Ratio";

		return featureName;
	}

	public static String readBookContent(String path) {
		File file = new File(path);
		try {
			byte[] bytes = Files.readAllBytes(file.toPath());
			return new String(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

}
