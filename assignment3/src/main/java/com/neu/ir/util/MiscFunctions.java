package com.neu.ir.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;

public class MiscFunctions {

	SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
	static List<String> keyWords = new ArrayList<String>();


	public void fillKeyWords(){
		keyWords.add("independence");
		keyWords.add("revolution");
		keyWords.add("revolutionary");
		keyWords.add("america");
		keyWords.add("american");
		keyWords.add("war");
		keyWords.add("george");
		keyWords.add("washington");
		keyWords.add("britain");
		keyWords.add("british");
		keyWords.add("saratoga");
		keyWords.add("boston");
		keyWords.add("massachusetts");
		keyWords.add("new york");
		keyWords.add("virginia");
		keyWords.add("east india company");
		keyWords.add("1778");
		keyWords.add("1777");
		keyWords.add("1775");
		keyWords.add("1776");
		keyWords.add("1779");
		keyWords.add("1780");
		keyWords.add("1781");
		keyWords.add("1782");
		keyWords.add("1783");
		keyWords.add("united states");
		keyWords.add("lexington");
		keyWords.add("sir william howe");
		keyWords.add("nathanael greene");
		keyWords.add("horatio gates");
		keyWords.add("lord cornwallis");
		keyWords.add("bunker hill");

	}

	public String getRobotFileFromURL(String url) throws Exception{
		try{
			URL urlRobot = new URL("http://"+url+"/robots.txt");
			HttpURLConnection huc = (HttpURLConnection)urlRobot.openConnection (); 
			huc.setRequestMethod ("HEAD");  
			huc.connect () ; 
			int code = huc.getResponseCode() ;
			if(code != 404 || code != 403){
				InputStream urlRobotStream = urlRobot.openStream();
				String str;
				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(new InputStreamReader(urlRobotStream, "UTF-8"));
				while((str = br.readLine()) != null)
					sb.append(str+"\r\n");
				return sb.toString();
			}
		}catch(Exception e){
			System.out.println("Could not read the robots.txt of :"+url);
			return "";
		}
		return "";
	}


	public BaseRobotRules checkForRobotRules(String baseUri) throws Exception{
		String content = getRobotFileFromURL(baseUri);
		if(content.equals(""))
			return null;
		BaseRobotRules brr = robotParser.parseContent("www.domain.com", content.getBytes("UTF-8"), "text/plain", "robot");
		return brr;
	}


	public String checkGarbageLinks(String url){

		String baseUri = url.toLowerCase();
		if(baseUri.contains("#"))
			return "";

		if(baseUri.contains("wikipedia.org") && (baseUri.contains("type=review") || baseUri.contains("protection_policy")
				|| baseUri.contains("pending_changes") || baseUri.contains("disambiguation")))
			return "";
		if(baseUri.endsWith(".jpg") || baseUri.endsWith(".pdf") || baseUri.endsWith(".png")
				|| baseUri.endsWith(".ogg") || baseUri.endsWith(".svg") || baseUri.endsWith(".php") ||
				baseUri.endsWith(".asp") || baseUri.endsWith(".gif"))
			return "";

		if(baseUri.contains("wikipedia:contact_us") || baseUri.contains("wikipedia:general_disclaimer") || 
				baseUri.contains("wikipedia:about") || baseUri.contains("terms_of_use") || baseUri.contains("privacy_policy")
				|| baseUri.contains("license") || baseUri.contains("cookie") || baseUri.contains("wikipedia:good_articles")
				|| baseUri.contains("special:recentchangeslinked") || baseUri.contains("bat-smg.wikipedia.org") || baseUri.contains("special:specialpages")
				|| baseUri.contains("www.mediawiki.org") || baseUri.contains("mailto:") || baseUri.contains("emailupdates") || baseUri.contains("advertise")
				|| baseUri.contains("contact") || baseUri.contains("privacy") || baseUri.contains("play.google") || baseUri.contains("wikipedia:citation_needed")
				|| baseUri.contains("template:campaignbox_american_revolutionary_war") || baseUri.contains("template_talk"))
			return "";

		return baseUri;
	}

	public String urlCanonicalization(String rawUrl) {

		int char2 = rawUrl.indexOf("?");
		if(char2 != -1)
			rawUrl = rawUrl.substring(0, char2);

		if(rawUrl.contains("https://"))
			rawUrl = rawUrl.substring(8, rawUrl.length());

		if(rawUrl.contains("http://"))
			rawUrl = rawUrl.substring(7, rawUrl.length());

		return rawUrl.toLowerCase();
	}


	public boolean containsKeyWords(String rawText){
		int count = 0;
		rawText = rawText.toLowerCase();
		for(String s : keyWords){
			if(rawText.contains(s))
				count++;
		}
		if(count >= 3)
			return true;
		else
			return false;
	}
}
