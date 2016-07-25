package com.neu.ir.document;

import java.util.List;

public class Term {

	private Integer tf;
	private List<Integer> positions;
	
	public Integer getTf() {
		return tf;
	}
	public void setTf(Integer tf) {
		this.tf = tf;
	}
	public List<Integer> getPositions() {
		return positions;
	}
	public void setPositions(List<Integer> positions) {
		this.positions = positions;
	}
	
}
