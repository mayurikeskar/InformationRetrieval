package com.neu.ir.document;

import java.util.HashSet;
import java.util.Set;

public class Page implements Comparable<Page>{

	private String docno;
	private Set<String> out_links;
	private Set<String> in_links;
	private String text;
	private double pageRank;
	private double authority_score;
	private double hub_score;
	
	public double getAuthority_score() {
		return authority_score;
	}

	public void setAuthority_score(double authority_score) {
		this.authority_score = authority_score;
	}

	public double getHub_score() {
		return hub_score;
	}

	public void setHub_score(double hub_score) {
		this.hub_score = hub_score;
	}

	public double getPageRank() {
		return pageRank;
	}

	public void setPageRank(double pageRank) {
		this.pageRank = pageRank;
	}

	public Page(){
		out_links = new HashSet<String>();
		in_links = new HashSet<String>();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	
	public String getDocno() {
		return docno;
	}


	public void setDocno(String docno) {
		this.docno = docno;
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
//		if(o.getScore() == score)
//			return (int) (timeStamp - o.getTimeStamp());
//		return o.getScore() - score;
		return 0;
	}

}