import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.neu.ir.document.DocumentIndex;
import com.neu.ir.document.DocumentIndexImpl;
import com.neu.ir.document.Term;
import com.neu.ir.util.MiscFunctions;

public class MainClass {
	
	public static void main(String[] args) throws IOException {
		
		MiscFunctions mf = new MiscFunctions();
		DocumentIndex docIn = new DocumentIndexImpl();
		BufferedWriter bw = new BufferedWriter(new FileWriter("out.result.unstemmed.txt"));
		BufferedReader br = new BufferedReader(new FileReader("in.0.50.txt"));
		String str = null;
		while((str = br.readLine()) != null){
		//	str = mf.stemTerm(str);
			Map<String, Integer> mapOfFrequency = docIn.getTermFrequency(str);
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
}
