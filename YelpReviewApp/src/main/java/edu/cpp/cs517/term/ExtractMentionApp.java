/**
 * 
 */
package edu.cpp.cs517.term;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import edu.stanford.nlp.util.CoreMap;

/**
 * @author zhangliang Extract mentions(noun phrases) from reviews
 */
public class ExtractMentionApp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// read reviews.txt line by line
		// extract mentions
		// write to mentions.txt
		// format: <review_id>\t<mention_text>\t<category_id>\t<sentiment>
		// note: category_id & sentiment are annotated manually

		try {
			String reviewFileName = "/Users/zhangliang/dev/source/cs517/TermProject/data/reviews.txt";
			String mentionFileName = "/Users/zhangliang/dev/source/cs517/TermProject/data/mentions.txt";

			ReviewAnalyzer analyzer = new ReviewAnalyzer();
			
			FileWriter mentionFile = new FileWriter(mentionFileName);
			FileReader reviewFile = new FileReader(reviewFileName);
			Scanner sc = new Scanner(reviewFile);
			int numReviews = 0;
			String line;
			while (sc.hasNext() && numReviews < 100) {
				line = sc.nextLine();
				String[] lineParts = line.split("\t");
				String reviewID = lineParts[0];
				String reviewText = lineParts[2];
				
				analyzer.analyze(reviewText, 2);
				for (CoreMap sentence: analyzer.getSentences()) {
					for (String nounPhrase: analyzer.extractNounPhrase(sentence)) {
						// write to mention file
//						System.out.println(reviewID + "\t" + nounPhrase + "\t\t");
						mentionFile.write(reviewID + "\t" + nounPhrase + "\t\t\n");
					}
				}
				numReviews ++;
			}
			reviewFile.close();
			mentionFile.flush();
			mentionFile.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

}
