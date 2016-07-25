package com.neu.ir.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.tartarus.snowball.ext.EnglishStemmer;

import com.neu.ir.document.Document;
import com.neu.ir.document.Term;
import com.neu.ir.query.Query;

public class MiscFunctions {

	BufferedReader br = null;
	Map<Integer, List<String>> mapOfIgnoreTerms = new HashMap<Integer, List<String>>();

	public String stemTerm (String term) {
		EnglishStemmer stemmer = new EnglishStemmer();
		stemmer.setCurrent(term);
		if(stemmer.stem()){
			//System.out.println(stemmer.getCurrent());
			return stemmer.getCurrent();
		}
		else
			return term;
	}



	public List<Document> prepareDocument(String fileName) throws IOException{

		br = new BufferedReader(new FileReader(fileName));
		String str = null;
		boolean textFlag = false;
		StringBuilder sb = null;
		List<Document> listOfDocuments = new ArrayList<Document>();
		Document doc = null;
		while((str = br.readLine()) != null){
			if(str.trim().equals(Constants.DOC_START_TAG)){
				doc = new Document();
				sb = new StringBuilder();
			}
			if(str.contains(Constants.DOCNO_END_TAG) && str.contains(Constants.DOCNO_START_TAG))
				doc.setDocId(str.substring(8, 21));
			if(str.equals(Constants.TEXT_START_TAG)){
				textFlag = true;
				//continue;
			}
			if(str.equals(Constants.TEXT_END_TAG)){
				textFlag = false;
				doc.setText(sb.toString());
			}
			if(textFlag){
				if(str.startsWith(Constants.TEXT_START_TAG))
					str = str.replace(Constants.TEXT_START_TAG, "");
				sb.append(str.trim().toLowerCase()).append(" ");
			}
			if(str.equals(Constants.DOC_END_TAG)){
				listOfDocuments.add(doc);
			}
		}
		return listOfDocuments;
	}


