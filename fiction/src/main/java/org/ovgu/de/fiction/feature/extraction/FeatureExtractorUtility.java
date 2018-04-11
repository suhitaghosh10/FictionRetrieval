package org.ovgu.de.fiction.feature.extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovgu.de.fiction.model.BookDetails;
import org.ovgu.de.fiction.model.Chunk;
import org.ovgu.de.fiction.model.Feature;
import org.ovgu.de.fiction.model.Word;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRGeneralUtils;

import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 * @author Suhita The class contains the methods for feature extraction
 */
public class FeatureExtractorUtility {

	/**
	 * @param chunks
	 * @return The method calculates average TTR over equal sized tokens
	 */
	public BigDecimal getAverageTTR(List<Chunk> chunks) {

		int chunkNo = chunks.size();
		float ratio = 0;
		for (Chunk chunk : chunks) {
			float totalWordsCount = chunk.getTokenListWithoutStopwordAndPunctuation().size();
			float distinctWordsCount = getDistinctWordCount(chunk.getTokenListWithoutStopwordAndPunctuation());
			chunk.setTokenListWithoutStopwordAndPunctuation(null);
			ratio = ratio + (distinctWordsCount / totalWordsCount);
		}
		return new BigDecimal((ratio * 100) / chunkNo).setScale(3, RoundingMode.CEILING);
	}

	private long getDistinctWordCount(List<String> tokens) {
		return tokens.parallelStream().distinct().count();

	}

	/**
	 * @param chunkNo
	 * @param paragraphCount
	 * @param raw
	 * @param stpwrdRmvd
	 * @param stpwrdPuncRmvd
	 * @param intrjctnCount
	 * @param hyphenCount
	 * @param semiColonCount
	 * @param colonCount
	 * @param periodCount
	 * @param commaCount
	 * @param coordConj
	 * @param locativePrepositionCount
	 * @param possPronounCount
	 * @param personalPronounCount
	 * @param femalePrpPosPronounCount
	 * @param malePrpPosPronounCount
	 * @param wordCountList
	 * @param numOfSyllables
	 * @param properWordCount
	 * @return The method generates the features for a chunk
	 */
	public Feature generateFeature(Integer chunkNo, Integer paragraphCount, Integer sentenceCount, List<Word> raw,
			List<Word> stpwrdRmvd, List<String> stpwrdPuncRmvd, double malePrpPosPronounCount,
			double femalePrpPosPronounCount, double personalPronounCount, double possPronounCount,
			double locativePrepositionCount, double coordConj, double commaCount, double periodCount, double colonCount,
			double semiColonCount, double hyphenCount, double intrjctnCount, double quoteCount,
			Map<Integer, Integer> wordCountList, int senti_negetiv, int senti_positiv, int senti_neutral,
			int properWordCount, int numOfSyllables) {

		Feature feature = new Feature();

		int totalCount = raw.size();
		double totalSenti = 0d;
		totalSenti = senti_negetiv + senti_positiv + senti_neutral;

		double fleschReadingScore = 206.835 - (1.015 * ((double) properWordCount / (double) sentenceCount))
				- 84.6 * ((double) numOfSyllables / (double) properWordCount); // as per wiki
		fleschReadingScore = fleschReadingScore / 100; // normalise over 100
		if (paragraphCount != null)
			feature.setParagraphCount(new Double(paragraphCount) / new Double(totalCount));

		feature.setPersonalPronounRatio(personalPronounCount / totalCount);
		feature.setPossPronounRatio(possPronounCount / totalCount);
		feature.setFemalePrpPosPronounRatio(femalePrpPosPronounCount / (personalPronounCount + possPronounCount));
		feature.setMalePrpPosPronounRatio(malePrpPosPronounCount / (personalPronounCount + possPronounCount));
		feature.setLocativePrepositionRatio(locativePrepositionCount / totalCount);
		feature.setCoordConjunctionRatio(coordConj / totalCount);
		feature.setCommaRatio(commaCount / totalCount);
		feature.setPeriodRatio(periodCount / totalCount);
		feature.setColonRatio(colonCount / totalCount);
		feature.setSemiColonRatio(semiColonCount / totalCount);
		feature.setHyphenRatio(hyphenCount / totalCount);
		feature.setInterjectionRatio(intrjctnCount / totalCount);
		feature.setConversationRatio(quoteCount / totalCount); // considering 1 open 1 losing quote
																// -a conversation
		feature.setConjunctionPunctuationRatio(
				(coordConj + commaCount + colonCount + semiColonCount + hyphenCount) / totalCount);
		feature.setSentimentNegetiv((double) senti_neutral / totalSenti);
		feature.setSentimentPositiv((double) senti_positiv / totalSenti);
		feature.setSentimentNeutral((double) senti_negetiv / totalSenti);
		feature.setFleshReadingScore(fleschReadingScore);
		int count = wordCountList.values().parallelStream().mapToInt(Integer::intValue).sum();
		int product = 0;
		for (Integer key : wordCountList.keySet()) {
			product += key * wordCountList.get(key);
		}
		feature.setAverageSentenceLength(new Double(product) / new Double(count));
		return feature;
	}

