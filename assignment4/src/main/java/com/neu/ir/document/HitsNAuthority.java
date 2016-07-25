package com.neu.ir.document;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.neu.ir.index.IndexData;
import com.neu.ir.index.IndexDataImpl;

public class HitsNAuthority {

	static final double b = 0.75;
	static final double k_1 = 1.2;
	static final double k_2 = 1.2;

	static Map<String, Integer> mapOfDocLengths = new HashMap<String, Integer>();

	Map<String, Double> mapOfHubs = new HashMap<String, Double>();
	Map<String, Double> mapOfAuthority = new HashMap<String, Double>();

	List<Double> hubPerplexity = new ArrayList<Double>();
	List<Double> authPerplexity = new ArrayList<Double>();

	static double avgLen = 0.0;
	IndexData id;
	PageRank pr;

	public HitsNAuthority(){
		try {
			id = new IndexDataImpl();
			pr = new PageRank();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void compute(Map<String, Page> P) throws IOException{

		System.out.println(P.size());
		for(Map.Entry<String, Page> entry : P.entrySet()){
			if(P.containsKey(entry.getKey())){
				if(P.get(entry.getKey()) == null)
					continue;
				P.get(entry.getKey()).setAuthority_score(1);
				P.get(entry.getKey()).setHub_score(1);
			} 
		}

		int j = -1;
		
		while(true){
			j++;
			double norm = 0;
			Page p;
			for(Map.Entry<String, Page> entry: P.entrySet()){			
				p = entry.getValue();
				p.setAuthority_score(0);
				double auth = p.getAuthority_score();
				for(String inlink : p.getIn_links()){
					Page q = P.get(inlink);
					if(q == null)
						continue;
					auth = auth + q.getHub_score();
				}
				p.setAuthority_score(auth);
				P.put(entry.getKey(), p);
				mapOfAuthority.put(entry.getKey(), auth);
				norm = norm + (auth * auth);
			}
			norm = Math.sqrt(norm);

			for(Map.Entry<String, Page> entry : P.entrySet()){
				p = entry.getValue();
				double newAuth = p.getAuthority_score()/norm;
				p.setAuthority_score(newAuth);
				P.put(entry.getKey(), p);
				mapOfAuthority.put(entry.getKey(), newAuth);
			}	


			norm = 0;
			for(Map.Entry<String, Page> entry : P.entrySet()){
				p = entry.getValue();
				//	p.setHub_score(0);
				double hub = p.getHub_score();

				for(String s : p.getOut_links()){
					Page r = P.get(s);
					if(r == null)
						continue;
					hub = hub + r.getAuthority_score();	
				}
				p.setHub_score(hub);
				P.put(entry.getKey(), p);
				mapOfHubs.put(entry.getKey(), hub);
				norm = norm + (hub * hub);
			}
			norm = Math.sqrt(norm);

			for(Map.Entry<String, Page> entry : P.entrySet()){
				p = entry.getValue();
				double newHub = p.getHub_score()/norm;
				p.setHub_score(newHub);
				P.put(entry.getKey(), p);
				mapOfHubs.put(entry.getKey(), newHub);
			}

			double hubEntropy = computeEntropy(mapOfHubs);
			double authEntropy = computeEntropy(mapOfAuthority);

			authPerplexity.add(authEntropy);
			hubPerplexity.add(hubEntropy);
			if(j==1000)
				break;		

		}

		int k=0;
		Map<String, Double> sortedHubs = pr.sortByComparator(mapOfHubs);
		BufferedWriter bw1 = new BufferedWriter(new FileWriter("h.txt"));
		for(Map.Entry<String, Double> entry : sortedHubs.entrySet()){
			bw1.write(entry.getKey() + " "+ entry.getValue());
			bw1.newLine();
			k++;
		}
		bw1.close();

		k=0;
		Map<String, Double> sortedAuth = pr.sortByComparator(mapOfAuthority);
		BufferedWriter bw2 = new BufferedWriter(new FileWriter("a.txt"));
		for(Map.Entry<String, Double> entry : sortedAuth.entrySet()){
			bw2.write(entry.getKey() + " "+ entry.getValue());
			bw2.newLine();

			k++;
		}
		bw2.close();

	}

	public boolean isAuthConverge(int j){

		if(authPerplexity.size() >= 4){		
			if(Math.round(authPerplexity.get(j)) == Math.round(authPerplexity.get(j-1)) && 
					Math.round(authPerplexity.get(j-1)) == Math.round(authPerplexity.get(j-2)) &&
					Math.round(authPerplexity.get(j-2)) == Math.round(authPerplexity.get(j-3))){
				return true;
			} 
			else
				return false;
		}
		return false;
	}

	public boolean isHubConverge(int j){

		if(hubPerplexity.size() >= 4){		
			if(Math.abs(hubPerplexity.get(j) - hubPerplexity.get(j-1)) < 0.001 && 
					Math.abs(hubPerplexity.get(j-1) - hubPerplexity.get(j-2)) < 0.001 &&
					Math.abs(hubPerplexity.get(j-2) - hubPerplexity.get(j-3)) < 0.001){
				return true;
			} 
			else
				return false;
		}
		return false;
	}

	public double computeEntropy(Map<String, Double> P){
		double page_rank = 0;
		double entropy = 0;
		for(Map.Entry<String, Double> entry : P.entrySet()){
			page_rank = entry.getValue();
			entropy = entropy + (page_rank * Math.log(1/page_rank)); 
		}
		return Math.pow(2, entropy);
	}

	public double getBM25(String docno, int rawTf, long docLen, double avLen, double weight) {
		double ans0 = (float)docLen / avLen;
		double ans1 = (float)(((k_1 + 1) * rawTf) / (k_1 * ((1-b) + (b * ans0)) + rawTf));
		double bm25 = ans1  * weight;
		return bm25;
	}

	/*public String stemTerm (String term) {
		EnglishStemmer stemmer = new EnglishStemmer();
		stemmer.setCurrent(term);
		if(stemmer.stem()){
			System.out.println(stemmer.getCurrent());
			return stemmer.getCurrent();
		}
		else
			return term;
	}*/

	public List<String> getMapOfTexts(String query) throws Exception{
		Map<String, Double> mapOfDocScores = new HashMap<String, Double>();	
		String queryTerms[] = query.split(" ");

		computeDocLengths();
		Map<String, Integer> mapOfTermFreqs = new HashMap<String, Integer>();
		for(String q : queryTerms){
			mapOfTermFreqs = id.getTermFrequency(q);
			double weight = computeIDF(mapOfTermFreqs.size());
			for(Map.Entry<String, Integer> entry : mapOfTermFreqs.entrySet()){
				String docId = entry.getKey();
				int rawTf = entry.getValue();
				int docLen = mapOfDocLengths.get(docId);
				double okapiTf  = getBM25(docId, rawTf, docLen, avgLen, weight);
				if(mapOfDocScores.containsKey(docId)){
					double currTf = mapOfDocScores.get(docId);
					currTf = currTf + okapiTf;
					mapOfDocScores.put(docId, currTf);
				} else
					mapOfDocScores.put(docId, okapiTf);
			}
		}

		PageRank pr = new PageRank();
		LinkedHashMap<String, Double> sortedDocScores = pr.sortByComparator(mapOfDocScores);

		//		LinkedHashMap<String, Double> topSortedDocs = new LinkedHashMap<String, Double>();
		List<String> li = new ArrayList<String>();
		for(Map.Entry<String, Double> en : sortedDocScores.entrySet()){
			//topSortedDocs.put(en.getKey(), en.getValue());
			li.add(en.getKey());

			if(li.size() == 1000)
				break;
		}

		return li;
	}

	public double computeIDF(int termCount){
		double weight;
		double temp= (float)(mapOfDocLengths.size() - termCount + 0.5)/ (termCount + 0.5);
		weight = Math.log(temp);
		return weight;
	}

	public void computeDocLengths() throws IOException{
		System.out.println("STEP 1 : Computing document lengths and avg document length");
		mapOfDocLengths = id.getDocumentLength();
		System.out.println(mapOfDocLengths.size());

		FileWriter fileWriter = new FileWriter("Test");
		BufferedWriter writer = new BufferedWriter(fileWriter);
		for(Map.Entry<String, Integer> entry : mapOfDocLengths.entrySet()){
			//System.out.println(entry.getKey() + "--" + entry.getValue());
			writer.write(entry.getKey()+" "+entry.getValue());
			writer.newLine(); 
			avgLen = avgLen + entry.getValue();
		}
		writer.close();
		System.out.println("SUM" + avgLen);
		avgLen  = (float)avgLen / mapOfDocLengths.size();
		System.out.println(avgLen);
		System.out.println("STEP 1 : Done");
	}

	public Map<String, Page> createRootSet(Map<String, Page>globalMap, List<String> mapOfDocScores){
		Map<String, Page> rootSet = new LinkedHashMap<String, Page>();
		for(String l : mapOfDocScores){

			if(globalMap.containsKey(l))
				rootSet.put(l, globalMap.get(l));

			if(rootSet.size() == 1000)
				break;
		}
		return rootSet;
	}

	public Map<String, Page> createBaseSet(Map<String, Page> rootSet, Map<String, Page> globalMap) throws IOException{

		Map<String, Page> baseSet = new HashMap<String, Page>();
		baseSet.putAll(rootSet);

		while(baseSet.size() < 20000){
			for(Map.Entry<String, Page> entry : rootSet.entrySet()){	
				Set<String> outlinks = entry.getValue().getOut_links();
				for(String outlink : outlinks){
					if(globalMap.containsKey(outlink))
						baseSet.put(outlink, globalMap.get(outlink));
				}
			}

			System.out.println(baseSet.size());
			for(Map.Entry<String, Page> entry : rootSet.entrySet()){	

				Set<String> inlinks = entry.getValue().getIn_links();

				for(String s : inlinks){
					if(globalMap.containsKey(s))
						baseSet.put(s, globalMap.get(s));
				}

				/*ArrayList<String> arrLinks = new ArrayList<String>(inlinks);
			while(d < 50){

				int val = (int) Math.random();
				String l = arrLinks.get(val);
				baseSet.put(l, globalMap.get(l));
				d++;
			}
				 */		}
			rootSet.putAll(baseSet);
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("base_set_inlink_count.txt"));
		for(Map.Entry<String, Page> entry : baseSet.entrySet()){
			bw.write(entry.getKey()+" "+entry.getValue().getIn_links().size()+"\n");
		}
		return baseSet;

	}
}


