package edu.cpp.cs517.term;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class ReviewAnalyzer {
	static class SentimentBiTuple {
		public List<String> categories = new ArrayList<String>();
		public int score;
	}

	private Annotation document;
	private static StanfordCoreNLP pipeline;
	private List<SentimentBiTuple> sentiments;

	private StanfordCoreNLP getPipeline() {
		if (pipeline == null) {
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
			pipeline = new StanfordCoreNLP(props);
		}

		return pipeline;
	}

	public ReviewAnalyzer() {
		this.document = null;
		this.sentiments = null;
	}

	public List<SentimentBiTuple> getSentiments() {
		return this.sentiments;
	}

	public void analyze(String text, int scanRange) {
		this.document = new Annotation(text);
		this.getPipeline().annotate(this.document);

		this.sentiments = new ArrayList<SentimentBiTuple>();
		ServiceCategory serviceCategory = new ServiceCategory();
		List<String> currentCategories = new ArrayList<String>();
		int currentScore = 0;
		int rangeCount = 1;
		for (CoreMap sentence : this.getSentences()) {
			List<String> categories = this.extractServices(sentence, serviceCategory);
			int score = this.normalizeScore(this.getSentimentScore(sentence));

			if (!categories.isEmpty()) { // New categories, start another round
											// of analysis
				// Step 1: save current result
				if (!currentCategories.isEmpty()) {
					SentimentBiTuple biTuple = new SentimentBiTuple();
					biTuple.categories = currentCategories;
					biTuple.score = currentScore;
					this.sentiments.add(biTuple);
				}

				// Initialize states
				currentCategories = categories;
				currentScore = score;
				rangeCount = 1;
			} else {
				if (currentCategories.isEmpty()) { // Not in range analysis,
													// skip
					continue;
				}

				if (rangeCount >= scanRange) {
					// Finish range analyzing
					SentimentBiTuple biTuple = new SentimentBiTuple();
					biTuple.categories = currentCategories;
					biTuple.score = currentScore;
					this.sentiments.add(biTuple);

					// Initialize states
					currentCategories = new ArrayList<String>();
					currentScore = 0;
					rangeCount = 1;
				} else {
					rangeCount++;
					currentScore += score;
				}
			}
		}

		if (!currentCategories.isEmpty()) {
			SentimentBiTuple biTuple = new SentimentBiTuple();
			biTuple.categories = currentCategories;
			biTuple.score = currentScore;
			this.sentiments.add(biTuple);
		}
	}

	public List<CoreMap> getSentences() {
		return this.document.get(SentencesAnnotation.class);
	}

	public List<CoreLabel> getWords(CoreMap sentence) {
		return sentence.get(TokensAnnotation.class);
	}

	public String getWordText(CoreLabel word) {
		return word.get(TextAnnotation.class);
	}

	public String getWordPOS(CoreLabel word) {
		return word.getString(PartOfSpeechAnnotation.class);
	}

	public List<String> extractServices(CoreMap sentence, ServiceCategory service) {
		List<String> resultList = new ArrayList<String>();
		String nounPhrase = "";
		boolean isLastNoun = false;
		for (CoreLabel word : this.getWords(sentence)) {
			String pos = this.getWordPOS(word);
			if (pos.equals("NN") || pos.equals("NNS") || pos.equals("NNP") || pos.equals("NNPS")) {
				nounPhrase += " " + this.getWordText(word);
				isLastNoun = true;
			} else {
				if (isLastNoun) {
					String category = service.match(nounPhrase.trim());
					if (!category.isEmpty()) {
						resultList.add(category);
					}
					nounPhrase = "";
					isLastNoun = false;
				}
			}
		}
		return resultList;
	}

	public int normalizeScore(int score) {
		if (score < 2) {
			return -1;
		} else if (score == 2) {
			return 0;
		} else {
			return 1;
		}
	}

	public int getSentimentScore(CoreMap sentence) {
		Tree tree = sentence.get(SentimentAnnotatedTree.class);
		return RNNCoreAnnotations.getPredictedClass(tree);
	}

	public String getSentimentString(int score) {
		switch (score) {
		case 0:
			return "Very Negative";
		case 1:
			return "Negative";
		case 2:
			return "Neutral";
		case 3:
			return "Positive";
		case 4:
			return "Very Positive";
		default:
			return "";
		}

	}

}
