package com.neu.ir.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Trec_Eval {

	BufferedReader br;
	static LinkedHashMap<String, LinkedHashMap<String, Integer>> mapOfQrels;
	static LinkedHashMap<String, LinkedHashMap<String, Double>> mapOfQrelsScores;
	static TreeMap<String, LinkedHashMap<String, Double>> mapOfTrecs;

	static double[] recalls = {0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
	static int[] cutoffs = {5, 10, 20, 50, 100};

	public Trec_Eval(){
		mapOfQrels = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
		mapOfQrelsScores = new LinkedHashMap<String, LinkedHashMap<String, Double>>();
		mapOfTrecs = new TreeMap<String, LinkedHashMap<String, Double>>();
	}

	public void readQrelFile(String fileName) throws Exception{

		br = new BufferedReader(new FileReader(fileName));
		String str;
		LinkedHashMap<String, Integer> listOfDocuments;	
		LinkedHashMap<String, Double> listOfScores;
		while((str = br.readLine()) != null){
			String[] line = str.split(" ");
			if(mapOfQrels.containsKey(line[0])){
				listOfDocuments = mapOfQrels.get(line[0]);
				listOfScores = mapOfQrelsScores.get(line[0]);
			}
			else{
				listOfDocuments = new LinkedHashMap<String, Integer>();
				listOfScores = new LinkedHashMap<String, Double>();
			}
			
			listOfDocuments.put(line[2], Integer.parseInt(line[3]));
			listOfScores.put(line[2], Double.parseDouble(line[4]));
			mapOfQrels.put(line[0], listOfDocuments);
			mapOfQrelsScores.put(line[0], listOfScores);
		}
	}

	public void readTrecFile(String fileName) throws Exception {

		br = new BufferedReader(new FileReader(fileName));
		String str;
		LinkedHashMap<String, Double> listOfDocuments;	
		while((str = br.readLine()) != null){
			String[] line = str.split(" ");
			if(mapOfTrecs.containsKey(line[0]))
				listOfDocuments = mapOfTrecs.get(line[0]);
			else
				listOfDocuments = new LinkedHashMap<String, Double>();
			listOfDocuments.put(line[2], Double.parseDouble(line[4]));
			mapOfTrecs.put(line[0], listOfDocuments);
		}
	}


	public void computePrecisionRecall(boolean isExplain) throws IOException{

		Map<String, Double> mapOfTrecDocs;
		Map<String, Integer> mapOfQrelDocs;
		Map<String, Double> mapOfScores;
		
		double[] avg_prec_at_cutoffs = new double[100];
		double[] avg_prec_at_recalls = new double[100];
		double[] avg_f1_at_cutoffs = new double[100];
	
		double[] sum_prec_at_recall = new double[1001];
		double[] sum_prec_at_cutoffs = new double[1001];
		double[] sum_f1_at_cutoffs = new double[1001];

		double total_num_ret = 0;
		double total_num_rel = 0;
		double total_num_rel_ret = 0;

		double sum_avg_prec = 0;
		double sum_r_prec = 0;

		List<Double> listOfScores;	
		double total_dcg =0;
		
		// for every query id
		for(Map.Entry<String, LinkedHashMap<String, Double>> entry : mapOfTrecs.entrySet()){
			String queryID = entry.getKey();
			mapOfQrelDocs = mapOfQrels.get(queryID);
			mapOfScores = mapOfQrelsScores.get(queryID);
			mapOfTrecDocs = entry.getValue();
			listOfScores = new ArrayList<Double>();
			double[] precision = new double[1001];
			double[] recall = new double[1001];
			double[] f1 = new double[1001];
			
			double total_relevance = 0;
			for(Map.Entry<String, Integer> en: mapOfQrelDocs.entrySet()){
				if(en.getValue() == 1)
					total_relevance = total_relevance + 1;
			}

			// initialize num_retrieved, num_relevance and sum_prec for every query ID
			int num_ret = 0;
			double num_ret_rel = 0;
			double sum_prec = 0;
			for(Map.Entry<String, Double> en : mapOfTrecDocs.entrySet()){
				
				if(mapOfScores.containsKey(en.getKey()))
					listOfScores.add(mapOfScores.get(en.getKey()));
				
				num_ret = num_ret + 1;
				double rel = 0;
				if(mapOfQrelDocs.containsKey(en.getKey()))
					rel = mapOfQrelDocs.get(en.getKey());

				if(rel == 1){
					double temp = (double)(1+num_ret_rel)/num_ret;
					sum_prec = sum_prec +  (rel * temp);
					num_ret_rel = num_ret_rel + rel;
				}
				double prec = (double)num_ret_rel/num_ret;
				double rec = (double)num_ret_rel/total_relevance;

				precision[num_ret] = prec;
				recall[num_ret] = rec;
				double num = (2*precision[num_ret] * recall[num_ret]);
				double den = (precision[num_ret] + recall[num_ret]);
				if(num == 0 && den == 0) 
					f1[num_ret] = 0;
				else
					f1[num_ret] = num/den;
				
				if(num_ret == 1000)
					break;
				
				writeToCSV(precision, recall, queryID);
			}
			
			double ndcg = ndcg(listOfScores);
			total_dcg = total_dcg + ndcg;
			
			double avg_prec = sum_prec/total_relevance;

			List<Double> prec_at_cutoffs = new ArrayList<Double>();
			List<Double> f1_at_cutoff = new ArrayList<Double>();


			for(int cutoff : cutoffs){
				prec_at_cutoffs.add(precision[cutoff]);
				f1_at_cutoff.add(f1[cutoff]);
			}

			double r_precision =0;
			// calculating R-precision
			if(total_relevance > num_ret)
				r_precision = num_ret_rel/total_relevance;
			else {
				int int_num_rel = (int) total_relevance;
				double frac_num_rel = total_relevance - int_num_rel;

				r_precision = (frac_num_rel > 0) 
						? (1 - frac_num_rel) * precision[int_num_rel] + frac_num_rel * precision[int_num_rel+1] 
								: precision[int_num_rel];
			}


			double max_prec = 0;
			for(int i =1000; i>=1; i--){
				if(precision[i] > max_prec)
					max_prec = precision[i];
				else
					precision[i] = max_prec;
			}

			List<Double> prec_at_recalls = new ArrayList<Double>();
			int i =1;
			for(Double r : recalls){
				while(i <=1000 && recall[i] < r){
					i++;
				}
				if(i <= 1000){
					prec_at_recalls.add(precision[i]);
				} else{
					prec_at_recalls.add(0.00);
				}
			}

			total_num_ret += num_ret;
			total_num_rel += total_relevance;
			total_num_rel_ret += num_ret_rel;

			for(int j =0; j<cutoffs.length; j++){
				double temp = sum_prec_at_cutoffs[j];
				sum_prec_at_cutoffs[j] = temp + prec_at_cutoffs.get(j);
			}
			
			for(int j =0; j<cutoffs.length; j++){
				double temp = sum_f1_at_cutoffs[j];
				sum_f1_at_cutoffs[j] = temp + f1_at_cutoff.get(j);
			}

			for(int j=0; j<recalls.length; j++){
				double temp = sum_prec_at_recall[j];
				sum_prec_at_recall[j] = temp + prec_at_recalls.get(j);
			}	

			sum_avg_prec = sum_avg_prec + avg_prec;
			//sum_avg_f1 = sum_avg_f1 + avg_f1;
			sum_r_prec = sum_r_prec + r_precision;
			
			if(isExplain){
				printDetails(1, sum_f1_at_cutoffs, num_ret, total_relevance, num_ret_rel, sum_prec_at_recall, sum_prec_at_cutoffs, sum_avg_prec, sum_r_prec, ndcg);
			}
			
		}

		for(int i=0; i<cutoffs.length; i++){
			avg_f1_at_cutoffs[i] = sum_f1_at_cutoffs[i]/mapOfTrecs.size();
		}
		
		for(int i=0; i<cutoffs.length; i++){
			avg_prec_at_cutoffs[i] = sum_prec_at_cutoffs[i]/mapOfTrecs.size();
		}

		for(int i=0; i<recalls.length; i++){
			avg_prec_at_recalls[i] = sum_prec_at_recall[i]/mapOfTrecs.size();
		}

		double mean_avg_prec = sum_avg_prec/mapOfTrecs.size();
		double mean_r_prec = sum_r_prec/mapOfTrecs.size();
		
		total_dcg = total_dcg/3;
		
		printDetails(mapOfTrecs.size(),avg_f1_at_cutoffs, total_num_ret, total_num_rel, total_num_rel_ret, avg_prec_at_recalls, avg_prec_at_cutoffs, mean_avg_prec, mean_r_prec, total_dcg);
	}


	public void printDetails(int num_topics,double[] avg_f1_at_cutoffs, double total_num_ret, double total_num_rel, double total_num_ret_rel, 
			double[] avg_prec_at_recalls, double[] avg_prec_at_cutoff, double mean_avg_prec, double mean_r_prec, double ndcg){
		
		System.out.println("Query id (Num): "+num_topics);
		System.out.println("Total number of documents over all queries");
		System.out.println("	Retrieved : "+total_num_ret);
		System.out.println("	Relevant : "+total_num_rel);
		System.out.println("	Ret_Rel : "+ total_num_ret_rel);
		System.out.println("Interpolated Recall - Precision Averages:");
		System.out.printf("	at 0.00 	%.4f\n", avg_prec_at_recalls[0]);
		System.out.printf("	at 0.10  	%.4f\n", avg_prec_at_recalls[1]);
		System.out.printf("	at 0.20  	%.4f\n", avg_prec_at_recalls[2]);
		System.out.printf("	at 0.30  	%.4f\n", avg_prec_at_recalls[3]);
		System.out.printf("	at 0.40  	%.4f\n", avg_prec_at_recalls[4]);
		System.out.printf("	at 0.50  	%.4f\n", avg_prec_at_recalls[5]);
		System.out.printf("	at 0.60  	%.4f\n", avg_prec_at_recalls[6]);
		System.out.printf("	at 0.70  	%.4f\n", avg_prec_at_recalls[7]);
		System.out.printf("	at 0.80  	%.4f\n", avg_prec_at_recalls[8]);
		System.out.printf("	at 0.90  	%.4f\n", avg_prec_at_recalls[9]);
		System.out.printf("	at 1.00  	%.4f\n", avg_prec_at_recalls[10]);

		System.out.println("Average precision (non-interpolated) for all rel docs(averaged over queries)");
		System.out.printf("%.4f\n", mean_avg_prec);

		System.out.println("Precision");
		System.out.printf("	At 5 docs : 	%.4f\n", avg_prec_at_cutoff[0]);
		System.out.printf("	At 10 docs : 	%.4f\n", avg_prec_at_cutoff[1]);
		System.out.printf("	At 20 docs : 	%.4f\n", avg_prec_at_cutoff[2]);
		System.out.printf("	At 50 docs : 	%.4f\n", avg_prec_at_cutoff[3]);
		System.out.printf("	At 100 docs : 	%.4f\n", avg_prec_at_cutoff[4]);
		System.out.println("R-Precision (precision after R (= num_rel for a query) docs retrieved):");  
		System.out.printf("%.4f\n", mean_r_prec);
		
		System.out.println("F1@k");
		System.out.printf("	At 5 docs : 	%.4f\n", avg_f1_at_cutoffs[0]);
		System.out.printf("	At 10 docs : 	%.4f\n", avg_f1_at_cutoffs[1]);
		System.out.printf("	At 20 docs : 	%.4f\n", avg_f1_at_cutoffs[2]);
		System.out.printf("	At 50 docs : 	%.4f\n", avg_f1_at_cutoffs[3]);
		System.out.printf("	At 100 docs : 	%.4f\n", avg_f1_at_cutoffs[4]);

			
		System.out.printf("nDCG : %.4f\n", ndcg);
	}
	
	 public double dcg(List<Double> scores) {
	        double dcg = scores.get(0);
	        for (int i = 1; i < scores.size(); ++i) {
	            dcg += scores.get(i) * Math.log(2) / Math.log(i + 1);
	        }
	        return dcg;
	    }

	    public double ndcg(List<Double> scores) {
	    	double dcg = dcg(scores);
	        Collections.sort(scores, Collections.reverseOrder());
	        double idcg = dcg(scores);
	        return dcg/idcg;
	    }
	    
	    
	    public void writeToCSV(double[] precision, double[] recall,String queryID){
	    	
	    	String csvFile = queryID+".csv";
	    	try {
				FileWriter fw = new FileWriter(csvFile);
				
				double prec_old = 1;
				double prec_new = 1;
				
				double recall_old = recall[0];
				double recall_new = recall[0];
				
				fw.append("PRECISION, RECALL\n");
				
				fw.append(prec_old+", "+recall_old+"\n");
				
				for(int i=1; i<precision.length; i++){
					recall_new = recall[i];
					prec_new = precision[i];
					
					if(recall_new < recall_old)
						recall_new = recall_old;
					
					if(prec_new > prec_old)
						prec_new = prec_old;
					
					fw.append(prec_new+", "+recall_new+"\n");	
					prec_old = prec_new;
					recall_old = recall_new;
				}
				
				fw.flush();
				fw.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
}
