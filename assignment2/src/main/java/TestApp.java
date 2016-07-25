import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.neu.ir.document.Document;
import com.neu.ir.document.DocumentIndex;
import com.neu.ir.document.DocumentIndexImpl;
import com.neu.ir.query.QueryAnalysis;
import com.neu.ir.query.QueryAnalysisImpl;
import com.neu.ir.util.MiscFunctions;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_ADDPeer;

public class TestApp {

	public static void main(String[] args) throws IOException {

		MiscFunctions mf = new MiscFunctions();	

		long t1 = System.currentTimeMillis();
		DocumentIndex docIn = new DocumentIndexImpl();	
		String folderName = "ap89_collection";
		File folder = new File(folderName);
		List<Document> listOfDocuments = new ArrayList<Document>();

		for(File file : folder.listFiles()){
			List<Document> listPerFile = mf.prepareDocument(file.getAbsolutePath());
			listOfDocuments.addAll(listPerFile);
		}


//		docIn.indexDocument(listOfDocuments);
		docIn.indexDocument(listOfDocuments.subList(0, 3000));
		for(int i = 3000; i<listOfDocuments.size(); i=i+3000){
			if(i + 3000 < listOfDocuments.size()){
				docIn.indexDocument(listOfDocuments.subList(i, i+3000));
			} else{
				int j = listOfDocuments.size() - i;
				docIn.indexDocument(listOfDocuments.subList(i, i + j));
			}
		}


		int c = 0;
		File output = new File("index");
		int len = output.listFiles().length;
		while(output.listFiles().length != 1){
			docIn.mergeFiles(output.listFiles()[c], output.listFiles()[c+1]);
			c = c + 1;
			if(c + 1 >= output.listFiles().length)
				c = 0;
		}

		//int avgLength = docIn.calculateAverageDocLength();
		int uniqueTerms = docIn.calculateUniqueTerms();

		//MainClass.main(args);

		docIn.ioCheck();
		
		String queryFile = "query_desc.51-100.short.txt";
		QueryAnalysis qa = new QueryAnalysisImpl();
		qa.analyzeTFIDF(queryFile);
		qa.analyzeBM25(queryFile);
		qa.analyzeTF(queryFile);
		qa.analyzeLaplaceSmoothingProb(queryFile);
		qa.analyzeJMScore(queryFile);
		qa.analyzeProximity(queryFile);

		long t2 = System.currentTimeMillis();
		System.out.println("Time taken : "+ (t2-t1)/1000+" secs");
	}
}