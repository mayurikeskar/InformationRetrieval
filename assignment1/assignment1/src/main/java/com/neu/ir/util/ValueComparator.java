package com.neu.ir.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ValueComparator<String, Double extends Comparable<Double>> implements Comparator<String>{

	HashMap<String, Double> map = new HashMap<String, Double>();

	public ValueComparator(Map<String, Double> map){
		this.map.putAll(map);
	}

	public int compare(String s1, String s2) {
		return -map.get(s1).compareTo(map.get(s2));//descending order
	}
}