package org.ovgu.de.fiction.feature.extraction;

import java.util.function.Predicate;

import org.ovgu.de.fiction.model.Word;
import org.ovgu.de.fiction.utils.FRConstants;

/**
 * @author Suhita Ghosh
 *
 */
public class ParagraphPredicate implements Predicate<Word> {

	/* 
	 * The method tests whether the sentence contains html tags
	 */
	public boolean test(Word word) {
		if (word.getLemma().equals(FRConstants.P_TAG) || word.getLemma().equals(FRConstants.S_TAG)) {
			return true;
		}
		return false;
	}
}
