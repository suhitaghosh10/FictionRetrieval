package org.ovgu.de.fiction.utils;

import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * @author Suhita
 *
 */
public class StanfordPipeline {

	private static StanfordCoreNLP pipeline;

	/**
	 * @return
	 */
	public static StanfordCoreNLP getPipeline(String annotations) {
		if (pipeline != null)
			return pipeline;

		Properties properties = new Properties();
		if (annotations == null)
			properties.put(FRConstants.STNFRD_ANNOTATOR, FRConstants.STNFRD_LEMMA_ANNOTATIONS);
		else
			properties.put(FRConstants.STNFRD_ANNOTATOR, annotations);
		properties.put("ner.useSUTime", "false ");
		properties.put("ner.applyNumericClassifiers", "false");
		if(annotations!=null && annotations.contains("parse")){
			properties.put("depparse.model", "edu/stanford/nlp/models/parser/nndep/english_SD.gz");
			//properties.put("parse.maxlen", "30");
		}
		return new StanfordCoreNLP(properties);
	}

	public static void resetPipeline() {
		pipeline = null;
	}
}
