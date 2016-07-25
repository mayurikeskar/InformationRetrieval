import java.io.UnsupportedEncodingException;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;

public class TestRobot {
	private static final String CRLF = "\r\n";
	public static void main(String[] args) throws UnsupportedEncodingException {

		final String simpleRobotsTxt1 = "User-agent: *" + CRLF + "Disallow: /fish" + CRLF;

		SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
		BaseRobotRules rule1  = robotParser.parseContent("www.domain.com", simpleRobotsTxt1.getBytes("UTF-8"), "text/plain", "RAN");
		System.out.println(rule1.isAllowed("http://www.fict.com/was"));
	}

}
