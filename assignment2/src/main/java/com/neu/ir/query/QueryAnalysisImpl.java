package com.neu.ir.query;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.neu.ir.algorithm.AlgorithmRequest;
import com.neu.ir.algorithm.AlgorithmRequestImpl;
import com.neu.ir.document.DocumentIndex;
import com.neu.ir.document.DocumentIndexImpl;
import com.neu.ir.util.MiscFunctions;
import com.neu.ir.util.ValueComparator;

public class QueryAnalysisImpl implements QueryAnalysis{
	MiscFunctions mf = null;
	DocumentIndex docIn = null;
	AlgorithmRequest ar = null;

	static Map<String, Integer> mapOfDocLengths = new HashMap<String, Integer>();
	Map<Integer, Map<String, Double>> listOfDocScores = new HashMap<Integer, Map<String, Double>>();
	static double avgLen = 0.0;
	public QueryAnalysisImpl() throws IOException{
		mf = new MiscFunctions();
		docIn = new DocumentIndexImpl();
	}

	public void analyzeTFIDF(String fileName) throws IOException{
		computeDocLengths();
		//System.out.println("STEP 2 : Parsing the query file");
		List<Query> listOfQueries = mf.prepareQuery(fileName);

		for(Query q : listOfQueries){
			Map<String, Double> mapOfDocScores = new HashMap<String, Double>();
			ar = new AlgorithmRequestImpl();
			String replacedQuery;
			for(String query : q.getQueryTerms()){
				if(!query.equalsIgnoreCase("U.S."))
					replacedQuery = query.replace(",", "").replace(".", "").replace(")", "").replace("(", "").replaceAll("\"", "");
				else
					replacedQuery = query;
				replacedQuery = mf.stemTerm(replacedQuery);
				Map<String, Integer> mapOfTermFreqs = docIn.getTermFrequency(replacedQuery);

				double weight = computeIDF(mapOfTermFreqs.size());
				for(Map.Entry<String, Integer> entry : mapOfTermFreqs.entrySet()){
					String docId = entry.getKey();
					int rawTf = entry.getValue();
					int docLen = mapOfDocLengths.get(docId);
					double okapiTf  = ar.getTFIDF(docId, rawTf, docLen, 250, weight);
					if(mapOfDocScores.containsKey(docId)){
						double currTf = mapOfDocScores.get(docId);
						currTf = currTf + okapiTf;
						mapOfDocScores.put(docId, currTf);
					} else
						mapOfDocScores.put(docId, okapiTf);
				}
			}

			listOfDocScores.put(q.getQueryNo(), mapOfDocScores);
		}
	//	System.out.println("STEP 2 : Done");
		docIn.fileWrite(listOfDocScores, "TF-IDF");
	}


	public void analyzeBM25(String fileName) throws IOException{
		computeDocLengths();
		//System.out.println("STEP 2 : Parsing the query file");
		List<Query> listOfQueries = mf.prepareQuery(fileName);
		String replacedQuery;
		for(Query q : listOfQueries){
			Map<String, Double> mapOfDocScores = new HashMap<String, Double>();
			ar = new AlgorithmRequestImpl();
			for(String query : q.getQueryTerms()){
				if(!query.equalsIgnoreCase("U.S."))
					replacedQuery = query.replace(",", "").replace(".", "").replace(")", "").replace("(", "").replaceAll("\"", "");
				else
					replacedQuery = query;
				replacedQuery = mf.stemTerm(replacedQuery);
				Map<String, Integer> mapOfTermFreqs = docIn.getTermFrequency(replacedQuery);
				double weight = computeIDF(mapOfTermFreqs.size());
				for(Map.Entry<String, Integer> entry : mapOfTermFreqs.entrySet()){
					String docId = entry.getKey();
					int rawTf = entry.getValue();
					int docLen = mapOfDocLengths.get(docId);
					double okapiTf  = ar.getBM25(docId, rawTf, docLen, avgLen, weight);
					if(mapOfDocScores.containsKey(docId)){
						double currTf = mapOfDocScores.get(docId);
						currTf = currTf + okapiTf;
						mapOfDocScores.put(docId, currTf);
					} else
						mapOfDocScores.put(docId, okapiTf);
				}
			}
			listOfDocScores.put(q.getQueryNo(), mapOfDocScores);
		}
	//	System.out.println("STEP 2 : Done");
		docIn.fileWrite(listOfDocScores, "BM25");
	}


