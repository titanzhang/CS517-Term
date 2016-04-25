package edu.cpp.cs517.term;

import java.util.List;

import edu.cpp.cs517.term.ReviewAnalyzer.SentimentBiTuple;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

public class App 
{
    public static void main( String[] args )
    {
        String text = "I got my transmission fluid and engine oil replaced. They did a good job!";
//    	String text = "Was treated like a king when I went into service for a check up and got a very honest answers and they got me out of there quickly. I got a transmission update and a steering check and left satisfied! The whole service crew seemed like really nice people, the guy that helped me is named Josh and he did a great job! I'll definitely be back and recommend Infiniti on Camelback and plus on top of that they gave my car a free wash couldn't be much happier!Service Department rocks!";
        ReviewAnalyzer analyzer = new ReviewAnalyzer();
//        analyzer.analyze(text, 1);
        analyzer.analyze(text, 2); // do extended sentiment analysis
        
        for (SentimentBiTuple sentiment: analyzer.getSentiments()) {
        	for (String category: sentiment.categories) {
        		System.out.print(category + ",");
        	}
        	System.out.print(" = " + sentiment.score);
        	System.out.println("\n");
        }
        
        // DEBUG
        ServiceCategory serviceCategory = new ServiceCategory();
        
        for (CoreMap sentence: analyzer.getSentences()) {
        	System.out.println("Sentence: " + sentence.toString());
        	
        	// Sentiment
        	int score = analyzer.getSentimentScore(sentence);
        	System.out.println("Sentiment: " + analyzer.normalizeScore(score));
        	
        	for (CoreLabel word: analyzer.getWords(sentence)) {
        		// Word text
        		String wordText = analyzer.getWordText(word);
        		// POS tagging
        		String posTag = analyzer.getWordPOS(word);
        		
        		System.out.print(wordText + "(" + posTag + ") ");
        	}        	
        	System.out.println("");

        	// Mentioned services
        	System.out.print("Related services: ");
        	List<String> services = analyzer.extractServices(sentence, serviceCategory);
        	for (String service: services) {
        		System.out.print("<<" + service + ">> ");
        	}
        	System.out.println("");

            System.out.println("");
        }
    }
}
