package com.neu.ir.Document;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.ir.Client.ClientService;
import com.neu.ir.Client.ClientServiceImpl;

public class IndexRetrieval {
	Client client = null;
	ObjectMapper jsonMapper = null;

	public IndexRetrieval() throws UnknownHostException{
		ClientService clientService = new ClientServiceImpl();
		client = clientService.getClient();
		jsonMapper = new ObjectMapper();

	}

	public List<String> getTopRecords(String query){

		List<String> listOfRecords = new ArrayList<String>();
		QueryBuilder qb = queryStringQuery(query);

		SearchResponse response = client.prepareSearch("chicago").setTypes("page")
				.setSize(1000).setQuery(qb).execute().actionGet();
		
		for(SearchHit hit : response.getHits()){
			listOfRecords.add(hit.getId());
		}
		return listOfRecords;

	}
}
