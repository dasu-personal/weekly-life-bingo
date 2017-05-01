package com.dasugames.bingoboard.datamodel;

public class BingoSquare {
	private String content;
	private MarkerType marker;
	private String description;
	private int contentId;
	
	public BingoSquare() {
		
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public MarkerType getMarker() {
		return marker;
	}
	public void setMarker(MarkerType marker) {
		this.marker = marker;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getContentId() {
		return contentId;
	}
	public void setContentId(int contentId) {
		this.contentId = contentId;
	}
	
	

}
