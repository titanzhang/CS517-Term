package edu.cpp.cs517.term;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cpp.cs517.term.ReviewAnalyzer.SentimentBiTuple;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

public class App {
	public static void main(String[] args) {
//		testLib();
//		testTraining();
		testPredicting();
	}

	private static void testLib() {
		String text = "I got my transmission and engine oil replaced. They did a good job!";
		// String text = "Was treated like a king when I went into service for a
		// check up and got a very honest answers and they got me out of there
		// quickly. I got a transmission update and a steering check and left
		// satisfied! The whole service crew seemed like really nice people, the
		// guy that helped me is named Josh and he did a great job! I'll
		// definitely be back and recommend Infiniti on Camelback and plus on
		// top of that they gave my car a free wash couldn't be much
		// happier!Service Department rocks!";
		ReviewAnalyzer analyzer = new ReviewAnalyzer();
		// analyzer.analyze(text, 1);
		analyzer.analyze(text, 2); // do extended sentiment analysis

		for (SentimentBiTuple sentiment : analyzer.getSentiments()) {
			for (String category : sentiment.categories) {
				System.out.print(category + ",");
			}
			System.out.print(" = " + sentiment.score);
			System.out.println("\n");
		}

		// DEBUG
		ServiceCategory serviceCategory = ServiceCategory.getInstance();

		for (CoreMap sentence : analyzer.getSentences()) {
			System.out.println("Sentence: " + sentence.toString());

			// Sentiment
			int score = analyzer.getSentimentScore(sentence);
			System.out.println("Sentiment: " + analyzer.normalizeScore(score));

			for (CoreLabel word : analyzer.getWords(sentence)) {
				// Word text
				String wordText = analyzer.getWordText(word);
				// POS tagging
				String posTag = analyzer.getWordPOS(word);

				System.out.print(wordText + "(" + posTag + ") ");
			}
			System.out.println("");

			// Mentioned services
			System.out.println("Mentions: ");
			List<String> nounPhrases = analyzer.extractNounPhrase(sentence);
			for (String nounPhrase : nounPhrases) {
				System.out.print("<<" + nounPhrase + ">> matches");

				List<String> exactMatches = serviceCategory.exactMatch(nounPhrase);
				System.out.print(" E(");
				for (String s : exactMatches) {
					System.out.print(s + ";");
				}
				System.out.print(")");

				List<String> partialMatches = serviceCategory.partialMatch(nounPhrase);
				System.out.print(" P(");
				for (String s : partialMatches) {
					System.out.print(s + ";");
				}
				System.out.print(")");

				System.out.println("");
			}
			System.out.println("");

			System.out.println("");
		}
	}
	
	private static void testPredicting() {
		try {
			String baseDir = "/Users/zhangliang/dev/source/cs517/TermProject/";
			String dictionaryFileName = baseDir + "data/dict.txt";
			String mentionFileName = baseDir + "data/mention_predict_test.txt";
			
			String svmExecutable = baseDir + "tool/svm_light/svm_classify ";
			String svmTestFileName = baseDir + "data/svm/predict_test.data";
			String svmModelFileName = baseDir + "data/svm/model_test.data";
			String svmResultFileName = baseDir + "data/svm/result_test.data";

			ReviewAnalyzer analyzer = new ReviewAnalyzer();
			
			// Load dictionary
			Dictionary dictionary = Dictionary.getInstance();
			dictionary.loadFromFile(dictionaryFileName);
			System.out.println("Dictionary loaded.");
			
			// Load annotated mentions
			List<Mention> mentions = new ArrayList<Mention>();
			BufferedReader mentionFile = new BufferedReader(new FileReader(mentionFileName));
			String line;
			while ((line = mentionFile.readLine()) != null) {
				String[] lineParts = line.trim().split("\t");
				Mention mention = new Mention(
						lineParts[0], // ReviewID
						lineParts[1], // Mention text
						Integer.parseInt(lineParts[2]), // CategoryID 
						Integer.parseInt(lineParts[3])); // sentiment (-1 | 0 | 1)
				mentions.add(mention);
			}
			mentionFile.close();
			System.out.println("Mentions loaded."); 
			
			// Convert to SVM format and write to file
			analyzer.writeTestFile(svmTestFileName, mentions);
			System.out.println("SVM test file generated.");
			
			// Execute SVM_Light to predict
			analyzer.execute(svmExecutable + svmTestFileName + " " + svmModelFileName + " " + svmResultFileName);
			System.out.println("SVM prediction done.");
			
			// Read the prediction result & return predicted categories
			List<Integer> predictCategories = analyzer.readPredictFile(svmResultFileName, mentions);
			
			// Compare categories predicted with annotated to evaluate the performance
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
	}
	private static void testTraining() {
		try {
			String baseDir = "/Users/zhangliang/dev/source/cs517/TermProject/";
			String dictionaryFileName = baseDir + "data/dict.txt";
			String mentionFileName = baseDir + "data/mention_train_test.txt";
			
			String svmExecutable = baseDir + "tool/svm_light/svm_learn ";
			String svmTrainingFileName = baseDir + "data/svm/training_test.data";
			String svmModelFileName = baseDir + "data/svm/model_test.data";

			ReviewAnalyzer analyzer = new ReviewAnalyzer();
			
			// Load dictionary
			Dictionary dictionary = Dictionary.getInstance();
			dictionary.loadFromFile(dictionaryFileName);
			System.out.println("Dictionary loaded.");
			
			// Load annotated mentions
			List<Mention> mentions = new ArrayList<Mention>();
			BufferedReader mentionFile = new BufferedReader(new FileReader(mentionFileName));
			String line;
			while ((line = mentionFile.readLine()) != null) {
				String[] lineParts = line.trim().split("\t");
				Mention mention = new Mention(
						lineParts[0], // ReviewID
						lineParts[1], // Mention text
						Integer.parseInt(lineParts[2]), // CategoryID 
						Integer.parseInt(lineParts[3])); // sentiment (-1 | 0 | 1)
				mentions.add(mention);
			}
			mentionFile.close();
			System.out.println("Mentions loaded.");
			
			// Convert to SVM format and write to file
			analyzer.writeTrainingFile(svmTrainingFileName, mentions);
			System.out.println("SVM training file generated.");
			
			// Execute SVM_Light to train the model
			analyzer.execute(svmExecutable + svmTrainingFileName + " " + svmModelFileName);
			System.out.println("SVM training done.");
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
