package com.neu.ir.Document;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.neu.ir.index.IndexData;
import com.neu.ir.index.IndexDataImpl;
import com.neu.ir.util.MiscFunctions;

import crawlercommons.robots.BaseRobotRules;

public class DocumentProcessImpl implements DocumentProcess{

	MiscFunctions mf;
	IndexData id;
	MagicHand mh;
	static final Logger logger = Logger.getLogger(DocumentProcessImpl.class);
	static Map<String, Page> setOfVisitedUrls = new HashMap<String, Page>();
	static Map<String, Boolean> mapOfRobotValues = new HashMap<String, Boolean>();
	static Map<String, Long> timingOfLastVisitedRecords = new HashMap<String, Long>();
//	static Map<String, Page> globalSetOfUrls = new HashMap<String, Page>();
	static Map<String, Integer> countOfDomain = new HashMap<String, Integer>();
	
	public DocumentProcessImpl() throws IOException{
		mh = new MagicHand();
		mf = new MiscFunctions();
		id = new IndexDataImpl();
	}

	// queue used to store all the URLs to be crawled
	static Frontier frontier = new Frontier();

	public void insertSeeds(List<String> seeds){
		Page p;
		String canonicalizedSeed;
		for(String seed : seeds){
			p = new Page();
			p.setRawUrl(seed);
			canonicalizedSeed = mf.urlCanonicalization(seed);
			p.setDocno(canonicalizedSeed);
			p.setTimeStamp(System.currentTimeMillis());
			p.setScore(Integer.MAX_VALUE);
			p.setDepth(0);
			frontier.enqueue(p);
		}
	}

