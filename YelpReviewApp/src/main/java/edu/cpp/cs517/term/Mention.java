package edu.cpp.cs517.term;

class Mention {
	public String reviewID = null;
	public String text = null;
	public int categoryID = -1;
	public int sentimentScore = -100;
	public int predictedCategoryID = -1;
	
	public Mention() {}
	public Mention(String reviewID, String text, int categoryID, int score) {
		this.reviewID = reviewID;
		this.text = text;
		this.categoryID = categoryID;
		this.sentimentScore = score;
	}
}