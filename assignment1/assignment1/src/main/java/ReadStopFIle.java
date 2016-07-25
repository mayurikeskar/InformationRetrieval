import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadStopFIle {
	
	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new FileReader("stoplist.txt"));
		List<String> stopList = new ArrayList<String>();
		String str = null;
		
		while((str = br.readLine()) != null){
			stopList.add(str);
		}
		
		String text = "share prices were higher at midday wednesday, as the market continued to be supported by tuesday's news of britain's largest takeover bid. "
				+ "shares surged higher from the outset of trading on follow-through buying from the previous session when the financial times-stock exchange 100-share index jumped "
				+ "nearly 60 points. but prices drifted back on a lack of further inspiration, dealers said. many traders blamed a london transport strike for containing activity. financier "
				+ "sir james goldsmith and fellow investors on tuesday announced a $21 billion offer for b.a.t industries plc, a diversified british conglomerate. selected sectors "
				+ "continued to find favor among investors, dealers said, with property, foods and insurance companies notably higher. at about 12:15 p.m., the financial times-stock "
				+ "exchange 100-share index was 7.3 points, or 0.3 percent, higher at 2,258.2, just above its midmorning minimum of 2,257.6. the morning peak of 2,272.7, a gain of "
				+ "21.8 points, came shortly after the start of official trading. volume was 539.1 million shares at midday compared with 335.7 million shares at the same"
				+ " time tuesday. ";
		
		String arr[] = text.split(" ");
		int len = 0;
		
		for(String s : arr){
			if(stopList.contains(s))
				continue;
			else
				len = len + 1;
		}
		
		System.out.println(len);
		
	}

}