	/**
	 * @param books
	 * @throws IOException
	 *             The method writes the generated features to a csv,which will be
	 *             later referenced. Step 1: Loop thru the chunks containing
	 *             features, Step 2: Extract a numeric vector per chunk in an array
	 *             Step 3: Create a map with Key=BookId, Values = Feature Array of
	 *             Doubles (per chunk), mind it! Step 4: Compare the query book i-th
	 *             chunk with all possible chunks of the the corpus , and store the
	 *             top 20 results, per chunk Step 5: So, if query book has 12 chunks
	 *             we have 12 results list (in a class)
	 */
	public static void writeFeaturesToCsv(List<BookDetails> books) throws IOException {

		Map<String, double[]> corpus = new HashMap<>(); // this corpus is (N*M) space, (each
														// book of
														// a corpus * #chunks of the book)
		double max_avg_senten_len = 1.00;
		double max_TTR = 1.00;
		double max_NUM_of_CHARS = 1.00;

		for (BookDetails book : books) { // enter all features of all books (including
											// the query book)
			List<Chunk> chunks = book.getChunks();
			String bookId = book.getBookId();

			for (Chunk chunk : chunks) { // enter a chunk of a book - each chunk is a "doc" for us!
											// (including qry book)
				Feature feature = chunk.getFeature();
				double[] feature_array = new double[FRConstants.FEATURE_NUMBER];
				feature_array[FRConstants.PARAGRAPH_COUNT_0] = feature.getParagraphCount();
				feature_array[FRConstants.FEMALE_PR_1] = feature.getMalePrpPosPronounRatio();
				feature_array[FRConstants.MALE_PR_2] = feature.getFemalePrpPosPronounRatio();
				feature_array[FRConstants.PERSONAL_PR_3] = feature.getPersonalPronounRatio();
				feature_array[FRConstants.POSS_PR_4] = feature.getPossPronounRatio();
				feature_array[FRConstants.PREPOSITION_5] = feature.getLocativePrepositionRatio();
				feature_array[FRConstants.CONJUNCTION_6] = feature.getCoordConjunctionRatio();
				feature_array[FRConstants.COMMA_7] = feature.getCommaRatio();
				feature_array[FRConstants.PERIOD_8] = feature.getPeriodRatio();
				feature_array[FRConstants.COLON_9] = feature.getColonRatio();
				feature_array[FRConstants.SCOLON_10] = feature.getSemiColonRatio();
				feature_array[FRConstants.HYPHEN_11] = feature.getHyphenRatio();
				feature_array[FRConstants.INTERJECTION_12] = feature.getInterjectionRatio();
				feature_array[FRConstants.CONJ_PUNC_13] = feature.getConjunctionPunctuationRatio();
				feature_array[FRConstants.SENTENCE_L_14] = feature.getAverageSentenceLength(); // need
																								// to
				// normalize,
				// across
				// corpus
				feature_array[FRConstants.QUOTES_15] = feature.getConversationRatio();
				feature_array[FRConstants.SENTI_N_16] = feature.getSentimentNegetiv();
				feature_array[FRConstants.SENTI_P_17] = feature.getSentimentPositiv();
				feature_array[FRConstants.SENTI_NEU_18] = feature.getSentimentNeutral();
				feature_array[FRConstants.FLSH_RS_19] = feature.getFleshReadingScore();
				if (max_avg_senten_len < feature_array[FRConstants.SENTENCE_L_14])
					max_avg_senten_len = feature_array[FRConstants.SENTENCE_L_14]; // max Avg. sentence length across
																					// corpus

				feature_array[FRConstants.NUM_CHARS_20] = book.getNumOfChars(); // global_vector_element,
																				// normalize_NUM_of_Chars
				if (max_NUM_of_CHARS < feature_array[FRConstants.NUM_CHARS_20])
					max_NUM_of_CHARS = feature_array[FRConstants.NUM_CHARS_20];

				feature_array[FRConstants.TTR_21] = book.getAverageTTR().doubleValue(); // global_vector_element,
																						// normalize_TTR
				if (max_TTR < feature_array[FRConstants.TTR_21])
					max_TTR = feature_array[FRConstants.TTR_21];

				// add each "doc" and its feature vector array
				corpus.put(bookId + "-" + chunk.getChunkNo(), feature_array);
			}
		}

		// Note: corpus is now normalized, and the same is written into csv file
		write(corpus, max_avg_senten_len, max_NUM_of_CHARS, max_TTR);

	}

