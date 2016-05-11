package edu.cpp.cs517.term;

import java.util.ArrayList;
import java.util.List;

public class ServiceCategory {
	private List<String> categories;
	private static ServiceCategory instance;
	
	public static ServiceCategory getInstance() {
		if (instance == null) {
			instance = new ServiceCategory();
		}
		return instance;
	}
	
	private ServiceCategory() {
		if (categories == null) {
			// Test only, should load from file
			categories = new ArrayList<String>();
			categories.add("emission test");
			categories.add("tire");
			categories.add("battery");
			categories.add("air filter");
			categories.add("transmission fluid");
			categories.add("engine oil");
			categories.add("coolant");
			categories.add("ball joint");
			categories.add("steering");
			categories.add("brake");
			categories.add("shock absorber");
		}
	}
	
	public String match(String word) {
		// Test, exact match
		for (String category: this.categories) {
			if (word.equalsIgnoreCase(category)) {
				return category;
			}
		}
		return "";
	}
	
	public boolean isExactMatch(String nounPhrase, String category) {
		return (category.trim().equalsIgnoreCase(nounPhrase.trim()));
	}
	
	public List<String> exactMatch(String nounPhrase) {
		List<String> potentialList = new ArrayList<String>();
		for (String category: this.categories) {
			if (this.isExactMatch(nounPhrase, category)) {
				potentialList.add(category);
			}
		}
		return potentialList;
	}

	public boolean isPartialMatch(String nounPhrase, String category) {
		String[] categoryWords = category.toLowerCase().split(" ");
		String[] phraseWords = nounPhrase.toLowerCase().split(" ");
		
		int numberMatched = 0;
		for (int i = 0; i < categoryWords.length; i++) {
			for (int j = 0; j < phraseWords.length; j ++) {
				if (categoryWords[i].equals(phraseWords[j])) {
					numberMatched ++;
				}
			}
		}
		
		return (numberMatched > phraseWords.length / 2);
	}
	
	public List<String> partialMatch(String nounPhrase) {
		List<String> potentialList = new ArrayList<String>();
		for (String category: this.categories) {			
			if (this.isPartialMatch(nounPhrase, category)) {
				potentialList.add(category);
			}
		}
		return potentialList;
	}
		
	public String get(int index) {
		if (index < 0) {
			return null;
		}
		return this.categories.get(index);
	}
}
