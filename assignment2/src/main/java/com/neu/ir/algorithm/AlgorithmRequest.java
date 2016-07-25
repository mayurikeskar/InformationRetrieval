package com.neu.ir.algorithm;

public interface AlgorithmRequest {
	
	public double getOKAPITf(String docId, int tf, long docLen, double avgLen);

	public double getTFIDF(String docId, int rawTf, long docLen, double avgLen, double weight);
	
	public double getBM25(String docId, int rawTf, long docLen, double avdLen, double weight);
	
	public double getLaplaceSmoothing(int rawTf, long docLen, long vocab);
	
	public double getJMScore(int rawTf, long docLen, long vocab, long ctf, long M);

	public double getProximityScore(int range, double queryCount, long vocab, int docLength);
}