	public Map<String, Term> sortByComparator(Map<String, Term> unsortMap) {

		List<Map.Entry<String, Term>> list = 
				new LinkedList<Map.Entry<String, Term>>(unsortMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Term>>() {
			public int compare(Map.Entry<String, Term> o1,
					Map.Entry<String, Term> o2) {
				return (o2.getValue().getTf()).compareTo((int)o1.getValue().getTf());
			}
		});

		Map<String, Term> sortedMap = new LinkedHashMap<String, Term>();
		for (Iterator<Map.Entry<String, Term>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Term> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	public Set<String> getStopList() throws IOException{

		br = new BufferedReader(new FileReader("stoplist.txt"));
		Set<String> stopList = new HashSet<String>();
		String stop = null;

		while((stop = br.readLine()) != null){
			stopList.add(stop);
		}
		br.close();
		return stopList;
	}

	String s1 = null;
	String s2 = null;
	int num1 = 0;
	int num2 = 0;

	public LinkedHashMap<String, Term> sortDocsByValues(LinkedHashMap<String, Term> map1,
			LinkedHashMap<String, Term> map2) {

		LinkedHashMap<String, Term> map3 = new LinkedHashMap<String, Term>();
		Set<Entry<String, Term>> set1 = map1.entrySet();
		Set<Entry<String, Term>> set2 = map2.entrySet();
		int i =0;
		int j =0;
		String s1, s2;
		int num1, num2;
		Term t;

		while(i < map1.size() && j < map2.size()){
			s1 = set1.toArray()[i].toString().split("=")[0];
			s2 = set2.toArray()[j].toString().split("=")[0];
			num1 = map1.get(s1).getTf();
			num2 = map2.get(s2).getTf();
			//int num1 = Integer.parseInt(set1.toArray()[i].toString().split("=")[1]);
			//int num2 = Integer.parseInt(set2.toArray()[j].toString().split("=")[1]);
			if(num1 >= num2){
				t = map1.get(s1);
				map3.put(s1, t);
				i++;
			} else{
				t = map2.get(s2);
				map3.put(s2, t);
				j++;
			}
		}
		while(i < map1.size()){
			s1 = set1.toArray()[i].toString().split("=")[0];
			t = map1.get(s1);
			map3.put(s1, t);
			i++;
		}
		while(j < map2.size()){
			s2 = set2.toArray()[j].toString().split("=")[0];
			t = map2.get(s2);
			map3.put(s2, t);
			j++;
		}

		/*		for(Map.Entry<String, Term> e : map3.entrySet()){
			System.out.println(e.getKey()+" -- "+e.getValue().getTf()+"--"+e.getValue().getPositions());
		}
		 */		return map3;
	}

	public List<Query> prepareQuery(String fileName) throws IOException{
		List<Query> listOfQueries = new ArrayList<Query>();
		br = new BufferedReader(new FileReader(fileName));
		String str;
		while((str = br.readLine()) != null){
			String[] queryLine = str.split(" ");
			Query q = new Query();
			q.setQueryNo(Integer.parseInt(queryLine[0].replace(".", "")));
			String[] tempArray = new String[queryLine.length-3];
			System.arraycopy(queryLine, 3, tempArray, 0, queryLine.length-3);
			List<String> queryTerms = new ArrayList<String>(Arrays.asList(tempArray));
			List<String> listOfSplitWords = new ArrayList<String>();
			for(String term : queryTerms){
				if(term.contains("-")){
					String splitArray[] = term.split("-");
					listOfSplitWords.add(splitArray[0]);
					listOfSplitWords.add(splitArray[1]);
				}
			}
			for(String split : listOfSplitWords){
				queryTerms.add(split);
			}
			ArrayList<String> finalQueryTerms = filterQuery(queryTerms);
			q.setQueryTerms(finalQueryTerms);	
			listOfQueries.add(q);
		}
		return listOfQueries;
	}

	public ArrayList<String> filterQuery(List<String> queryArray){
		ArrayList<String> queryTemp = new ArrayList<String>();
		for(String term : queryArray){
			//if(!Arrays.asList(ignoreList).contains(term.toLowerCase()))
			queryTemp.add(term.toLowerCase());
		}
		return queryTemp;
	}


	public int implementProxitmity(Map<String, String> mp1){
		int minDifference = Integer.MAX_VALUE;
		Integer[] processing = new Integer[mp1.size()];
		Integer[] finalArray = new Integer[mp1.size()];
		int[] indexTracker = new int[mp1.size()];

		List<List<Integer>> main = new ArrayList<List<Integer>>();
		boolean workingFlag = true;
		for (int i = 0; i < mp1.size(); i++) {
			List li = new ArrayList<Integer>();
			main.add(li);
		}

		int z = 0;
		int procIndex = 0;
		for (Map.Entry<String, String> e : mp1.entrySet()) {
			String pos = e.getValue();
			String poses[] = pos.split("#");
			int first = Integer.parseInt(poses[0]);
			main.get(z).add(first);
			System.out.print(first);
			for(int i=1; i<poses.length; i++){
				int second = Integer.parseInt(poses[i]);
				main.get(z).add(first+second);
				first = second;
			}
			processing[procIndex] = main.get(z).get(0);
			//	System.out.println(e.getValue().getPositions());
			//	System.out.println("processing element: " + processing[procIndex]);
			z++;
			procIndex++;
		}

		while (workingFlag) {
			int max = 0;
			int min = Integer.MAX_VALUE;
			int maxIndex = 0;
			int minIndex = 0;
		
			for (int i = 0; i < processing.length; i++) {
				if (processing[i] > max) {
					max = processing[i];
					maxIndex = i;
				}
				if (processing[i] < min) {
					min = processing[i];
					minIndex = i;
				}
			}
		
			int diff = max - min;
			if (diff < minDifference) {
				minDifference = diff;
				finalArray = processing;
			}
			indexTracker[minIndex]++;
			if (main.get(minIndex).size() == indexTracker[minIndex]) {
				workingFlag = false;
			} else {
				int val = main.get(minIndex).get(indexTracker[minIndex]);
				processing[minIndex] = val;
			}
		}
		//System.out.println("MinDifference: " + minDifference);
		return minDifference;
	}	

}

