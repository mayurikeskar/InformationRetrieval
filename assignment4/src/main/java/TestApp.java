import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import com.neu.ir.document.HitsNAuthority;
import com.neu.ir.document.Page;
import com.neu.ir.document.PageRank;
import com.neu.ir.document.Readwt2g;

public class TestApp {

	public static void main(String[] args) throws Exception {
		Readwt2g r = new Readwt2g();
		PageRank pr = new PageRank();
		HitsNAuthority ha = new HitsNAuthority();
		
		System.out.println("1. Compute Page Rank of wt2g file");
		System.out.println("2. Compute Page Rank of crawled data");
		System.out.println("3. Compute Hubs and Authority of crawled data");

		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br= new BufferedReader(isr);

		int option = Integer.parseInt(br.readLine());

		if(option == 1){
			Map<String, Page> globalMap = r.readFile("wt2g_inlinks.txt");
			pr.calculatePageRank(globalMap, "page_rank.txt");
		}

		if(option == 2){
			Map<String, Page> globalMap = r.readFile("merged_inlinks.txt");
			pr.calculatePageRank(globalMap, "page_rank_for_crawler.txt");
		}

		if(option == 3){
			Map<String, Page> globalMap = r.readFile("merged_inlinks.txt");
			System.out.println("Enter the query");
			String query = br.readLine();
			List<String> top1000docs = ha.getMapOfTexts(query);
			Map<String, Page> rootSet = ha.createRootSet(globalMap, top1000docs);
			Map<String, Page> baseSet = ha.createBaseSet(rootSet, globalMap);
			ha.compute(baseSet);
		}

	}
}