	private static Map<String, double[]> write(Map<String, double[]> corpus, double max_avg_senten_len,
			double max_NUM_of_CHARS, double max_TTR) throws IOException {
		Map<String, double[]> corpus_normalized = new HashMap<>();
		double dummy = 10000.0000;
		String FEATURE_CSV_FILE = FRGeneralUtils.getPropertyVal("file.feature");
		String FEATURE_ARFF_FILE = FRGeneralUtils.getPropertyVal("file.wekafeature");
		try (FileWriter fileWriter = new FileWriter(FEATURE_CSV_FILE);) {

			fileWriter.append(FRConstants.FILE_HEADER.toString());
			fileWriter.append(FRConstants.NEW_LINE);

			for (Map.Entry<String, double[]> chunk_features : corpus.entrySet()) {
				fileWriter.append(String.valueOf(chunk_features.getKey()) + FRConstants.COMMA);// 'pg547-1'
																								// -
																								// First
																								// element,
																								// Primary
																								// Key
				double[] feature_vector = chunk_features.getValue(); // feature vector per book
																		// chunk
				for (int j = 0; j < feature_vector.length; j++) {// loop over a vector

					if (j != feature_vector.length - 1) {// for all Feature vector's -'j'th-element F1, F2..except last
															// index
						if (j == FRConstants.SENTENCE_L_14) {// normalize Avg. Sentence Length ,
																// 14th element of array
							fileWriter.append(String.format("%.4f",
									Math.round((feature_vector[j] / max_avg_senten_len) * dummy) / dummy)
									+ FRConstants.COMMA);
							feature_vector[j] = Math.round((feature_vector[j] / max_avg_senten_len) * dummy) / dummy;
						}
						if (j == FRConstants.NUM_CHARS_20) {// normlize
							fileWriter.append(String.format("%.4f",
									Math.round((feature_vector[j] / max_NUM_of_CHARS) * dummy) / dummy)
									+ FRConstants.COMMA);
							feature_vector[j] = Math.round((feature_vector[j] / max_NUM_of_CHARS) * dummy) / dummy;
						}
						if (j != FRConstants.NUM_CHARS_20 && j != FRConstants.SENTENCE_L_14) { // do not normalize
																								// others
							fileWriter.append(String.format("%.4f", Math.round(feature_vector[j] * dummy) / dummy)
									+ FRConstants.COMMA);
						}
					} else {// for last element, no comma!, but normalize, and then put '\n'
						fileWriter
								.append(String.format("%.4f", Math.round((feature_vector[j] / max_TTR) * dummy) / dummy)
										+ FRConstants.NEW_LINE);
						feature_vector[j] = Math.round((feature_vector[j] / max_TTR) * dummy) / dummy;
					}
				}
				corpus_normalized.put(chunk_features.getKey(), feature_vector);
			}
			// now write the csv to ARFF format for weka
			writeCSVtoARFF(FEATURE_CSV_FILE, FEATURE_ARFF_FILE);
			fileWriter.flush();
		}
		return corpus_normalized;

	}

