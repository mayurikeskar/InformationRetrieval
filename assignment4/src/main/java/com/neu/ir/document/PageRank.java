package com.neu.ir.document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PageRank {
	
	List<Double> perplexity = new ArrayList<Double>();
	Map<String, Integer> mapOfInlinkCounts = new HashMap<String, Integer>();
	
	public void calculatePageRank(Map<String, Page> P, String outputFile) throws IOException{
		
		int N = P.size();
		double d = 0.85;
		
		for(Map.Entry<String, Page> entry : P.entrySet()){
			Page p = P.get(entry.getKey());
			p.setPageRank((double)1/N);
			P.put(entry.getKey(), p);
		}
	
		Set<Page> sinkNodes = findSinkNodes(P);
		int j = -1;
		
		double sum_page_rank =0;
		
		while(true){
			j++;
			double sinkPR = 0;
			for(Page s : sinkNodes){
				sinkPR = sinkPR + s.getPageRank();
			}
			
			for(Map.Entry<String, Page> entry : P.entrySet()){
				double newPR = (1-d)/N;
				newPR = newPR + ((d*sinkPR)/N);
				
				for(String q : entry.getValue().getIn_links()){
					Page p = P.get(q);
					if(p == null){
						System.out.println("null");
						continue;
					}
					newPR = newPR + (d*p.getPageRank())/p.getOut_links().size();
				}
				P.get(entry.getKey()).setPageRank(newPR);
				sum_page_rank = sum_page_rank + (newPR * newPR);
			}
			
			double entropy = computeEntropy(P);
			perplexity.add(entropy);
			if(isConverge(j)){
				System.out.println("No if iterations : "+j);
				System.out.println("Sum of Page rank : "+sum_page_rank);
				BufferedWriter br = new BufferedWriter(new FileWriter("perplexities.txt"));	
				for(Double db : perplexity){
					br.write(db+"\n");
				}
				br.close();
				break;		
			}
		}	
		sortPageRankAndWrite(P, outputFile);	
	}
	
	public Set<Page> findSinkNodes(Map<String, Page> map){
		
		Set<Page> setOfSinkNodes = new HashSet<Page>();
		
		for(Map.Entry<String, Page> entry : map.entrySet()){
			if(entry.getValue().getOut_links().size() == 0)
				setOfSinkNodes.add(entry.getValue());
		}
		return setOfSinkNodes;
	}
	
	public double computeEntropy(Map<String, Page> P){
		double page_rank = 0;
		double entropy = 0;
		for(Map.Entry<String, Page> entry : P.entrySet()){
			page_rank = entry.getValue().getPageRank();
			entropy = entropy + (page_rank * Math.log(1/page_rank)); 
		}
		return Math.pow(2, entropy);
	}
	
	public boolean isConverge(int j){
		
		if(perplexity.size() >= 4){		
			if(Math.abs(perplexity.get(j) - perplexity.get(j-1)) < 0.001 && 
					Math.abs(perplexity.get(j-1) - perplexity.get(j-2)) < 0.001 &&
							Math.abs(perplexity.get(j-2) - perplexity.get(j-3)) < 0.001){
				return true;
			} 
			else
				return false;
		}
		return false;
	}
	
	
	public void sortPageRankAndWrite(Map<String, Page> P, String outputFile) throws IOException{
		Map<String, Double> mapOfPageRanks = new HashMap<String, Double>();
		double total = 0;
		for(Map.Entry<String, Page> en : P.entrySet()){
			total = total + en.getValue().getPageRank();
			//System.out.println("Page Rank of "+en.getKey()+": "+en.getValue().getPageRank());
			mapOfPageRanks.put(en.getKey(), en.getValue().getPageRank());
		}
		System.out.println(total);
		Map<String, Double> sortedMap = sortByComparator(mapOfPageRanks);
		
		BufferedWriter bw1 = new BufferedWriter(new FileWriter(outputFile));
		for(Map.Entry<String, Double> en : sortedMap.entrySet()){
			bw1.write(en.getKey()+" "+en.getValue()+"\n");
		}
		bw1.close();
	}
	
	public LinkedHashMap<String, Double> sortByComparator(Map<String, Double> unsortMap) {

		List<Map.Entry<String, Double>> list = 
				new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1,
					Map.Entry<String, Double> o2) {
				return (o2.getValue().compareTo(o1.getValue()));
			}
		});

		LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}
