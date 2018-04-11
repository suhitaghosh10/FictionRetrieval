package org.ovgu.de.fiction.model;

import java.io.Serializable;

/**
 * @author Suhita
 */
public class Feature implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4194260711052517403L;

	private double paragraphCount;

	private double malePrpPosPronounRatio; // he his
	private double femalePrpPosPronounRatio; // she her
	private double personalPronounRatio; // I, you, he, she, it, we, they, who
	private double possPronounRatio;// My, your, his, her, its, our and their
	private double locativePrepositionRatio; // in, at, on, by, next to, beside, near,in front
											 // of,towards
	private double coordConjunctionRatio; // fanboys
	private double commaRatio;
	private double periodRatio;
	private double colonRatio;
	private double semiColonRatio;
	private double hyphenRatio;
	private double interjectionRatio;
	private double conjunctionPunctuationRatio;
	private double averageSentenceLength;
	private double conversationRatio;
	private double sentimentNeutral;
	private double sentimentNegetiv;
	private double sentimentPositiv;
	private double fleshReadingScore;

	/**
	 * @return the paragraphCount
	 */
	public double getParagraphCount() {
		return paragraphCount;
	}

	/**
	 * @param paragraphCount
	 *            the paragraphCount to set
	 */
	public void setParagraphCount(double paragraphCount) {
		this.paragraphCount = paragraphCount;
	}

	

	/**
	 * @return the malePrpPosPronounRatio
	 */
	public double getMalePrpPosPronounRatio() {
		return malePrpPosPronounRatio;
	}

	/**
	 * @param malePrpPosPronounRatio
	 *            the malePrpPosPronounRatio to set
	 */
	public void setMalePrpPosPronounRatio(double malePrpPosPronounRatio) {
		this.malePrpPosPronounRatio = malePrpPosPronounRatio;
	}

	/**
	 * @return the femalePrpPosPronounRatio
	 */
	public double getFemalePrpPosPronounRatio() {
		return femalePrpPosPronounRatio;
	}

	/**
	 * @param femalePrpPosPronounRatio
	 *            the femalePrpPosPronounRatio to set
	 */
	public void setFemalePrpPosPronounRatio(double femalePrpPosPronounRatio) {
		this.femalePrpPosPronounRatio = femalePrpPosPronounRatio;
	}

	/**
	 * @return the personalPronounRatio
	 */
	public double getPersonalPronounRatio() {
		return personalPronounRatio;
	}

	/**
	 * @param personalPronounRatio
	 *            the personalPronounRatio to set
	 */
	public void setPersonalPronounRatio(double personalPronounRatio) {
		this.personalPronounRatio = personalPronounRatio;
	}

	/**
	 * @return the possPronounRatio
	 */
	public double getPossPronounRatio() {
		return possPronounRatio;
	}

	/**
	 * @param possPronounRatio
	 *            the possPronounRatio to set
	 */
	public void setPossPronounRatio(double possPronounRatio) {
		this.possPronounRatio = possPronounRatio;
	}

	/**
	 * @return the locativePrepositionRatio
	 */
	public double getLocativePrepositionRatio() {
		return locativePrepositionRatio;
	}

	/**
	 * @param locativePrepositionRatio
	 *            the locativePrepositionRatio to set
	 */
	public void setLocativePrepositionRatio(double locativePrepositionRatio) {
		this.locativePrepositionRatio = locativePrepositionRatio;
	}

	/**
	 * @return the coordConjunctionRatio
	 */
	public double getCoordConjunctionRatio() {
		return coordConjunctionRatio;
	}

	/**
	 * @param coordConjunctionRatio
	 *            the coordConjunctionRatio to set
	 */
	public void setCoordConjunctionRatio(double coordConjunctionRatio) {
		this.coordConjunctionRatio = coordConjunctionRatio;
	}

	/**
	 * @return the conversationRatio
	 */
	public double getConversationRatio() {
		return conversationRatio;
	}

	/**
	 * @param conversationRatio
	 *            the conversationRatio to set
	 */
	public void setConversationRatio(double conversationRatio) {
		this.conversationRatio = conversationRatio;
	}

	/**
	 * @return the commaRatio
	 */
	public double getCommaRatio() {
		return commaRatio;
	}

	/**
	 * @param commaRatio
	 *            the commaRatio to set
	 */
	public void setCommaRatio(double commaRatio) {
		this.commaRatio = commaRatio;
	}

	/**
	 * @return the periodRatio
	 */
	public double getPeriodRatio() {
		return periodRatio;
	}

	/**
	 * @param periodRatio
	 *            the periodRatio to set
	 */
	public void setPeriodRatio(double periodRatio) {
		this.periodRatio = periodRatio;
	}

	/**
	 * @return the colonRatio
	 */
	public double getColonRatio() {
		return colonRatio;
	}

	/**
	 * @param colonRatio
	 *            the colonRatio to set
	 */
	public void setColonRatio(double colonRatio) {
		this.colonRatio = colonRatio;
	}

	/**
	 * @return the semiColonRatio
	 */
	public double getSemiColonRatio() {
		return semiColonRatio;
	}

	/**
	 * @param semiColonRatio
	 *            the semiColonRatio to set
	 */
	public void setSemiColonRatio(double semiColonRatio) {
		this.semiColonRatio = semiColonRatio;
	}

	/**
	 * @return the hyphenRatio
	 */
	public double getHyphenRatio() {
		return hyphenRatio;
	}

	/**
	 * @param hyphenRatio
	 *            the hyphenRatio to set
	 */
	public void setHyphenRatio(double hyphenRatio) {
		this.hyphenRatio = hyphenRatio;
	}

	/**
	 * @return the interjectionRatio
	 */
	public double getInterjectionRatio() {
		return interjectionRatio;
	}

	/**
	 * @param interjectionRatio
	 *            the interjectionRatio to set
	 */
	public void setInterjectionRatio(double interjectionRatio) {
		this.interjectionRatio = interjectionRatio;
	}

	/**
	 * @return the conjunctionPunctuationRatio
	 */
	public double getConjunctionPunctuationRatio() {
		return conjunctionPunctuationRatio;
	}

	/**
	 * @param conjunctionPunctuationRatio
	 *            the conjunctionPunctuationRatio to set
	 */
	public void setConjunctionPunctuationRatio(double conjunctionPunctuationRatio) {
		this.conjunctionPunctuationRatio = conjunctionPunctuationRatio;
	}

	/**
	 * @return the averageSentenceLength
	 */
	public double getAverageSentenceLength() {
		return averageSentenceLength;
	}

	/**
	 * @param averageSentenceLength
	 *            the averageSentenceLength to set
	 */
	public void setAverageSentenceLength(double averageSentenceLength) {
		this.averageSentenceLength = averageSentenceLength;
	}

	public double getSentimentNeutral() {
		return sentimentNeutral;
	}

	public void setSentimentNeutral(double sentimentNeutral) {
		this.sentimentNeutral = sentimentNeutral;
	}

	public double getSentimentNegetiv() {
		return sentimentNegetiv;
	}

	public void setSentimentNegetiv(double sentimentNegetiv) {
		this.sentimentNegetiv = sentimentNegetiv;
	}

	public double getSentimentPositiv() {
		return sentimentPositiv;
	}

	public void setSentimentPositiv(double sentimentPositiv) {
		this.sentimentPositiv = sentimentPositiv;
	}

	
	public double getFleshReadingScore() {
		return fleshReadingScore;
	}

	public void setFleshReadingScore(double fleshReadingScore) {
		this.fleshReadingScore = fleshReadingScore;
	}

	@Override
	public String toString() {
		return "Feature [paragraphCount=" + paragraphCount + ", malePrpPosPronounRatio=" + malePrpPosPronounRatio
				+ ", femalePrpPosPronounRatio=" + femalePrpPosPronounRatio + ", personalPronounRatio="
				+ personalPronounRatio + ", possPronounRatio=" + possPronounRatio + ", locativePrepositionRatio="
				+ locativePrepositionRatio + ", coordConjunctionRatio=" + coordConjunctionRatio + ", commaRatio="
				+ commaRatio + ", periodRatio=" + periodRatio + ", colonRatio=" + colonRatio + ", semiColonRatio="
				+ semiColonRatio + ", hyphenRatio=" + hyphenRatio + ", interjectionRatio=" + interjectionRatio
				+ ", conjunctionPunctuationRatio=" + conjunctionPunctuationRatio + ", averageSentenceLength="
				+ averageSentenceLength + ", conversationRatio=" + conversationRatio + ", sentimentNeutral="
				+ sentimentNeutral + ", sentimentNegetiv=" + sentimentNegetiv + ", sentimentPositiv=" + sentimentPositiv
				+ ", fleshReadingScore=" + fleshReadingScore + "]";
	}

}
