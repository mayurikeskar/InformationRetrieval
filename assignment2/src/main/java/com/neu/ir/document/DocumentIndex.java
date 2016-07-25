package com.neu.ir.document;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DocumentIndex {

	public void indexDocument(List<Document> listOfDocuments) throws IOException;
	
	public int calculateAverageDocLength();
	
	public int calculateUniqueTerms();
	
	public void writeToFile(Map<String, Map<String, Term>> mapOfTermFrequencies) throws IOException;
	
	public void mergeFiles(File f1, File f2) throws IOException;
	
	public Map<String, Integer> getTermFrequency(String term) throws IOException;
	
	public Map<String, Integer> getDocumentLengths();
	
	public Map<String, String> getTermPositions(String term) throws IOException;
	
	//public String getStringFromOffset(long startOffset, File f) throws IOException;
	
	public void fileWrite(Map<Integer, Map<String, Double>> mapOfDocScores, String algortihm) throws IOException;
	
	public void ioCheck() throws IOException;
	
	public int getSizeOfOffset();
}
