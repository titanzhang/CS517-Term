package edu.cpp.cs517.term;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class Dictionary {
	private Map<String, Integer> words;
	private static Dictionary instance;

	public static Dictionary getInstance() {
		if (instance == null) {
			instance = new Dictionary();
		}
		return instance;
	}
	
	private Dictionary() {
		this.words = new HashMap<String, Integer>();
	}

	public void add(String word) {
		if (this.words.containsKey(word)) {
			return;
		}
		int index = this.words.size();
		this.words.put(word, Integer.valueOf(index));
	}

	public int getIndex(String word) {
		return this.words.getOrDefault(word, -1);
	}

	public int getSize() {
		return this.words.size();
	}
	
	public void loadFromFile(String fileName) throws IOException {
		this.words = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String word = "";
		while ((word = reader.readLine()) != null) {
			this.add(word.trim());
		}
		reader.close();
	}
}