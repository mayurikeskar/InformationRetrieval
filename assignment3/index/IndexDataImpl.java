package com.neu.ir.index;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.script.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.neu.ir.client.ClientService;
import com.neu.ir.client.ClientServiceImpl;
import com.neu.ir.document.Document;
import com.neu.ir.util.Constants;
import com.neu.ir.util.MiscFunctions;

public class IndexDataImpl implements IndexData{

	Client client = null;
	ObjectMapper jsonMapper = null;
	MiscFunctions mf = null;
	long avgLen = 0;

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

	public void saveIndex(String fileName) throws IOException {
		List<Document> listOfDocuments = mf.prepareDocument(fileName);
		BulkRequestBuilder bulkRequestBuilder = client.prepareBulk().setRefresh(true);
		for(Document doc : listOfDocuments){
			String json = getJsonMapper().writeValueAsString(doc); 
			bulkRequestBuilder.add(client.prepareIndex(Constants.INDEX_NAME, Constants.TYPE_NAME, doc.getDocId()).setSource(json)); 
		}
		bulkRequestBuilder.execute().actionGet();
	}


	public void addIndex(String fileName) throws IOException{
		List<Document> listOfDocuments = mf.prepareDocument(fileName);
		for(Document doc : listOfDocuments){
			IndexResponse irb = client.prepareIndex(Constants.INDEX_NAME, Constants.TYPE_NAME)
					.setSource(Constants.DOCNO, doc.getDocId()).setSource(Constants.TEXT, doc.getText()).setId(doc.getDocId()).execute().actionGet();
			if(irb.isCreated())
				System.out.println("done");
		}
	}

	public void getIndex(String docId){
		GetResponse getResponse = client.prepareGet(Constants.INDEX_NAME,Constants.TYPE_NAME, docId)
				.execute()
				.actionGet();
		System.out.println(getResponse.getSource());
	}

	public Map<String, Integer> getTermFrequency(String queryString) {
		boolean flag = false;
		Map<String, Integer> mapOfTermFrequency = new HashMap<String, Integer>();
		String stemmedString = mf.stemTerm(queryString);
		QueryBuilder qb1 = QueryBuilders.matchQuery(Constants.TEXT,queryString);
		SearchResponse response = null;

		ImmutableMap<String, String> params = ImmutableMap.of("field", Constants.TEXT, "term", stemmedString);
		response = client.prepareSearch(Constants.INDEX_NAME).setTypes(Constants.TYPE_NAME)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setSize(10000)
				.setQuery(QueryBuilders.functionScoreQuery
						(QueryBuilders.termQuery(Constants.TEXT, stemmedString), 
								new ScriptScoreFunctionBuilder(new Script("tf", 
										ScriptType.FILE, "groovy", params)))
						.boostMode("replace"))
				.setExplain(true)
				.setFrom(0)
				.execute()
				.actionGet();

		//}

		//	else{

		/*	String stemmedString = mf.stemTerm(queryString);
			if(stemmedString.equals("government"))
				stemmedString = "govern";

			ImmutableMap<String, String> params = ImmutableMap.of("field", "text", "term", stemmedString);
			QueryBuilder qb2 = QueryBuilders.matchQuery("text",stemmedString);
			response = client.prepareSearch(Constants.INDEX_NAME).setTypes(Constants.TYPE_NAME)
					.setQuery(qb2)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setSize(100000)
					.setFrom(0)
					.addScriptField("term frequency", new Script("tf", ScriptType.FILE, "groovy", params))
					.execute()
					.actionGet();*/			
		//}

		/*final Map<String, Object> params = new HashMap<String, Object>();
		params.put("term", queryString);
		params.put("field", "text");

		SearchResponse response = client.prepareSearch("ap_dataset")
				.setTypes("document")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.functionScoreQuery
						(QueryBuilders.termQuery("text", queryString), 
								new ScriptScoreFunctionBuilder(new Script("getTF", 
										ScriptType.INDEXED, "groovy", params)))
						.boostMode("replace"))
				.setFrom(0)
				.setSize(1000)
				.execute()
				.actionGet();

		if(response.getContext().isEmpty()){
			String stemmedString = mf.stemTerm(queryString);
			params.put("term", stemmedString);
			response = client.prepareSearch("ap_dataset")
					.setTypes("document")
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(QueryBuilders.functionScoreQuery
							(QueryBuilders.termQuery("text", stemmedString), 
									new ScriptScoreFunctionBuilder(new Script("getTF", 
											ScriptType.INDEXED, "groovy", params)))
							.boostMode("replace"))
					.setFrom(0)
					.setSize(1000)
					.execute()
					.actionGet();
		}
		 */			
	/*	for(SearchHit hit : response.getHits()){
			hit.getInnerHits();
			Explanation[] e = hit.getExplanation().getDetails();
			Explanation e1 = e[0];
			Explanation e2 = e1.getDetails()[0];
			Explanation e3 = e2.getDetails()[0];
			//System.out.println(e3.getDetails()[0].getValue());
			mapOfTermFrequency.put(hit.getId(), (int)e3.getDetails()[0].getValue());
			System.out.println(queryString+" -- "+hit.getId() + "--"+ (int)e3.getDetails()[0].getValue());
		}
	*/	
		for(SearchHit hit : response.getHits().getHits()){
			mapOfTermFrequency.put(hit.getId(), (int)hit.getScore());
			//System.out.println(queryString +"---"+hit.getId()+"---"+(int)hit.getScore());
		
		}
		
		//System.out.println();
		return  mapOfTermFrequency;
	}

	public Map<String, Integer> getDocumentLength() throws IOException {
		Map<String, Integer> mapOfDocLength = new HashMap<String, Integer>();
		QueryBuilder qb = QueryBuilders.matchAllQuery();
		ImmutableMap<String, String> params = ImmutableMap.of("field", Constants.TEXT);
		
		SearchResponse sr = client.prepareSearch(Constants.INDEX_NAME)
				.setTypes(Constants.TYPE_NAME)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setSize(100000)
				.setQuery(QueryBuilders.functionScoreQuery(QueryBuilders.matchAllQuery(), 
						new ScriptScoreFunctionBuilder(new Script("doclen", ScriptType.FILE, "groovy", params)))
						.boostMode("replace"))
				.setFrom(0)
				.execute()
				.actionGet();
		for(SearchHit hit : sr.getHits()){
			avgLen = avgLen + (int)hit.getScore();
			mapOfDocLength.put(hit.getId(), (int)hit.getScore());
		}
		return mapOfDocLength;
	}

	public int getVocabulary(){
		
		CardinalityBuilder aggr = AggregationBuilders.cardinality("unique").field("text");
				
		SearchResponse response = client.prepareSearch(Constants.INDEX_NAME).setTypes(Constants.TYPE_NAME)
				.setQuery(QueryBuilders.matchAllQuery())
				.addAggregation(aggr).execute().actionGet();
		
		
		response.getAggregations().get("unique");
		
		return 0;
		
		
		
	}
}