	public void retrievePage() throws Exception {
		String baseUri;	
		Page parentPage;
		Page p;
		boolean isAllowed;
		while(frontier.hasItems()){
			parentPage = frontier.dequeue();
			
			if(countOfDomain.containsKey(parentPage.getDocno())){
				if(countOfDomain.get(parentPage.getDocno()) > 500){
					frontier.enqueue(parentPage);
					continue;
				}
			}

			if(setOfVisitedUrls.keySet().contains(parentPage.getDocno()))
				continue;

			isAllowed = updateRobotValues(parentPage.getRawUrl());
			if(isAllowed){
				try{
					Document document = null;
					
					try{
						if(parentPage.getRawUrl().startsWith("https://books.google.com/books"))
							document = null;
						long diff =0;
						String cUrl = mf.urlCanonicalization(parentPage.getRawUrl());
						if(timingOfLastVisitedRecords.containsKey(cUrl)){
							Long t = timingOfLastVisitedRecords.get(cUrl);
							diff = System.currentTimeMillis() - t; 
							timingOfLastVisitedRecords.put(cUrl, System.currentTimeMillis());
							if(diff < 1000)
								Thread.sleep(1000 - diff); 
						} else{
							timingOfLastVisitedRecords.put(cUrl, System.currentTimeMillis());
						}

						Connection c = Jsoup.connect(parentPage.getRawUrl()).header("Accept-Language", "en").timeout(10000);
						Connection.Response r = c.execute();
						parentPage.setHTTPheader(r.headers().toString());
						
						if(r.statusCode() != 200)
							document = null;
						else
							document = c.get();
						
					} catch(Exception ex){
						System.out.println("Connection failed for url :"+parentPage.getRawUrl());
						//frontier.enqueue(parentPage);
					}
					
					
					if(document == null)
						continue;
					Set<String> outgoingLinks = new HashSet<String>();

					// parse the document
					// save the raw html in parentPage
					String raw_text = document.body().text();
					if(!mf.containsKeyWords(raw_text)){
						System.out.println(parentPage.getDocno());
						continue;
					}
					
					if(!document.child(0).attr("lang").equals("en") && !document.child(0).attr("lang").equals("") && 
							!document.child(0).attr("lang").equals("en-US"))
						continue;
					
					parentPage.setText(raw_text);
					parentPage.setTitle(document.title());
					//parentPage.setHtml_Source(document.toString());
					//parentPage.setPageId(pageCount);

					String cUrl = mf.urlCanonicalization(parentPage.getRawUrl());
					parentPage.setDocno(cUrl);
					setOfVisitedUrls.put(cUrl, parentPage);
					
					if(countOfDomain.containsKey(cUrl)){
						int count = countOfDomain.get(cUrl)+1;
						countOfDomain.put(cUrl, count);
					}else
						countOfDomain.put(cUrl, 1);
					
					//globalSetOfUrls.put(cUrl, parentPage);
					logger.debug(parentPage.getRawUrl() +" -- "+ cUrl);

					// before adding the links of a page, verify if its a relevant page. 
					// Check its anchor text
					Elements links = document.select("a[href]");

					for(Element link : links){	
						baseUri = link.attr("abs:href");
	
						String canonicalizedUrl = mf.urlCanonicalization(baseUri);
						if(mf.checkGarbageLinks(canonicalizedUrl).equals(""))
							continue;

						if(canonicalizedUrl.equals(parentPage.getDocno()))
							continue;
						
						// check if a link is already visited or not. 
						// if visited, add the parent uri in the list of incoming links
						// if not, add it the setOfVisitedUrls map
						if(setOfVisitedUrls.keySet().contains(canonicalizedUrl)){
							Page p1 = setOfVisitedUrls.get(canonicalizedUrl);
							p1.setDocno(canonicalizedUrl);
							Set<String> tempIncomingLinks = p1.getIn_links();
							tempIncomingLinks.add(parentPage.getDocno());
							p1.setIn_links(tempIncomingLinks);
							p1.setScore(tempIncomingLinks.size());
							p1.setTimeStamp(System.currentTimeMillis());
							setOfVisitedUrls.put(canonicalizedUrl, p1);
							Page temp = frontier.contains(baseUri);
							if(temp != null)
								frontier.update(p1, temp);

						} else{
							p = new Page();
							Set<String> tempIncomingLinks = new HashSet<String>();
							tempIncomingLinks.add(parentPage.getDocno());
							p.setDocno(canonicalizedUrl);
							p.setDepth(parentPage.getDepth()+1);
							p.setRawUrl(baseUri);
							p.setIn_links(tempIncomingLinks);
							p.setScore(tempIncomingLinks.size());
							p.setTimeStamp(System.currentTimeMillis());
							frontier.enqueue(p);
						}
						outgoingLinks.add(canonicalizedUrl);
					}
					parentPage.setOut_links(outgoingLinks);
					parentPage.setScore(parentPage.getIn_links().size());
					setOfVisitedUrls.put(parentPage.getDocno(), parentPage);
				//	globalSetOfUrls.put(parentPage.getDocno(), parentPage);
					
					if(setOfVisitedUrls.size() % 1000 == 0)
						saveToES();	
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}			
	}

	public boolean updateRobotValues(String rawUrl) throws Exception{
		URL url = new URL(rawUrl);
		BaseRobotRules brr;
		boolean isAllowed;
		if(mapOfRobotValues.containsKey(url.getHost()))
			return mapOfRobotValues.get(url.getHost());
		else{
			brr = mf.checkForRobotRules(url.getHost());
			if(brr == null)
				isAllowed = true;
			else
				isAllowed = brr.isAllowed(rawUrl);	
			mapOfRobotValues.put(url.getHost(), isAllowed);
			return isAllowed;
		}
	}

	public Document retrieveDocumentFromWebPage(String rawUrl) throws Exception{

		try{
			if(rawUrl.startsWith("https://books.google.com/books"))
				return null;
			//Thread.sleep(4000);
			long diff =0;
			String cUrl = mf.urlCanonicalization(rawUrl);
			if(timingOfLastVisitedRecords.containsKey(cUrl)){
				Long t = timingOfLastVisitedRecords.get(cUrl);
				diff = System.currentTimeMillis() - t; 
				timingOfLastVisitedRecords.put(cUrl, System.currentTimeMillis());
				if(diff < 1000)
					Thread.sleep(1000 - diff); 
			} else{
				timingOfLastVisitedRecords.put(cUrl, System.currentTimeMillis());
			}

			Connection c = Jsoup.connect(rawUrl).timeout(10000);
			Connection.Response r = c.execute();
			if(r.statusCode() != 200)
				return null;
			return c.get();
		} catch(Exception ex){
			System.out.println("Connection failed for url :"+rawUrl);
		}
		return null;
	}

	public void saveToES() throws IOException{
		System.out.println("Before : "+setOfVisitedUrls.size());
		id.saveIndex(setOfVisitedUrls);	
		setOfVisitedUrls.clear();
		countOfDomain.clear();
		mh.rearrangeFrontier(frontier);
		System.out.println("After : "+setOfVisitedUrls.size());

	}

}
