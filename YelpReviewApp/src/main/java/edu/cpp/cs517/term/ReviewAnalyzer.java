package edu.cpp.cs517.term;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

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
		ServiceCategory serviceCategory = ServiceCategory.getInstance();
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

	public List<String> extractNounPhrase(CoreMap sentence) {
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
					resultList.add(nounPhrase.trim());
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

	public void writeTrainingFile(String fileName, List<Mention> mentions) throws IOException {
		ServiceCategory serviceCategory = ServiceCategory.getInstance();
		Dictionary dictionary = Dictionary.getInstance();
		int matchIndexStart = dictionary.getSize();

		// Open file
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), false));

		for (Mention mention : mentions) {
			String annotatedCategory = serviceCategory.get(mention.categoryID);

			Map<Integer, Integer> wordFeatures = new TreeMap<Integer, Integer>();
			// Features from every word of noun phrase
			for (String word : mention.text.split(" ")) {
				// Lookup dictionary to get the index
				int wordIndex = dictionary.getIndex(word.toLowerCase());
				if (wordIndex < 0) {
					continue; // not in dictionary
				}
				wordFeatures.put(wordIndex, 1);
			}

			// no category related to this mention
			if (annotatedCategory == null) { 
				writer.write("-1");
				for (Integer feature : wordFeatures.keySet()) {
					writer.write(" " + feature + ":" + wordFeatures.get(feature));
				}
				writer.write("\n");
				continue;
			}

			// Find possible categories
			List<String> possibleCategories = serviceCategory.partialMatch(mention.text);
			if (possibleCategories.isEmpty()) { // same meaning, different
												// words, ex: tranny and
												// transmission
				// Do not support this case for now
				// TODO: add edit distance to support this
				continue;
			}

			for (String possibleCategory : serviceCategory.partialMatch(mention.text)) {
				// SVM file format: <1/-1> <feature>:<value> ....
				Map<Integer, Integer> features = new TreeMap<Integer, Integer>();

				// Target value for SVM
				boolean isMatched = annotatedCategory.equalsIgnoreCase(possibleCategory);

				// Features from match results
				// TODO: add feature for edit distance
				if (serviceCategory.isExactMatch(mention.text, possibleCategory)) {
					features.put(matchIndexStart + 1, 1);
				}
				if (serviceCategory.isPartialMatch(mention.text, possibleCategory)) {
					features.put(matchIndexStart + 2, 1);
				}

				// write target and features to trainning file
				writer.write(isMatched ? "1" : "-1");
				for (Integer feature : wordFeatures.keySet()) {
					writer.write(" " + feature + ":" + wordFeatures.get(feature));
				}
				for (Integer feature : features.keySet()) {
					writer.write(" " + feature + ":" + features.get(feature));
				}
			}

		}

		writer.close();
	}

	public void writeTestFile(String fileName, List<Mention> mentions) throws IOException {
		ServiceCategory serviceCategory = ServiceCategory.getInstance();
		Dictionary dictionary = Dictionary.getInstance();
		int matchIndexStart = dictionary.getSize();

		// Open file
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), false));

		for (Mention mention : mentions) {
			Map<Integer, Integer> wordFeatures = new TreeMap<Integer, Integer>();
			// Features from every word of noun phrase
			for (String word : mention.text.split(" ")) {
				// Lookup dictionary to get the index
				int wordIndex = dictionary.getIndex(word.toLowerCase());
				if (wordIndex < 0) {
					continue; // not in dictionary
				}
				wordFeatures.put(wordIndex, 1);
			}

			// Find possible categories
			List<String> possibleCategories = serviceCategory.partialMatch(mention.text);
			if (possibleCategories.isEmpty()) { // same meaning, different
												// words, ex: tranny and
												// transmission
				// Do not support this case for now
				// TODO: add edit distance to support this
				continue;
			}

			for (String possibleCategory : serviceCategory.partialMatch(mention.text)) {
				// SVM file format: <1/-1> <feature>:<value> ....
				Map<Integer, Integer> features = new TreeMap<Integer, Integer>();

				// Features from match results
				// TODO: add feature for edit distance
				if (serviceCategory.isExactMatch(mention.text, possibleCategory)) {
					features.put(matchIndexStart + 1, 1);
				}
				if (serviceCategory.isPartialMatch(mention.text, possibleCategory)) {
					features.put(matchIndexStart + 2, 1);
				}

				// write target and features to test file
				writer.write("0");
				for (Integer feature : wordFeatures.keySet()) {
					writer.write(" " + feature + ":" + wordFeatures.get(feature));
				}
				for (Integer feature : features.keySet()) {
					writer.write(" " + feature + ":" + features.get(feature));
				}
			}

		}

		writer.close();
	}

	public List<Integer> readPredictFile(String fileName, List<Mention> mentions) throws IOException {
		// TODO:implement
		List<Integer> predictCategories = new ArrayList<Integer>();
		return predictCategories;
	}
	public int execute(String command) {
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);

			// Separate thread to print out error and output.
//			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), null);
//			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), null);
//			errorGobbler.start();
//			outputGobbler.start();

			// Wait for thread to finish and print out exit value.
			int exitVal = proc.waitFor();
			return exitVal;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return -1;
	}

}
