import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.neu.ir.index.IndexData;
import com.neu.ir.index.IndexDataImpl;
import com.neu.ir.query.QueryAnalysis;
import com.neu.ir.query.QueryAnalysisImpl;

public class TestApp {

	public static void main(String[] args) throws IOException {

		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		System.out.println("Would you like to index data or run the algorithms?");
		System.out.println("1. Index Data");
		System.out.println("2. Run Algorithms");

		String answer = br.readLine();
		IndexData data = new IndexDataImpl();

		if(answer.equals("1")){
			System.out.println("Enter the input path");
			//String folderName = "C://Users//mkeskar//Downloads//AP89_DATA//AP_DATA//ap89_collection";
			String folderName = br.readLine();
			File folder = new File(folderName);
			for(File file : folder.listFiles()){
				data.saveIndex(file.getAbsolutePath());
			}

		} if(answer.equals("2")){
			System.out.println("1. OkapiTF");
			System.out.println("2. TF-IDF");
			System.out.println("3. BM25");
			System.out.println("4. Laplace");
			System.out.println("5. JM");

			QueryAnalysis qa = new QueryAnalysisImpl();

			String ans = br.readLine();
			System.out.println("Enter query input path");
			String inputLink = br.readLine();
			switch(ans){
			case "1":
				qa.analyzeTF(inputLink);
				break;
			case "2":
				qa.analyzeTFIDF(inputLink);
				break;

			case "3":
				qa.analyzeBM25(inputLink);
				break;

			case "4":
				qa.analyzeLaplaceSmoothingProb(inputLink);
				break;

			case "5":
				qa.analyzeJMScore(inputLink);
				break;
			}
		}
	}
}
