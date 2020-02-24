package org.ovgu.de.fiction.feature.extraction;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ovgu.de.fiction.model.BookDetails;
import org.ovgu.de.fiction.model.Chunk;
import org.ovgu.de.fiction.model.Concept;
import org.ovgu.de.fiction.model.Feature;
import org.ovgu.de.fiction.model.Word;
import org.ovgu.de.fiction.utils.FRConstants;
import org.ovgu.de.fiction.utils.FRFileOperationUtils;
import org.ovgu.de.fiction.utils.FRGeneralUtils;
import org.ovgu.de.fiction.utils.StanfordPipeline;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/**
 * @author Suhita
 */
public class ChunkDetailsGenerator {

	final static Logger LOG = Logger.getLogger(ChunkDetailsGenerator.class);

	private static Set<String> LOCATIVE_PREPOSITION_LIST;
	protected static Integer CHUNK_SIZE;
	protected static Integer TTR_CHUNK_SIZE;
	protected static String OUT_FOLDER_TOKENS;
	private int NUM_OF_CHARS_PER_BOOK = -1;
	private String CONTENT_EXTRCT_FOLDER;
	StanfordCoreNLP SENTI_PIPELINE;

	protected void init() throws NumberFormatException, IOException {

		CHUNK_SIZE = Integer.parseInt(FRGeneralUtils.getPropertyVal(FRConstants.CHUNK_SIZE));
		TTR_CHUNK_SIZE = Integer.parseInt(FRGeneralUtils.getPropertyVal(FRConstants.CHUNK_SIZE_FOR_TTR));

		OUT_FOLDER_TOKENS = FRGeneralUtils.getPropertyVal(FRConstants.OUT_FOLDER_TOKENS);
		CONTENT_EXTRCT_FOLDER = FRGeneralUtils.getPropertyVal(FRConstants.OUT_FOLDER_CONTENT);

		LOCATIVE_PREPOSITION_LIST = FRGeneralUtils.getPrepositionList();

		SENTI_PIPELINE = StanfordPipeline.getPipeline(FRConstants.STNFRD_SENTI_ANNOTATIONS);

	}

	/**
	 * @return
	 * @throws IOException
	 */
	public List<BookDetails> getChunksFromAllFiles() throws IOException {
		init();

		List<BookDetails> books = new ArrayList<>();
		FeatureExtractorUtility feu = new FeatureExtractorUtility();

		// following loop runs, over path of each book
		FRFileOperationUtils.getFileNames(CONTENT_EXTRCT_FOLDER).stream().forEach(file -> {
			String fileName = file.getFileName().toString().replace(FRConstants.CONTENT_FILE, FRConstants.NONE);

			try {
				BookDetails book = new BookDetails();
				book.setBookId(fileName);
				book.setMetadata(FRGeneralUtils.getMetadata(fileName));
				book.setChunks(getChunksFromFile(file.toString())); // this is a
																	 // list of
																	 // chunks,
																	 // each
																	 // chunk
																	 // again has
																	 // a feature
																	 // object/vector
				book.setAverageTTR(feu.getAverageTTR(getEqualChunksFromFile(getTokensFromAllChunks(book.getChunks()))));
				book.setNumOfChars(NUM_OF_CHARS_PER_BOOK == 0 ? 1 : NUM_OF_CHARS_PER_BOOK);
				books.add(book);

			} catch (IOException e) {
				LOG.error("IOException in generating chunks -" + e.getMessage() + " for book " + fileName);
			} catch (ArrayIndexOutOfBoundsException ai) {
				LOG.error("ArrayIndexOutOfBoundsException in generating chunks -" + ai.getMessage() + " for book " + fileName);
			} catch (Exception e) {
				// LOG.error("Error in generating chunks -" + e.getMessage() + " for book " +
				// fileName);
				e.printStackTrace();
			}

		});
		return books;
	}

