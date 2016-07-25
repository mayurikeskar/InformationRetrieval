package com.neu.ir.Document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestApp {
	public static void main(String[] args) throws Exception {

//		String[] queries = {"Founding Fathers", "independence war causes", "declaration of independence"};
//
//		IndexRetrieval ir = new IndexRetrieval();
//		BufferedWriter bw1 = new BufferedWriter(new FileWriter("score.txt"));
//		int g=152101;
//		for(String query : queries){
//			List<String> li = ir.getTopRecords(query);
//			int rank = 1;
//			for(String s : li){
//				bw1.write(g +" Q0 " +s+" "+rank+" "+rank+" Exp\n");
//				rank++;
//			}
//			g++;
//		}
//		bw1.close();
		
//		BufferedWriter bw = new BufferedWriter(new FileWriter("qrel.txt"));
//		for(int a=1; a<=3; a++){	
//			Map<String, Integer>[] arrOfRelevance1 = new Map[3];
//			BufferedReader br;
//
//			String[] rel1 = {"relevance"+a+"_dhruv.txt", "relevance"+a+"_mayuri.txt", "relevance"+a+"_alekhya.txt"};
//
//			String str;
//			Map<String, Integer> mapOfRelevance;
//			int i=0;
//			String queryID ="";
//			for(String s : rel1){	
//				mapOfRelevance = new HashMap<String, Integer>();
//
//				br = new BufferedReader(new FileReader(s));
//				while((str = br.readLine()) != null){
//					String[] st = str.split(" ");
//					mapOfRelevance.put(st[2], Integer.parseInt(st[3]));
//					queryID = st[0];
//				}
//				arrOfRelevance1[i] = mapOfRelevance;
//				i++;
//			}
//
//			Map<String, String> finaQrelMap = new HashMap<String, String>();
//
//			Map<String, Integer> start = arrOfRelevance1[0];
//
//			for(Map.Entry<String, Integer> entry : start.entrySet()){
//				String docno = entry.getKey();
//				if(arrOfRelevance1[1].containsKey(docno) && arrOfRelevance1[2].containsKey(docno)){
//
//					if(finaQrelMap.size() == 200)
//						break;
//					int h = entry.getValue();
//					int j = arrOfRelevance1[1].get(docno);
//					int k = arrOfRelevance1[2].get(docno);
//					int count = 0;
//					if(h == 0)
//						count++;
//
//					if(j==0)
//						count++;
//
//					if(k==0)
//						count++;
//
//					int avg = (h + j + k)/3;
//					if(count <= 1){
//						finaQrelMap.put(docno, 1+"#"+avg);
//					}
//					else
//						finaQrelMap.put(docno, 0+"#"+avg);
//				}
//			}
//
//			System.out.println("size of qrel : "+finaQrelMap.size());
//
//			for(Map.Entry<String, String> entry : finaQrelMap.entrySet()){
//				bw.write(queryID+" 0 "+entry.getKey()+" "+entry.getValue().split("#")[0]+" "+entry.getValue().split("#")[1]+"\n");
//			}
//
//		}
//		bw.close();
//		Thread.sleep(2000);

		Trec_Eval te = new Trec_Eval();
		if(args.length == 3){
			te.readQrelFile(args[1]);
			te.readTrecFile(args[2]);
			te.computePrecisionRecall(true);
		}
		else{
			te.readQrelFile(args[0]);
			te.readTrecFile(args[1]);
			te.computePrecisionRecall(false);
		}
	}
}
