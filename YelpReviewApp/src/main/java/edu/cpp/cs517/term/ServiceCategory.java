package edu.cpp.cs517.term;

import java.util.ArrayList;
import java.util.List;

public class ServiceCategory {
	private static List<String> categories;
	
	public ServiceCategory() {
		if (categories == null) {
			// Test only, should load from file
			categories = new ArrayList<String>();
			categories.add("Brake Pad");
			categories.add("Brake Rotor");
			categories.add("Brake Caliper");
			categories.add("ABS");
			categories.add("Battery");
			categories.add("Door Window");
			categories.add("AC");
			categories.add("Air Filter");
			categories.add("Transmission Fluid");
			categories.add("Engine Oil");
			categories.add("Coolant");
			categories.add("Ball Joint");
			categories.add("Steering");
			categories.add("Wheel Bearing");
			categories.add("Shock Absorber");
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
