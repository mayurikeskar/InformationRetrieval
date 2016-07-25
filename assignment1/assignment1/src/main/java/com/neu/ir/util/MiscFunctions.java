package com.neu.ir.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.PorterStemmer;

import com.neu.ir.document.Document;
import com.neu.ir.query.Query;

public class MiscFunctions {

	BufferedReader br = null;
	String[] ignoreList = {"document", "will", "of", "or", "a", "the", "in", "its", "by", "identify","which", "report", "include", "identify", "predict", "cite", "describe", "discuss", "being", "any", "has", "taken", "caused", "making", "based", "type", "actual", "has", "actually", "one", "at", "least", "about", "group" };
	Map<Integer, List<String>> mapOfIgnoreTerms = new HashMap<Integer, List<String>>();


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
			//doc.setDocId(str.substring(7,8));
			if(str.contains(Constants.TEXT_START_TAG)){
				textFlag = true;
				//continue;
			}
			if(str.contains(Constants.TEXT_END_TAG)){
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

	public String stemTerm (String term) {
		EnglishStemmer stemmer = new EnglishStemmer();
		stemmer.setCurrent(term);
		if(stemmer.stem()){
			System.out.println(stemmer.getCurrent());
			return stemmer.getCurrent();
		}
		else
			return term;
	}

	public ArrayList<String> filterQuery(List<String> queryArray){
		ArrayList<String> queryTemp = new ArrayList<String>();
		for(String term : queryArray){
			//if(!Arrays.asList(ignoreList).contains(term.toLowerCase()))
			queryTemp.add(term.toLowerCase());
		}
		return queryTemp;
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
}
