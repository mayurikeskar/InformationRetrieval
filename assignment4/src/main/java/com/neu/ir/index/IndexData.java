package com.neu.ir.index;

import java.io.IOException;
import java.util.Map;

public interface IndexData {
	
//	public void saveIndex(String fileName) throws IOException;
	
//	public void addIndex(String fileName) throws IOException;
	
	public void getIndex(String docId);

	public Map<String, Integer> getDocumentLength() throws IOException;
	
	public Map<String, Integer> getTermFrequency(String queryString);
	
//	public int getVocabulary();

	
}
