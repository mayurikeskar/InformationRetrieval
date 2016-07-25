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
import org.elasticsearch.common.lucene.search.function.ScriptScoreFunction;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.script.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.script.ScriptService.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityBuilder;
import org.tartarus.snowball.ext.EnglishStemmer;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.neu.ir.client.ClientService;
import com.neu.ir.client.ClientServiceImpl;
import com.neu.ir.document.HitsNAuthority;

public class IndexDataImpl implements IndexData{

	Client client = null;
	ObjectMapper jsonMapper = null;
	long avgLen = 0;

	public IndexDataImpl() throws UnknownHostException{
		ClientService clientService = new ClientServiceImpl();
		client = clientService.getClient();
		jsonMapper = new ObjectMapper();
	}

	public ObjectMapper getJsonMapper() {
		return jsonMapper;
	}

	public void setJsonMapper(ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	public void getIndex(String docId){
		GetResponse getResponse = client.prepareGet("newyork","page", docId)
				.execute()
				.actionGet();
		System.out.println(getResponse.getSource());
	}

	public Map<String, Integer> getTermFrequency(String queryString) {
		Map<String, Integer> mapOfTermFrequency = new HashMap<String, Integer>();
		//String stemmedString = stemTerm(queryString);
		SearchResponse response = null;
		
		ImmutableMap<String, String> params = ImmutableMap.of("field", "text", "term", queryString);
		Script getTf = new Script("getTF", ScriptService.ScriptType.INDEXED, "groovy", params);
	//	for(int i =0; i<61613; i=i+10000){
			
			response = client.prepareSearch("newyork").setTypes("page")
					.setQuery(QueryBuilders.functionScoreQuery(QueryBuilders.matchQuery("text",queryString),scriptFunction(getTf)).boostMode("replace"))
					.setFrom(0)
					.setSize(10000)
					.setNoFields()
					.execute()
					.actionGet();
			
			for(SearchHit hit : response.getHits().getHits()){
				mapOfTermFrequency.put(hit.getId(), (int)hit.getScore());
			}
		//}

		return  mapOfTermFrequency;
	}

	public Map<String, Integer> getDocumentLength() throws IOException {
		Map<String, Integer> mapOfDocLength = new HashMap<String, Integer>();
		
		ImmutableMap<String, String> params = ImmutableMap.of("field", "text");
		Script doclen = new Script("doclen", ScriptService.ScriptType.INDEXED, "groovy", params);
		SearchResponse sr = client.prepareSearch("newyork")
				.setTypes("page")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setSize(100000)
				.setQuery(QueryBuilders.functionScoreQuery(QueryBuilders.matchAllQuery(), scriptFunction(doclen)).boostMode("replace"))
				.setFrom(0)
				.setNoFields()
				.execute()
				.actionGet();
		for(SearchHit hit : sr.getHits()){
			avgLen = avgLen + (int)hit.getScore();
			mapOfDocLength.put(hit.getId(), (int)hit.getScore());
		}
		return mapOfDocLength;
	}

	public String stemTerm (String term) {
		EnglishStemmer stemmer = new EnglishStemmer();
		stemmer.setCurrent(term);
		if(stemmer.stem()){
			System.out.println(stemmer.getCurrent());
			return stemmer.getCurrent();
		}
		else
			return term;
	}

}
