package com.neu.ir.algorithm;

public class AlgorithmRequestImpl implements AlgorithmRequest{

	static final double k = 0.5;
	static final double c = 1.5;
	static final double b = 0.75;
	static final double k_1 = 1.2;
	static final double k_2 = 1.2;
	static final double lambda = 0.3;
	static final double C = 1500;

	public double getOKAPITf(String docId, int rawTf, long docLen, double avgLen) {
		double ans1 = (float)docLen / 250;
		double okapiTf = (float)rawTf  / (rawTf + k + (c * ans1));
		return okapiTf;
	}

	public double getTFIDF(String docId, int rawTf, long docLen, double avgLen, double weight){
		double ans1 = (float)docLen / 250;
		double ans2 = (float)rawTf  / (rawTf + k + (c * ans1));
		double tfIdf = ans2 * weight;
		return tfIdf;

	}

	public double getBM25(String docId, int rawTf, long docLen, double avLen, double weight) {
		double ans0 = (float)docLen / 250;
		double ans1 = (float)(((k_1 + 1) * rawTf) / (k_1 * ((1-b) + (b * ans0)) + rawTf));
		//double ans2 = (float)((k_2 + 1) * rawTf) / (k_2 + rawTf);
		double bm25 = ans1  * weight;
		return bm25;
	}

	public double getLaplaceSmoothing(int rawTf, long docLen, long vocab){
		double laplaceProb = (float)(rawTf + 1) / (docLen + vocab);
		return Math.log(laplaceProb);
	}

	public double getJMScore(int rawTf, long docLen, long vocab, long ctf, long M) {
		double p_doc = (float)rawTf / docLen;
		double p_collection = (float)ctf / M;
		double p_interpolated = (lambda * p_doc) + ((1- lambda) * p_collection);
		return Math.log(p_interpolated);
	}
		
	public double getProximityScore(int range, double queryCount, long vocab, int docLength) {
		double temp = (float) (C - range) * queryCount/(docLength + vocab);
		return temp;
		
	}
}
