package com.neu.ir.Document;

import java.util.HashSet;
import java.util.Set;

public class Page implements Comparable<Page>{

	//private int pageId;
	private String docno;
	private String rawUrl;
	private String title;
	private String text;
	private String HTTPheader;
	private String html_Source;
	private Set<String> out_links;
	public double getTfScore() {
		return tfScore;
	}

	public void setTfScore(double tfScore) {
		this.tfScore = tfScore;
	}


	private Set<String> in_links;
	private int score;
	private long timeStamp;
	private int depth;
	private String author;
	private double tfScore;

	public Page(){
		out_links = new HashSet<String>();
		in_links = new HashSet<String>();
		author = "Mayuri";
		tfScore = 0;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public String getAuthor() {
		return author;
	}


	public void setAuthor(String author) {
		this.author = author;
	}


	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getRawUrl() {
		return rawUrl;
	}
	public void setRawUrl(String domain) {
		this.rawUrl = domain;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDocno() {
		return docno;
	}


	public void setDocno(String docno) {
		this.docno = docno;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public String getHTTPheader() {
		return HTTPheader;
	}


	public void setHTTPheader(String hTTPheader) {
		HTTPheader = hTTPheader;
	}


	public String getHtml_Source() {
		return html_Source;
	}


	public void setHtml_Source(String html_Source) {
		this.html_Source = html_Source;
	}


	public Set<String> getOut_links() {
		return out_links;
	}


	public void setOut_links(Set<String> out_links) {
		this.out_links = out_links;
	}


	public Set<String> getIn_links() {
		return in_links;
	}


	public void setIn_links(Set<String> in_links) {
		this.in_links = in_links;
	}


	public int compareTo(Page o) {
		if(o.getTfScore() == tfScore){
			if(o.getScore() == score)
				return (int) (timeStamp - o.getTimeStamp());
			else
				return o.getScore() - score;
		} 
		return (int) (o.getTfScore() - tfScore);
	}


	public int getScore() {
		return score;
	}


	public void setScore(int score) {
		this.score = score;
	}
}
