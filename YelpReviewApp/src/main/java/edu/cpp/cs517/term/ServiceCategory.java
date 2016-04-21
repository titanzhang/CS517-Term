package edu.cpp.cs517.term;

import java.util.ArrayList;
import java.util.List;

public class ServiceCategory {
	private static List<String> categories;
	
	public ServiceCategory() {
		if (categories == null) {
			// Test only, should load from file
			categories = new ArrayList<String>();
			categories.add("transmission");
			categories.add("engine");
		}
	}
	
	public String match(String word) {
		// Test, exact match
		for (String category: categories) {
			if (word.equalsIgnoreCase(category)) {
				return category;
			}
		}
		return "";
	}
}
