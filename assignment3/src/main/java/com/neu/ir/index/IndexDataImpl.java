package com.neu.ir.index;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.neu.ir.Document.Page;
import com.neu.ir.client.ClientService;
import com.neu.ir.client.ClientServiceImpl;
import com.neu.ir.util.Constants;
import com.neu.ir.util.MiscFunctions;

public class IndexDataImpl implements IndexData{

	Client client = null;
	ObjectMapper jsonMapper = null;
	MiscFunctions mf = null;
	long avgLen = 0;
	FileWriter fw;
	BufferedWriter bw;

	Map<String, Page> mapOfRepetitions = new HashMap<String, Page>();
	Set<String> setOfURLs = new HashSet<String>();


	public IndexDataImpl() throws UnknownHostException{
		ClientService clientService = new ClientServiceImpl();
		client = clientService.getClient();
		mf = new MiscFunctions();
		jsonMapper = new ObjectMapper();
	}

	public ObjectMapper getJsonMapper() {
		return jsonMapper;
	}

	public void setJsonMapper(ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	public void saveIndex(Map<String, Page> mapOfVisitedPages) throws IOException {
		BulkRequestBuilder bulkRequestBuilder = client.prepareBulk().setRefresh(true);
		for(Map.Entry<String, Page> entry : mapOfVisitedPages.entrySet()){
			if(getIndex(entry.getKey(), entry.getValue()))
				continue;
			String json = getJsonMapper().writeValueAsString(entry.getValue()); 
			bulkRequestBuilder.add(client.prepareIndex(Constants.INDEX_NAME, Constants.TYPE_NAME, entry.getKey()).setSource(json)); 
		}
		bulkRequestBuilder.execute().actionGet();
		addTheRest();
	}

	public void addTheRest() throws IOException{
		if(mapOfRepetitions.size() > 0){
			BulkRequestBuilder bulkRequestBuilder = client.prepareBulk().setRefresh(true);
			for(Map.Entry<String, Page> entry : mapOfRepetitions.entrySet()){
				String json = getJsonMapper().writeValueAsString(entry.getValue()); 
				bulkRequestBuilder.add(client.prepareIndex(Constants.INDEX_NAME, Constants.TYPE_NAME, entry.getKey()).setSource(json)); 
			}
			bulkRequestBuilder.execute().actionGet();
			mapOfRepetitions.clear();
		}
	}


	public void addIndex(Map<String, Page> mapOfVisitedPages) throws IOException{
		for(Map.Entry<String, Page> entry : mapOfVisitedPages.entrySet()){
			IndexRequest indexRequest = new IndexRequest(Constants.INDEX_NAME,Constants.TYPE_NAME, entry.getKey());
			indexRequest.source(new Gson().toJson(entry.getValue()	));
			IndexResponse response = client.index(indexRequest).actionGet();
			if(response.isCreated())
				System.out.println("done");
		}
		//addTheRest();
	}

	public void getAllRecords() throws IOException {
		fw = new FileWriter("Urls1.txt");
		bw = new BufferedWriter(fw);
		for(int i =61613; i<= 61617; i++){
			Map<String, Page> map = new HashMap<String, Page>();
			Page p;
			SearchResponse res = client.prepareSearch(Constants.INDEX_NAME)
					.setTypes(Constants.TYPE_NAME)
					.setQuery(QueryBuilders.matchAllQuery())
					.setFrom(i) 	
					.setSize(1)
					.execute()
					.actionGet();

			for(SearchHit hit : res.getHits()){
				setOfURLs.add(hit.getId());
				p = new Page();
				Map<String, Object> mp = hit.getSource();
				p.setDocno((String)mp.get("docno"));
				p.setText((String)mp.get("text"));
				p.setTitle((String)mp.get("title"));
				p.setRawUrl((String) mp.get("rawUrl"));
				p.setHtml_Source((String)mp.get("html_Source"));
				//	p.setAuthor((String)mp.get("author"));
				p.setHTTPheader((String)mp.get("HTTPheader"));
				p.setDepth((Integer)mp.get("depth"));
				ArrayList<String> li = (ArrayList)mp.get("in_links");
				Set<String> s = new HashSet<String>(li);
				p.setIn_links(s);

				ArrayList<String> li2 = (ArrayList)mp.get("out_links");
				Set<String> s2 = new HashSet<String>(li2);
				p.setOut_links(s2);

				StringBuilder sb = new StringBuilder();
				String a = ((String)mp.get("author")).toLowerCase();
				if(a.contains("dhruv"))
					sb.append("Dhruv ");

				if(a.contains("mayuri"))
					sb.append("Mayuri ");

				if(a.contains("alekhya"))
					sb.append("Alekhya ");

				p.setAuthor(sb.toString());
				map.put(hit.getId(), p);
			}
			System.out.println(i);
			mergeDocsInNetwork(map);
		}

		for(String s : setOfURLs){
			bw.write(s+"\n");
		}
		bw.close();
	}

	public void mergeDocsInNetwork(Map<String, Page> map) throws IOException{
		BulkRequestBuilder bulkRequestBuilder = client.prepareBulk().setRefresh(true);

		for(Map.Entry<String, Page> entry : map.entrySet()){
			//			if(getIndex(entry.getKey(), entry.getValue()))
			//				continue;
			//			IndexRequest indexRequest = new IndexRequest(Constants.MERGE_INDEX,Constants.TYPE_NAME, entry.getKey());
			//			indexRequest.source(new Gson().toJson(entry.getValue())).id(entry.getKey());
			//			IndexResponse response = client.index(indexRequest).actionGet();
			String json = getJsonMapper().writeValueAsString(entry.getValue()); 
			bulkRequestBuilder.add(client.prepareIndex(Constants.MERGE_INDEX, Constants.TYPE_NAME, entry.getKey()).setSource(json)); 
		}
		bulkRequestBuilder.execute().actionGet();
		//addTheRestForMerge();
	}



	public void addTheRestForMerge() throws IOException{
		if(mapOfRepetitions.size() > 0){
			BulkRequestBuilder bulkRequestBuilder = client.prepareBulk().setRefresh(true);
			for(Map.Entry<String, Page> entry : mapOfRepetitions.entrySet()){
				String json = getJsonMapper().writeValueAsString(entry.getValue()); 
				bulkRequestBuilder.add(client.prepareIndex(Constants.MERGE_INDEX, Constants.TYPE_NAME, entry.getKey()).setSource(json)); 
			}
			bulkRequestBuilder.execute().actionGet();
			mapOfRepetitions.clear();


		}
	}



		public boolean getIndex(String url, Page p){
		Set<String> hs;
		GetResponse response = client.prepareGet(Constants.INDEX_NAME
				,Constants.TYPE_NAME, url)
				.execute()
				.actionGet();

		if(response.isExists()){
			//System.out.println(response.getSource());
			Map<String, Object> mp = response.getSourceAsMap();
			ArrayList inlinks = (ArrayList)mp.get("in_links");
			ArrayList outlinks = (ArrayList) mp.get("out_links");

			inlinks.addAll(p.getIn_links());
			hs = new HashSet<String>(inlinks);
			p.setIn_links(hs);

			outlinks.addAll(p.getOut_links());
			hs = new HashSet<String>(outlinks);
			p.setOut_links(hs);
			//p.setPageId((Integer)mp.get("pageId"));
			p.setDepth((Integer)mp.get("depth"));
			p.setAuthor(mp.get("author")+" "+p.getAuthor());
			mapOfRepetitions.put(url, p);
			return true;
		}
		return false;
	}

	public void getMayuriData() throws IOException{

		for(int i =0; i<= 61613; i=i+100){
			Map<String, Page> map = new HashMap<String, Page>();
			Page p;
			SearchResponse res = client.prepareSearch(Constants.INDEX_NAME)
					.setTypes(Constants.TYPE_NAME)
					.setQuery(QueryBuilders.matchAllQuery())
					.setFrom(i) 	
					.setSize(100)
					.execute()
					.actionGet();

			for(SearchHit hit : res.getHits()){

				Map<String, Object> mp = hit.getSource();
				String aut = (String)mp.get("author");
				if(aut.contains("Mayuri")){
					p = new Page();
					p.setDocno((String)mp.get("docno"));
					p.setText((String)mp.get("text"));
					p.setTitle((String)mp.get("title"));
					p.setRawUrl((String) mp.get("rawUrl"));
					p.setHtml_Source((String)mp.get("html_Source"));
					//	p.setAuthor((String)mp.get("author"));
					p.setHTTPheader((String)mp.get("httpheader"));
					p.setDepth((Integer)mp.get("depth"));
					ArrayList<String> li = (ArrayList)mp.get("in_links");
					Set<String> s = new HashSet<String>(li);
					p.setIn_links(s);

					ArrayList<String> li2 = (ArrayList)mp.get("out_links");
					Set<String> s2 = new HashSet<String>(li2);
					p.setOut_links(s2);
					p.setAuthor("Mayuri");
					map.put(hit.getId(), p);
				}
			}
			System.out.println(i);
			mergeDocsInNetwork(map);
		}
	}


	public void dataForAss4() throws IOException{
		fw = new FileWriter("merged_inlinks_with text.txt");
		bw = new BufferedWriter(fw);
		for(int i =0; i<= 61613; i++){
			SearchResponse res = client.prepareSearch("newyork")
					.setTypes("page")
					.setQuery(QueryBuilders.matchAllQuery())
					.setFrom(i) 	
					.setSize(1)
					.execute()
					.actionGet();

			for(SearchHit hit : res.getHits()){
				Map<String, Object> data = hit.getSource();
				bw.write(hit.getId()+" ");
				String text = (String)data.get("text");
//				ArrayList<String> li = (ArrayList)data.get("in_links");
//				for(String s : li){
//					bw.write(s+" ");
//				}
				bw.write("\n");
				bw.write(text);
				bw.write("\n");			
			}
		}
		bw.close();
	}
}