	/**
	 * @author Suhita, Sayantan
	 * @see - The method generates List of Chunk out of the file passed in the
	 *      signature. this path is a path to a single book, mind it!
	 * @param path
	 *            to book location
	 * @return : List of Chunks, Chunk has a feature vector object
	 * @throws IOException
	 */
	public List<Chunk> getChunksFromFile(String path) throws IOException {

		int batchNumber;
		List<Chunk> chunksList = new ArrayList<>();
		Annotation annotation = null;

		WordAttributeGenerator wag = new WordAttributeGenerator();
		FeatureExtractorUtility feu = new FeatureExtractorUtility();
		List<String> stopwords = Arrays.asList(FRGeneralUtils.getPropertyVal(FRConstants.STOPWORD_FICTION).split("\\|"));
		Concept cncpt = wag.generateWordAttributes(Paths.get(path)); 
		
		for (Entry<String,Integer> c : cncpt.getCharacterMap().entrySet()) {
			LOG.info(c.getKey()+" "+c.getValue());
		}
		NUM_OF_CHARS_PER_BOOK = cncpt.getCharacterMap().size();

		List<Word> wordList = cncpt.getWords();
		int numOfSntncPerBook  = cncpt.getNumOfSentencesPerBook();


		ParagraphPredicate filter = new ParagraphPredicate();
		List<Word> copy = new ArrayList<>(wordList);
		copy.removeIf(filter);
		int length = copy.size();

		int remainder = 0;

		if (length < CHUNK_SIZE) {
			batchNumber = 0;
			remainder = length;
		} else {
			batchNumber = length / CHUNK_SIZE;
			remainder = (length % CHUNK_SIZE);
			if (remainder <= CHUNK_SIZE / 2) {
				batchNumber--;
				remainder = CHUNK_SIZE + remainder;
			}
		}

		List<Word> raw = new ArrayList<>();
		List<String> stpwrdPuncRmvd = new ArrayList<>();
		int wordcntr = 0;
		int chunkNo = 1;
		int paragraphCount = 0;
		int chunkSize = 0;
		Map<Integer, Integer> wordCountPerSntncMap = null;
		int senti_negetiv_cnt = 0;
		int senti_positiv_cnt = 0;
		int senti_neutral_cnt = 0;
		int wordCountPerSntnc = 0;
		double totalNumOfRandomSntnPerChunk =0; // sentiment_calculated_over_these_randm_sentences_per_chunk
		
		
		if(batchNumber==0) //very_small_book
			totalNumOfRandomSntnPerChunk =  (FRConstants.PERCTG_OF_SNTNC_FOR_SENTIM * numOfSntncPerBook);
		else
			totalNumOfRandomSntnPerChunk = FRConstants.PERCTG_OF_SNTNC_FOR_SENTIM * ((numOfSntncPerBook)/(batchNumber));//10%_of_sentences_per_chunk
			
		for (int batchCtr = 0; batchCtr <= batchNumber; batchCtr++) { //loop_over_number_of_chunks_of_a_book

			chunkSize = batchCtr < batchNumber ? CHUNK_SIZE : remainder;

			double malePrpPosPronounCount = 0;
			double femalePrpPosPronounCount = 0;
			double personalPronounCount = 0;
			double possPronounCount = 0;
			double locativePrepositionCount = 0;
			double coordConj = 0;
			double commaCount = 0;
			double periodCount = 0;
			double colonCount = 0;
			double semiColonCount = 0;
			double hyphenCount = 0;
			double intrjctnCount = 0;
			double convCount = 0;
			int sentenceCount = 0;
			wordCountPerSntnc = 0;
			wordCountPerSntncMap = new HashMap<>();
			senti_negetiv_cnt = 0;
			senti_positiv_cnt = 0;
			senti_neutral_cnt = 0;
			int properWordCount = 0;
			int numOfSyllables = 0;
			int randomSntnCount =0;
			StringBuffer sentenceSbf = new StringBuffer();

			for (int index = 0; index < chunkSize; index++) {// loop_over_tokens_of_a_given_chunk
				Word token = wordList.get(wordcntr);
				String l = token.getLemma();

				if (l.equals(FRConstants.P_TAG)) {
					paragraphCount++;
					wordcntr++;
					index--;
					continue;
				}
				if (l.equals(FRConstants.S_TAG)) {
					/**
					 * calculate sentiment for the previous formed sentence,
					 * the choice of selecting a sentence is totally random
					 * example, when the random number >5k and the num of selected sentences have not crossed , sampling bound = 10%_of_sentences_per_chunk
					 */
                    Random rnd = new Random();
                    int randNum = rnd.nextInt(FRConstants.RANDOM_SENTENCES_SENTIM_TOP_VAL); //get_an_INT_less_than_10k
                    
					
					if (sentenceSbf.toString().length()>0 && randNum<FRConstants.RANDOM_SENTENCES_SENTIM_MID_VAL && randomSntnCount<totalNumOfRandomSntnPerChunk) { // making_a_random_choice_here
						// calculateSenti as=>
						annotation = SENTI_PIPELINE.process(sentenceSbf.toString());
						int score = 2; // Default as Neutral. 1 = Negative, 2 =
						// Neutral, 3 = Positive
						for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class))// ideally
						// this
						// loop
						// runs
						// once!
						{
							Tree tree = sentence.get(SentimentAnnotatedTree.class);
							score = RNNCoreAnnotations.getPredictedClass(tree);
						}
						if (score == 2)
							senti_neutral_cnt++;
						if (score == 1)
							senti_negetiv_cnt++;
						if (score == 3)
							senti_positiv_cnt++;
						
						randomSntnCount++;
					}
					// reset the sentence buffer for next sentence
					sentenceSbf = new StringBuffer();

					/* partly end of sentiment calculation */
					if (wordCountPerSntnc != 0) {
						addToWordCountMap(raw, wordCountPerSntncMap, wordCountPerSntnc);
					}
					wordCountPerSntnc = 0;
					sentenceCount++;
					wordcntr++;
					index--;
					continue;
				}
				/* append the token to form sentence. Below part needed for sentiment calculation */

				else if (!l.equals(FRConstants.S_TAG) && !l.equals(FRConstants.P_TAG)) {
					sentenceSbf.append(" ").append(token.getOriginal());
				}

				if (!FRGeneralUtils.hasPunctuation(l) && !stopwords.contains(l))
					stpwrdPuncRmvd.add(l);

				raw.add(token);
				/* calculate Flesch reading score */
				if (token.getNumOfSyllables() > 0) {
					numOfSyllables += token.getNumOfSyllables();
					properWordCount++;
				}
				/* calculate pos stats */
				if (token.getPos().equals(FRConstants.PERSONAL_P)) {
					personalPronounCount++;
					if (l.equals(FRConstants.HE))
						malePrpPosPronounCount++;
					else if (l.equals(FRConstants.SHE))
						femalePrpPosPronounCount++;

				} else if (token.getPos().equals(FRConstants.POSSESIV_P)) {
					possPronounCount++;
					if (l.equals(FRConstants.HE))
						malePrpPosPronounCount++;
					else if (l.equals(FRConstants.SHE))
						femalePrpPosPronounCount++;

				} else if (token.getPos().equals(FRConstants.PREPOSITION)) {
					if (LOCATIVE_PREPOSITION_LIST.contains(l))
						locativePrepositionCount++;
					if (l.equals(FRConstants.IN)) {
						int temp = wordcntr;
						if ((l.equals(FRConstants.IN) && wordList.get(++temp).getLemma().equals(FRConstants.FRONT)
								&& wordList.get(++temp).getLemma().equals(FRConstants.OF)))
							locativePrepositionCount++;
					}

				} else if (l.equals(FRConstants.NEXT)) {
					int temp = wordcntr;
					if (l.equals(FRConstants.NEXT) && wordList.get(++temp).getLemma().equals(FRConstants.TO))
						locativePrepositionCount++;
				} else if (token.getPos().equals(FRConstants.THERE_EX) || token.getLemma().equals(FRConstants.COME))
					locativePrepositionCount++;
				else if (token.getPos().equals(FRConstants.INTERJECTION))
					intrjctnCount++;
				else if (token.getPos().equals(FRConstants.COORD_CONJUNCTION))
					coordConj++;
				else if (token.getLemma().equals(FRConstants.COMMA))
					commaCount++;
				else if (token.getLemma().equals(FRConstants.PERIOD)) {
					periodCount++;
				} else if (token.getLemma().equals(FRConstants.COLON))
					colonCount++;
				else if (token.getLemma().equals(FRConstants.SEMI_COLON))
					semiColonCount++;
				else if (token.getLemma().equals(FRConstants.HYPHEN))
					hyphenCount++;
				else if (token.getLemma().equals(FRConstants.EXCLAMATION))
					intrjctnCount++;
				else if (token.getLemma().equals(FRConstants.DOUBLE_QUOTES))
					convCount++;

				wordcntr++;
				wordCountPerSntnc++;
			}
			addToWordCountMap(raw, wordCountPerSntncMap, wordCountPerSntnc);