	public void analyzeLaplaceSmoothingProb(String fileName) throws IOException{
		computeDocLengths();
		//System.out.println("STEP 2 : Parsing the query file");
		List<Query> listOfQueries = mf.prepareQuery(fileName);
		String replacedQuery;
		for(Query q : listOfQueries){
			Map<String, Double> mapOfDocScores = new HashMap<String, Double>();
			ar = new AlgorithmRequestImpl();
			for(String query : q.getQueryTerms()){
				if(!query.equalsIgnoreCase("U.S."))
					replacedQuery = query.replace(",", "").replace(".", "").replace(")", "").replace("(", "").replaceAll("\"", "");
				else
					replacedQuery = query;
				replacedQuery = mf.stemTerm(replacedQuery);
				Map<String, Integer> mapOfTermFreqs = docIn.getTermFrequency(replacedQuery);
				int rawTf;
				for(Map.Entry<String, Integer> entry : mapOfDocLengths.entrySet()){
					String docId = entry.getKey();

					if(mapOfTermFreqs.containsKey(docId))
						rawTf = mapOfTermFreqs.get(docId);
					else
						rawTf = 0;

					int docLen = mapOfDocLengths.get(docId);
					double okapiTf  = ar.getLaplaceSmoothing(rawTf, docLen, 178050);
					if(mapOfDocScores.containsKey(docId)){
						double currTf = mapOfDocScores.get(docId);
						currTf = currTf + okapiTf;
						mapOfDocScores.put(docId, currTf);
					} else
						mapOfDocScores.put(docId, okapiTf);
				}
			}
			
			listOfDocScores.put(q.getQueryNo(), mapOfDocScores);
		}
		//System.out.println("STEP 2 : Done");
		docIn.fileWrite(listOfDocScores, "LAPLACE");
	}



	public void analyzeTF(String fileName) throws IOException{
		computeDocLengths();
	//	System.out.println("STEP 2 : Parsing the query file");
		List<Query> listOfQueries = mf.prepareQuery(fileName);
		for(Query q : listOfQueries){
			Map<String, Double> mapOfDocScores = new HashMap<String, Double>();
			ar = new AlgorithmRequestImpl();
			String replacedQuery;
			for(String query : q.getQueryTerms()){
				double score = 0;
				if(!query.equalsIgnoreCase("U.S."))
					replacedQuery = query.replace(",", "").replace(".", "").replace(")", "").replace("(", "").replaceAll("\"", "");
				else
					replacedQuery = query;
				replacedQuery = mf.stemTerm(replacedQuery);
				Map<String, Integer> mapOfTermFreqs = docIn.getTermFrequency(replacedQuery);
				for(Map.Entry<String, Integer> entry : mapOfTermFreqs.entrySet()){
					String docId = entry.getKey();
					int rawTf = entry.getValue();
					int docLen = mapOfDocLengths.get(docId);
					//System.out.println(docId + "-" +docLen);
					double okapiTf  = ar.getOKAPITf(docId, rawTf, docLen, avgLen);
					if(mapOfDocScores.containsKey(docId)){
						double currTf = mapOfDocScores.get(docId);
						currTf = currTf + okapiTf;
						mapOfDocScores.put(docId, currTf);
					} else
						mapOfDocScores.put(docId, okapiTf);
					score = score + okapiTf;
				}
			}

			listOfDocScores.put(q.getQueryNo(), mapOfDocScores);
		}
		//System.out.println("STEP 2 : Done");
		docIn.fileWrite(listOfDocScores, "TF");
	}


	public void analyzeJMScore(String fileName) throws IOException {
		computeDocLengths();
	//	System.out.println("STEP 2 : Parsing the query file");
		List<Query> listOfQueries = mf.prepareQuery(fileName);
		String replacedQuery;
		for(Query q : listOfQueries){
			Map<String, Double> mapOfDocScores = new HashMap<String, Double>();
			ar = new AlgorithmRequestImpl();
			for(String query : q.getQueryTerms()){
				if(!query.equalsIgnoreCase("U.S."))
					replacedQuery = query.replace(",", "").replace(".", "").replace(")", "").replace("(", "").replaceAll("\"", "");
				else
					replacedQuery = query;
				replacedQuery = mf.stemTerm(replacedQuery);
				Map<String, Integer> mapOfTermFreqs = docIn.getTermFrequency(replacedQuery);

				int ctf = 0;
				for(Map.Entry<String, Integer> entry : mapOfTermFreqs.entrySet()){
					ctf = ctf + entry.getValue();
				}

				int rawTf;
				for(Map.Entry<String, Integer> entry : mapOfDocLengths.entrySet()){
					String docId = entry.getKey();

					if(mapOfTermFreqs.containsKey(docId))
						rawTf = mapOfTermFreqs.get(docId);
					else
						rawTf = 0;

					int docLen = mapOfDocLengths.get(docId);
					double okapiTf  = ar.getJMScore(rawTf, docLen, 178050, ctf, 20972402);
					if(mapOfDocScores.containsKey(docId)){
						double currTf = mapOfDocScores.get(docId);
						currTf = currTf + okapiTf;
						mapOfDocScores.put(docId, currTf);
					} else
						mapOfDocScores.put(docId, okapiTf);
				}
			}
			listOfDocScores.put(q.getQueryNo(), mapOfDocScores);
		}
	//	System.out.println("STEP 2 : Done");
		docIn.fileWrite(listOfDocScores, "JMSCORE");

	}

