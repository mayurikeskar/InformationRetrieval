package com.neu.ir.document;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.text.StrTokenizer;

import com.neu.ir.util.MiscFunctions;
import com.neu.ir.util.ValueComparator;

public class DocumentIndexImpl extends Thread implements DocumentIndex{

	static Map<String, Integer> mapOfDocLengths = new HashMap<String, Integer>();
	static Map<String, String> mapOfDocIds = new HashMap<String, String>();
	int uniqueTerms = 0;
	static char textNo = 'a';
	static int name = 0;
	MiscFunctions mf;
	Set<String> stopList;
	static Map<String, Map<String, Long>> setOfOffsets = new HashMap<String, Map<String, Long>>();
	BufferedReader br = null;
	static Map<String, String> mapOfTermIds = new HashMap<String, String>();
	static int termId = 1;
	RandomAccessFile raf1 = null;
	RandomAccessFile raf2 = null;
	static int docCounter = 1;
	
	public DocumentIndexImpl() throws IOException{
		mf = new MiscFunctions();
		stopList = mf.getStopList();
	}

	public Map<String, Integer> getDocumentLengths(){
		return mapOfDocLengths;
	}

	public void indexDocument(List<Document> listOfDocuments) throws IOException{
		Map<String, Map<String, Term>> mapOfTermFrequencies = new HashMap<String, Map<String, Term>>();
		
		for(Document doc : listOfDocuments){
			mapOfDocIds.put(""+docCounter, doc.getDocId());
			
			int docLength = 0;
			Map<String, Term>mapForDoc = new HashMap<String, Term>();
			StrTokenizer st = new StrTokenizer(doc.getText(), " ");

			String stemWord = null;

			String s = null;
			int position = -1;
		 	while(st.hasNext()){
				//int count = 1;
				s = st.nextToken().toLowerCase();
				String[] strSpearatedByHyphen = s.split("-");

				StringBuilder sb;
				for(String str : strSpearatedByHyphen){
					sb =  new StringBuilder(str);
					position++;
					stemWord = preprocessString(sb);

					if(stemWord.equals(""))
						continue;	
					addToTermIdMap(stemWord);
					int count = 0;
					List<Integer> setOfPos = new ArrayList<Integer>();
					Term t = new Term();
					if(mapForDoc.containsKey(stemWord)){
						t = mapForDoc.get(stemWord);
						count = t.getTf();
						setOfPos = t.getPositions();
					}
					count = count + 1;
					setOfPos.add(new Integer(position));
					t.setTf(count);
					t.setPositions(setOfPos);
					mapForDoc.put(stemWord, t);
					t = null;
				}
			}

			for(Map.Entry<String, Term> entry : mapForDoc.entrySet()){
				docLength = docLength + entry.getValue().getTf();
				Map<String, Term> mapForATerm = new HashMap<String, Term>();
				if(mapOfTermFrequencies.containsKey(entry.getKey()))
					mapForATerm = mapOfTermFrequencies.get(entry.getKey());

				mapForATerm.put(""+docCounter, entry.getValue());
				mapOfTermFrequencies.put(entry.getKey(), mapForATerm);
			}
			mapOfDocLengths.put(""+docCounter, docLength);
			docCounter++;
		}
		writeDocMapToFile();
		writeToFile(mapOfTermFrequencies);
	}

	
	public void writeDocMapToFile() throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter("doc_map"));
		for(Map.Entry<String, String> entry : mapOfDocIds.entrySet()){
			bw.write(entry.getKey() + " " + entry.getValue());
			bw.newLine();
		}
		bw.flush();
		bw.close();
	}
	
	/**
	 * adds term and term id to the termId Map
	 * @param word
	 */
	private void addToTermIdMap(String word){
		if(!mapOfTermIds.containsKey(word)){
			mapOfTermIds.put(word, ""+termId);
			termId += 1;
		}
	}


	/**
	 * Pre-process a raw term and return a stemmed term
	 * @param term
	 * @return String
	 */
	private String preprocessString(StringBuilder term){
		if(term.equals("") || stopList.contains(term.toString()))
			return "";

		replaceString(term, ",", "");
		replaceString(term, "[", "");
		replaceString(term, "]", "");
		replaceString(term, "(", "");
		replaceString(term, "\"", "");
		replaceString(term, "'", "");
		replaceString(term ,")", "");
		replaceString(term, "!", "");
		replaceString(term, "/", "");
		replaceString(term, "_", "");
		replaceString(term, "~", "");
		replaceString(term, ":", "");
		replaceString(term, " ", "");
		replaceString(term, "`", "");
		replaceString(term, "?", "");
		replaceString(term, "|", "");
		replaceString(term, "{", "");
		replaceString(term, "}", "");
		replaceString(term, "@", "");
		replaceString(term, "^", "");
		replaceString(term, ";", "");
		replaceString(term, "\\", "");
		replaceString(term, "$", "");

		if(term.toString().equals("."))
			return "";
		
		if(term.toString().equals(""))
			return "";

		if(term.charAt(term.length()-1) == '.')
			term.deleteCharAt(term.length()-1);

		if(stopList.contains(term.toString()))
			return "";

		String stemWord = mf.stemTerm(term.toString());
		if(stemWord.equals(""))
			return "";
		return stemWord;
	}

	public StringBuilder replaceString(StringBuilder sb,
			String toReplace,
			String replacement) {      
		int index = -1;
		while ((index = sb.lastIndexOf(toReplace)) != -1) {
			sb.replace(index, index + toReplace.length(), replacement);
		}
		return sb;
	}


	public void writeToFile(Map<String, Map<String, Term>> mapOfTermFrequencies) throws IOException{
		Map<String, Long> mapOfOffsets = new HashMap<String, Long>();
		long offset_start = 0;
		long offset_end = 0;
		String term_id;
		long lineNo = 1;
		FileWriter fileWriter = new FileWriter("index/"+textNo+""+name);
		BufferedWriter writer = new BufferedWriter(fileWriter);
		for(Map.Entry<String, Map<String, Term>> entry : mapOfTermFrequencies.entrySet()){
			Map<String, Term> mapOfSortedValues = mf.sortByComparator(entry.getValue());
			int tempOffset = entry.getKey().getBytes().length;
			term_id = entry.getKey();
			writer.write(term_id);
			StringBuilder line;
			for(Map.Entry<String, Term> e : mapOfSortedValues.entrySet()){
				line = new StringBuilder(" "+e.getKey()+"#"+e.getValue().getTf());
				int first = e.getValue().getPositions().get(0);
				line.append("#"+first);
				int second =0;
				for(int i=1; i<e.getValue().getPositions().size(); i++){
					second = e.getValue().getPositions().get(i);
					line.append("#"+(second-first));
					first = second;
				}
				tempOffset = tempOffset + line.toString().getBytes().length;
				writer.write(line.toString());
				line.setLength(0);
			}	
			offset_end = offset_start + tempOffset;
			//			bo = new ByteOffset();
			//			bo.setEndOffset(offset_end);
			//			bo.setStartOffset(offset_start);
			//			bo.setLineNumber(lineNo);
			mapOfOffsets.put(term_id, offset_start);
			writer.write("\n");
			offset_start = offset_end + 1;
			//lineNo ++;
		}
		writer.flush();
		writer.close();
		prepareMapOfOffsetValues(mapOfOffsets);

		if(name == 9){
			textNo = (char) (textNo +1);
			name = 0;
		}
		name++;
	}

	public void prepareMapOfOffsetValues(Map<String, Long> mapOfOffsets){
		setOfOffsets.put(""+textNo+name, mapOfOffsets);
	}

	public void mergeFiles(File f1, File f2) throws IOException { 
		File f = new File("index//"+f1.getName()+f2.getName());
		FileWriter fwriter = new FileWriter(f);
		BufferedWriter fw = new BufferedWriter(fwriter);

		raf1 = new RandomAccessFile(f1.getAbsolutePath(), "r");
		raf2 = new RandomAccessFile(f2.getAbsolutePath(), "r");
		Map<String, Long> mapOfFirstFile = setOfOffsets.get(f1.getName());
		Map<String, Long> mapOfSecondFile = setOfOffsets.get(f2.getName());
		LinkedHashMap<String, Term> map2 = null;
		LinkedHashMap<String, Term> map1 = null;
		Map<String, Long> mapOfOffsets = new HashMap<String, Long>();
		long start_offset = 0;
		long end_offset = 0;
		int temp_offset = 0;
		//System.out.println("Size of map before"+mapOfSecondFile.size());

		// iterate through the first file's catalog and write the merged file
		for(Map.Entry<String, Long> entry : mapOfFirstFile.entrySet()){
			String term = entry.getKey();

			String row = getStringFromOffset(raf1, entry.getValue(), f1);
			map1 = new LinkedHashMap<String, Term>();
			map2 = new LinkedHashMap<String, Term>();

			StringBuilder record = new StringBuilder(term+" ");

			if(mapOfSecondFile.containsKey(term)){

				String split1[] = row.split(" ");
				for(int i=1; i<split1.length; i++){
					String temp1[] = split1[i].split("#");
					Term t = new Term();
					List<Integer> li = new ArrayList<Integer>();
					for(int j = 2; j<temp1.length; j++){
						li.add(Integer.parseInt(temp1[j]));
					}
					t.setTf(Integer.parseInt(temp1[1]));
					t.setPositions(li);
					map1.put(temp1[0], t);
				}

				long val = mapOfSecondFile.get(term);
				String ans = getStringFromOffset(raf2, val, f2);
				String split2[] = ans.split(" ");
				for(int i=1; i<split2.length; i++){
					String temp2[] = split2[i].split("#");
					Term t = new Term();
					List<Integer> li = new ArrayList<Integer>();
					for(int j = 2; j<temp2.length; j++){
						li.add(Integer.parseInt(temp2[j]));
					}
					t.setTf(Integer.parseInt(temp2[1]));
					t.setPositions(li);

					map2.put(temp2[0], t);
				}
				LinkedHashMap<String, Term> mapOfSortedDocs = mf.sortDocsByValues(map1, map2);


				for(Map.Entry<String, Term> en : mapOfSortedDocs.entrySet()){
					record.append(en.getKey()+"#"+en.getValue().getTf());
					List<Integer> li = en.getValue().getPositions();
					for(int i : li)
						record.append("#"+i);
					record.append(" ");
				}
				temp_offset = record.toString().getBytes().length;
				fw.write(record.toString());
				record.setLength(0);

			} else{
				temp_offset = row.getBytes().length;
				fw.write(row);
			}
			fw.write("\n");
			end_offset = start_offset + temp_offset;
			mapOfOffsets.put(term, start_offset);
			start_offset = end_offset + 1;
			mapOfSecondFile.remove(term);
		}
		fw.flush();
		//System.out.println("Size of map after"+mapOfSecondFile.size());

		long s_off = end_offset + 1;
		long e_off = 0;

		// for the terms remaining in the second catalog that were not in the first one
		for(Map.Entry<String, Long> en : mapOfSecondFile.entrySet()){
			String record = getStringFromOffset(raf2, en.getValue(), f2);
			fw.write(record+"\n");
			long temp = record.getBytes().length;
			e_off = s_off + temp;
			mapOfOffsets.put(en.getKey(), s_off);
			s_off = e_off + 1;
		}

		
		fw.close();
		fwriter.close();
		raf1.close();
		raf2.close();
		setOfOffsets.put(f1.getName()+f2.getName(), mapOfOffsets);
		setOfOffsets.remove(f1.getName());
		setOfOffsets.remove(f2.getName());
		f1.delete();
		f2.delete();
	}


	public String getStringFromOffset(RandomAccessFile raf, long start, File f) throws IOException{
		raf.seek(start);
		return raf.readLine();
		//raf.close();
	}


	public int calculateUniqueTerms(){
		return mapOfTermIds.size();
	}

	public int calculateAverageDocLength(){
		int avgLength = 0;
		for(Map.Entry<String, Integer> entry : mapOfDocLengths.entrySet()){
			avgLength = avgLength + entry.getValue();
		}
		return avgLength/mapOfDocLengths.size();
	}

	public int getSizeOfOffset(){
		return setOfOffsets.size();
	}


	public Map<String, Term> getProximitySearch(String query){
		File f = new File("index");
		String[] qTerms = query.split(" ");
		Map<String, Long> offsetMap = setOfOffsets.get(f.listFiles()[0].getName());
		return null;

	}

	RandomAccessFile raf = null;
	public Map<String, Integer> getTermFrequency(String term) throws IOException{
		File f = new File("index");
		Map<String, Integer> mapOfFreqs = new HashMap<String, Integer>();
		Map<String, Long> offsetMap = setOfOffsets.get(f.listFiles()[0].getName());
		String termId = mapOfTermIds.get(term);
		
		if(offsetMap.containsKey(term)){
			long start = offsetMap.get(term);
			raf = new RandomAccessFile(f.listFiles()[0].getAbsolutePath(), "r");
			raf.seek(start);
			String str = raf.readLine();
			String[] s = str.split(" ");
			String docId;
			int tf;
			String arr[];
			if(s[0].equals(term)){
				for(int i = 1; i<s.length; i++){
					arr =s[i].split("#");
					docId = arr[0];
					tf = Integer.parseInt(arr[1]);
					mapOfFreqs.put(docId, tf);
				}
			}
			raf.close();
			return mapOfFreqs;
		}
		else
			return mapOfFreqs;

	}

	public Map<String, String> getTermPositions(String term) throws IOException{
		File f = new File("index");
		Map<String, String> mapOfPositions = new HashMap<String, String>();
		Map<String, Long> offsetMap = setOfOffsets.get(f.listFiles()[0].getName());
		String termId = mapOfTermIds.get(term);
		if(offsetMap.containsKey(term)){
			long start = offsetMap.get(term);
			raf = new RandomAccessFile(f.listFiles()[0].getAbsolutePath(), "r");
			raf.seek(start);
			String str = raf.readLine();
			String[] s = str.split(" ");
			StringBuilder positions = new StringBuilder();
			String arr[];
			if(s[0].equals(term)){
				for(int i = 1; i<s.length; i++){
					arr = s[i].split("#");
					for(int j = 2; j<arr.length; j++)
						positions.append(arr[j]+"#");
					mapOfPositions.put(arr[0], positions.toString().substring(0, positions.toString().length()-1));
					positions.setLength(0);
				}
			}
			raf.close();
			return mapOfPositions;
		}
		else			
			return mapOfPositions;
	}

	public void ioCheck() throws IOException{
		MiscFunctions mf = new MiscFunctions();
		DocumentIndex docIn = new DocumentIndexImpl();
		BufferedWriter bw = new BufferedWriter(new FileWriter("out.4"));
		BufferedReader br = new BufferedReader(new FileReader("in.4"));
		String str = null;
		while((str = br.readLine()) != null){
			String stem = mf.stemTerm(str);
			Map<String, Integer> mapOfFrequency = docIn.getTermFrequency(stem);
			int docCount = 0;
			int termCount = 0;
			for(Map.Entry<String,Integer> entry : mapOfFrequency.entrySet()){
				docCount = mapOfFrequency.size();
				termCount = termCount + entry.getValue();
			}
			bw.write(str+ " "+ docCount + " "+ termCount+"\n");
		}
		br.close();
		bw.close();
	}

	public void fileWrite(Map<Integer, Map<String, Double>> mapOfDocScores, String algortihm) throws IOException{
		FileWriter fileWriter = new FileWriter(algortihm);
		BufferedWriter writer = new BufferedWriter(fileWriter);
		for(Map.Entry<Integer, Map<String, Double>> e : mapOfDocScores.entrySet()){
			Comparator<String> comparator = new ValueComparator<String, Double>(e.getValue());
			TreeMap<String, Double> result = new TreeMap<String, Double>(comparator);
			result.putAll(e.getValue());
			int rank = 1;
			for(Map.Entry<String, Double> entry : result.entrySet()){
			writer.write(e.getKey()+" Q0 "+mapOfDocIds.get(entry.getKey())+" "+rank+" "+entry.getValue()+" Exp");
			String s = mapOfDocIds.get("1");
				writer.newLine();
				rank = rank + 1;
				if(rank == 1001)
					break;
			}
		}
		writer.close();
	}
}

