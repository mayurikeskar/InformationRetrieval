package com.neu.ir.document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Readwt2g {

	BufferedReader br;
	Page p;
	Map<String, Page> mapOfAllPages;

	public Readwt2g(){
		mapOfAllPages = new HashMap<String, Page>();	
	}

	Map<String, Integer> mapOfInlinks = new HashMap<String, Integer>();
	public Map<String, Page> readFile(String fileName) throws IOException {

		br = new BufferedReader(new FileReader(fileName));
		String s;
		while((s = br.readLine()) != null){
			String arr[] = s.split(" ");
			p = new Page();
			p.setDocno(arr[0]);
			Set<String> inlinks = new HashSet<String>();
			mapOfInlinks.put(arr[0], arr.length-1);
			for(int i =1; i<arr.length; i++)
				inlinks.add(arr[i]);
			p.setIn_links(inlinks);
			mapOfAllPages.put(arr[0], p);
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("inlink_count.txt"));
		for(Map.Entry<String, Integer> entry : mapOfInlinks.entrySet()){
			bw.write(entry.getKey()+" "+entry.getValue()+"\n");
		}
		bw.close();
		return prepareOutlinks();
		//	return prepareText();
	}


	public Map<String, Page> prepareOutlinks() {

		Set<String> outlinks = null;
		Set<String> inlinks = null;
		for(Map.Entry<String, Page> entry : mapOfAllPages.entrySet()){

			inlinks = entry.getValue().getIn_links();
			Page p;
			for(String inlink : inlinks){
				p = mapOfAllPages.get(inlink);
				if(p == null)
					continue;
				outlinks = p.getOut_links();
				outlinks.add(entry.getKey());
				p.setOut_links(outlinks);
				mapOfAllPages.put(inlink, p);
			}
		}
		return mapOfAllPages;
	}


	/*public Map<String, Page> prepareText() throws IOException{

		br = new BufferedReader(new FileReader("merged_inlinks_with_text.txt"));
		String s;
		while((s = br.readLine()) != null){

			String docno = s.trim();
			String text = br.readLine();
			Page p;
			if(mapOfAllPages.containsKey(docno)){
				p  = mapOfAllPages.get(docno);
				p.setText(text);
			}	
		}

		br.close();
		return mapOfAllPages;

	}
	 */
}