	/*public void writeToFile(Map<Integer, Map<String, Double>> mapOfDocScores, String algortihm) throws IOException{
		FileWriter fileWriter = new FileWriter(algortihm);
		BufferedWriter writer = new BufferedWriter(fileWriter);
		for(Map.Entry<Integer, Map<String, Double>> e : mapOfDocScores.entrySet()){
			Comparator<String> comparator = new ValueComparator<String, Double>(e.getValue());
			TreeMap<String, Double> result = new TreeMap<String, Double>(comparator);
			result.putAll(e.getValue());
			int rank = 1;
			for(Map.Entry<String, Double> entry : result.entrySet()){
				writer.write(e.getKey()+" Q0 "+entry.getKey()+" "+rank+" "+entry.getValue()+" Exp");
				writer.newLine();
				rank = rank + 1;
				if(rank == 1001)
					break;
			}
		}
		writer.close();
	}*/

	public void computeDocLengths() throws IOException{
	//	System.out.println("STEP 1 : Computing document lengths and avg document length");
		mapOfDocLengths = docIn.getDocumentLengths();
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
		//System.out.println("SUM" + avgLen);
		avgLen  = (float)avgLen / mapOfDocLengths.size();
		System.out.println(avgLen);
	//	System.out.println("STEP 1 : Done");
	}

	public double computeIDF(int termCount){
		double weight;
		double temp= (float)(mapOfDocLengths.size() - termCount + 0.5)/ (termCount + 0.5);
		weight = Math.log(temp);
		return weight;
	}

	public void analyzeProximity(String fileName) throws IOException {

		computeDocLengths();
		List<Query> listOfQueries = mf.prepareQuery(fileName);
		String replacedQuery;
		for(Query q : listOfQueries){
			Map<String, Double> mapOfDocScores = new HashMap<String, Double>();
			ar = new AlgorithmRequestImpl();
			for(String query : q.getQueryTerms()){
				replacedQuery = query;
				replacedQuery = mf.stemTerm(replacedQuery);
				Map<String, Integer> mapOfTermFreqs = docIn.getTermFrequency(replacedQuery);
				double weight = computeIDF(mapOfTermFreqs.size());
				for(Map.Entry<String, Integer> entry : mapOfTermFreqs.entrySet()){
					String docId = entry.getKey();
					int rawTf = entry.getValue();
					int docLen = mapOfDocLengths.get(docId);
					double okapiTf  = ar.getBM25(docId, rawTf, docLen, avgLen, weight);
					if(mapOfDocScores.containsKey(docId)){
						double currTf = mapOfDocScores.get(docId);
						currTf = currTf + okapiTf;
						mapOfDocScores.put(docId, currTf);
					} else
						mapOfDocScores.put(docId, okapiTf);
				}
			}

			Map<String, Double> m = calculateProximityForQuery(mapOfDocScores.keySet(), q.getQueryTerms());
			listOfDocScores.put(q.getQueryNo(), m);
		}
		docIn.fileWrite(listOfDocScores, "PROXIMITY");

	}

	public Map<String, Double> calculateProximityForQuery(Set mapOfDocScores, ArrayList<String> queryTerms) throws IOException{
		Map<String, Map<String, String>> superMap = new HashMap<String, Map<String, String>>();
		String replacedQuery;
		long volcabulary = docIn.calculateUniqueTerms();
		int counter = 0;
		int docLen = 0;
		for(String query : queryTerms){
			replacedQuery = mf.stemTerm(query);
			Map<String, String> mapOfPositions = docIn.getTermPositions(replacedQuery);
			Map<String, String> m = null;
			String docId = null;
			for(Map.Entry<String, String> e : mapOfPositions.entrySet()){
				docId = e.getKey();
				docLen = mapOfDocLengths.get(docId);
		//		if(mapOfDocScores.contains(docId)){
					m = new HashMap<String, String>();
					if(superMap.containsKey(docId))
						m = superMap.get(docId);
					m.put(query, e.getValue());
					superMap.put(docId, m);
					//mapOfDocScores.remove(docId);
			//	}
			}
			//System.out.println(mapOfDocScores.size());
			counter++;
		}
		//System.out.println();
		Map<String, Double> results = new HashMap<String, Double>();
		double score = 0;
		for(Map.Entry<String, Map<String, String>> e : superMap.entrySet()){
			int range = mf.implementProxitmity(e.getValue());
			docLen = mapOfDocLengths.get(e.getKey());
			score = ar.getProximityScore(range, e.getValue().size(), volcabulary, docLen);
			//System.out.println(e.getKey() + " "+ score);
			results.put(e.getKey(), score);
		}
		return results;
	}

}