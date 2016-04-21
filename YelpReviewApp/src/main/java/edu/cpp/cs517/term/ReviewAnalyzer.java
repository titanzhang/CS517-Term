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
	private Annotation document;
	private static StanfordCoreNLP pipeline;

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
	}

	public void analyze(String text) {
		this.document = new Annotation(text);
		this.getPipeline().annotate(this.document);
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
		List<String> resultList = new ArrayList();
		for (CoreLabel word: this.getWords(sentence)) {
			String pos = this.getWordPOS(word);
			if (pos.equals("NN") || pos.equals("NNS") ||pos.equals("NNP") ||pos.equals("NNPS")) {
				String category = service.match(this.getWordText(word));
				if (!category.isEmpty()) {
					resultList.add(category);
				}
			}
		}
		return resultList;
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
