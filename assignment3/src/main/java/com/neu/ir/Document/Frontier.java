package com.neu.ir.Document;

import java.util.PriorityQueue;
import java.util.Queue;


public class Frontier {

	private Queue<Page> list = new PriorityQueue<Page>();
	
	public boolean enqueue(Page item) {
		if(list.size() <= 20000){
			//list.addLast(item);
			list.add(item);
			
			return true;
		}
		return false;
	}

	public Page dequeue() {
		return list.poll();
	}

	public boolean hasItems() {
		return !list.isEmpty();
	}

	public int size() {
		return list.size();
	}

/*	public void addItems(Page q) {
		list.addLast(q);
	}*/

	// method to check if a page having a particular url is present in the frontier or not
	public Page contains(String url){
		for(Page p : list){
			if(p.getRawUrl().equalsIgnoreCase(url))
				return p;
		}
		return null;
	}
	
	// method to get a particular page from the queue to update its values
	public Page get(String url){
		for(Page p : list){
			if(p.getDocno().equalsIgnoreCase(url))
				return p;
		}
		return null;
	}

	
	// given a page, find the position of that page in the list
	public int getPosition(String url){
		int i = 0;
		for(Page p : list){
			if(p.getDocno().equalsIgnoreCase(url))
				break;
			i++;
		}
		return i;
	}
	
	// method to update a page currently present in the list without changing the position of the page
	public void update(Page newPage, Page oldPage){
		list.remove(oldPage);
		list.add(newPage);
	
	}
}
