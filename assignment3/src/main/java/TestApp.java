import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.neu.ir.Document.DocumentProcess;
import com.neu.ir.Document.DocumentProcessImpl;
import com.neu.ir.index.IndexData;
import com.neu.ir.index.IndexDataImpl;
import com.neu.ir.util.MiscFunctions;

public class TestApp {

	static final Logger logger = Logger.getLogger(TestApp.class);

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.properties");

		List<String> seeds = new ArrayList<String>();
		seeds.add("http://en.wikipedia.org/wiki/American_Revolutionary_War");
		seeds.add("http://en.wikipedia.org/wiki/United_States_Declaration_of_Independence");
		seeds.add("http://www.history.com/topics/american-revolution/american-revolution-history");
		seeds.add("http://en.wikipedia.org/wiki/Founding_Fathers_of_the_United_States");
		seeds.add("http://en.wikipedia.org/wiki/American_Revolution");
		seeds.add("http://www.revolutionary-war.net/causes-of-the-american-revolution.html");

		MiscFunctions mf = new MiscFunctions();
		DocumentProcess dp = new DocumentProcessImpl();
		IndexData id = new IndexDataImpl();

		
		mf.fillKeyWords();
		dp.insertSeeds(seeds);
		dp.retrievePage();

//		id.getAllRecords();
		
//		id.getMayuriData();
		
		id.dataForAss4();
	}
}
