package com.neu.ir.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ValueComparator<String, Integer extends Comparable<Integer>> implements Comparator<String>{

	HashMap<String, Integer> map = new HashMap<String, Integer>();

	public ValueComparator(Map<String, Integer> map){
		this.map.putAll(map);
	}

	public int compare(String s1, String s2) {
		return -map.get(s1).compareTo(map.get(s2));//descending order
	}
}