			Chunk chunk = new Chunk();
			chunk.setChunkNo(chunkNo);
			
			System.out.println("numbr of sentences for sentiment  ="+randomSntnCount+" for chunknum ="+chunkNo+", and total sentc  ="+numOfSntncPerBook+" for book path "+path);
			chunk.setTokenListWithoutStopwordAndPunctuation(stpwrdPuncRmvd);
			Feature feature = feu.generateFeature(chunkNo, paragraphCount, sentenceCount, raw, null, stpwrdPuncRmvd, malePrpPosPronounCount,
					femalePrpPosPronounCount, personalPronounCount, possPronounCount, locativePrepositionCount, coordConj, commaCount,
					periodCount, colonCount, semiColonCount, hyphenCount, intrjctnCount, convCount, wordCountPerSntncMap, senti_negetiv_cnt,
					senti_positiv_cnt, senti_neutral_cnt, properWordCount, numOfSyllables);
			chunk.setFeature(feature);
			chunksList.add(chunk);
			chunkNo++;

			// reset all var for next chunk
			raw = new ArrayList<>();
			stpwrdPuncRmvd = new ArrayList<>();
			paragraphCount = 0;

		}

		return chunksList;
	}

	public void addToWordCountMap(List<Word> raw, Map<Integer, Integer> wordCountPerSntncMap, int wordCount) {
		wordCountPerSntncMap.put(wordCount, !wordCountPerSntncMap.containsKey(wordCount) ? 1 : wordCountPerSntncMap.get(wordCount) + 1);
	}

	/**
	 * @param path
	 * @param stopwords
	 * @return List of Equal Chunks per file
	 * @throws IOException
	 *             The method generates List of equal sized Chunk from the
	 *             tokens list passed in the signature.It has been developed
	 *             especially for ttr
	 */
	public List<Chunk> getEqualChunksFromFile(List<String> tokens) throws Exception {

		int batchNumber;
		int remainder;
		List<Chunk> chunksList = new ArrayList<>();
		int length = tokens.size();

		if (length < TTR_CHUNK_SIZE) {
			batchNumber = 0;
			remainder = TTR_CHUNK_SIZE - length;

		} else {
			batchNumber = length / TTR_CHUNK_SIZE;
			remainder = length % TTR_CHUNK_SIZE;
		}

		List<String> textTokens = new ArrayList<>();
		List<String> appendAtEnd = new ArrayList<>();

		int chunkNo = 1;
		int wordcntr = 0;
		int chunkSize = 0;

		for (int batchCtr = 0; batchCtr <= batchNumber; batchCtr++) {

			chunkSize = batchCtr < batchNumber ? TTR_CHUNK_SIZE : remainder;
			for (int index = 0; index < chunkSize; index++) {

				String token = tokens.get(wordcntr);
				textTokens.add(token);

				if (batchCtr == 0 && wordcntr < (TTR_CHUNK_SIZE - remainder)) // tokens						 // CHUNK_SIZE
				{
					appendAtEnd.add(token);
				}
				wordcntr++;
			}

			if (remainder != 0 && batchCtr == batchNumber)
				textTokens.addAll(appendAtEnd);

			Chunk chunk = new Chunk();
			chunk.setChunkNo(chunkNo);
			chunk.setTokenListWithoutStopwordAndPunctuation(textTokens);
			chunksList.add(chunk);
			textTokens = new ArrayList<>();
			chunkNo++;
		}

		return chunksList;
	}

	/**
	 * @param chunks
	 * @return Returns list of tokens from all chunks
	 */
	private List<String> getTokensFromAllChunks(List<Chunk> chunks) {

		List<String> tokens = new ArrayList<>();
		chunks.forEach(c -> tokens.addAll(c.getTokenListWithoutStopwordAndPunctuation()));
		return tokens;

	}

}
