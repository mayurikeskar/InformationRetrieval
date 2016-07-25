package com.neu.ir.query;

import java.io.IOException;

public interface QueryAnalysis {
	
	public void analyzeTFIDF(String fileName) throws IOException;
	
	public void analyzeTF(String fileName) throws IOException;
	
	public void analyzeBM25(String fileName) throws IOException; 
	
	public void analyzeLaplaceSmoothingProb(String fileName) throws IOException; 
	
	public void analyzeJMScore(String fileName) throws IOException;
	
	public void analyzeProximity(String fileName) throws IOException;
	
	public void computeDocLengths() throws IOException;
}