	public static void writeCSVtoARFF(String source_CSV_FILE, String target_ARFF_FILE) throws IOException {
		// load CSV
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File(source_CSV_FILE));// source_csv
		Instances data = loader.getDataSet();

		// save ARFF
		BufferedWriter writer = new BufferedWriter(new FileWriter(target_ARFF_FILE));
		writer.write(data.toString());
		writer.flush();
		writer.close();
	}

	/**
	 * @author Sayantan,Suhita Extract the characters from the plot
	 * @param charMap
	 * @return
	 */
	public Map<String, Integer> getUniqueCharacterMap(Map<String, Integer> charMap) {

		List<String> all_names = new ArrayList<String>(charMap.keySet());

		Map<String, Integer> charMapClone = new HashMap<>(charMap);

		for (String names : all_names) {

			for (Map.Entry<String, Integer> inputMap : charMap.entrySet()) {
				if (!names.equals(inputMap.getKey())) {
					String[] outerName = names.split(" ");
					String[] innerName = inputMap.getKey().split(" ");
					String FNAME_OUTER = outerName[0];
					String LNAME_OUTER = "";
					for (int i = 1; i < outerName.length; i++) { // start_at_index_1!
						LNAME_OUTER = LNAME_OUTER + outerName[i];
					}

					String FNAME_INNER = innerName[0];
					String LNAME_INNER = "";
					for (int i = 1; i < innerName.length; i++) { // start_at_index_1!
						LNAME_INNER = LNAME_INNER + innerName[i];
					}

					/*
					 * case 1 : {John, Johny, Johny Doe} w/o "." | op = Johny Doe All absorbed into
					 * single output(o/p) "char", i.e. remove "John" and "Johny" from the charMap
					 * object and retain "Johny Doe" the biggest not that this sequence should be
					 * avoided : {M. Prosper, M. Ferdinand}, here even though "M." region matches!
					 */
					if (!FNAME_OUTER.contains(".")) {
						// case 1: {John Doe, Johny Doe} | op = Johny Doe
						if (FNAME_OUTER.length() >= FNAME_INNER.length() && FNAME_OUTER.contains(FNAME_INNER)
								&& LNAME_OUTER.equals(LNAME_INNER)) {
							if (names.length() > inputMap.getKey().length())
								charMapClone.remove(inputMap.getKey()); // remove smaller - inner- 'Johny Doe'
							else
								charMapClone.remove(names);
						}
						/*
						 * case 2: {Johny Doe, Doe} | o/p = Johny Doe Surname absorbed into Single
						 * "char", if SNAME = FNAME, i.e delete "Doe" from charMap
						 */
						if (outerName.length >= 2 && innerName.length == 1 && LNAME_OUTER.contains(FNAME_INNER)) {
							charMapClone.remove(inputMap.getKey()); // Note:innerName.length==1,
																	// doesnt combine/delete : John Doe and Mary Doe
						}

						/**
						 * case 3: {Johny Doe, John} | o/p = Johny Doe
						 */
						if (outerName.length >= 2 && innerName.length == 1 && FNAME_OUTER.contains(FNAME_INNER)) {
							charMapClone.remove(inputMap.getKey());
						}
					} else { // we deal with "." dots here below
						/*
						 * case 4: { J. Doe, John Doe, Johny Doe} | o/p = Johny Doe combine these dot
						 * names, if both of the first name starts with same alphabet and last name is
						 * same
						 */
						if (String.valueOf(FNAME_OUTER.charAt(0)).equals(String.valueOf(FNAME_INNER.charAt(0)))) {
							if ((LNAME_OUTER.contains(LNAME_INNER) || LNAME_INNER.contains(LNAME_OUTER)) // if
																											// (LNAME_OUTER.length()
																											// >=
																											// LNAME_INNER.length()
																											// &&
																											// LNAME_OUTER.contains(LNAME_INNER)
									&& !LNAME_INNER.equals("") && !LNAME_OUTER.equals("")) {
								if (names.length() < inputMap.getKey().length()) {// remove smaller
									charMapClone.remove(names);
								} else {
									charMapClone.remove(inputMap.getKey());
								}
							}

						}

					}

				}
			}
		}
		return charMapClone;
	}

}
