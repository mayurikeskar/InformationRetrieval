package com.neu.ir.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MagicHand {

	static final double k = 0.5;
	static final double c = 1.5;
	Map<String, Double> mapOfScores = new HashMap<String, Double>();
	BufferedReader br = null;
	long avgLen = 0;
	String query = "american revolutionary war";
	Set<String> stopList;

	public MagicHand(){

	}

	public void rearrangeFrontier(Frontier frontier) throws IOException{

		try{
			Document document = null;
			int i =0;
			while(i < 2000){
				Page parentPage = frontier.dequeue();
				Connection c = Jsoup.connect(parentPage.getRawUrl()).header("Accept-Language", "en").timeout(10000);
				document = c.get();
 				String raw_text = document.body().text(); 
				double score = getTFIDF(raw_text, parentPage.getDocno());
				parentPage.setTfScore(score);
				frontier.enqueue(parentPage);
				i++;
			}
			
		} catch(IOException e){
			e.printStackTrace();
		}
	}


	public double getTFIDF(String text, String docno) throws IOException{
		stopList = getStopList();
		Map<String, Integer> termFreq = new HashMap<String, Integer>();
		String[] text_array = text.split(" ");
		int docLength = 0;
		for(String s : text_array){
			if(stopList.contains(s))
				continue;
			if(s.equalsIgnoreCase("american") || s.equalsIgnoreCase("revolutionary") || s.equalsIgnoreCase("war")){
				int count =0;
				if(termFreq.containsKey(s)){
					count = termFreq.get(s);
				}
				count = count + 1;
				termFreq.put(s, count);
			}
			docLength++;
		}
		
		double score =0;
		for(Map.Entry<String, Integer> entry : termFreq.entrySet())
			score =  score + getOKAPITf(docno, entry.getValue(), docLength, 1262);
		
		mapOfScores.put(docno, score);
		return score;

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
	
	public double getOKAPITf(String docId, int rawTf, long docLen, double avgLen) {
		double ans1 = (float)docLen / 249;
		double okapiTf = (float)rawTf  / (rawTf + k + (c * ans1));
		return okapiTf;
	}


}
