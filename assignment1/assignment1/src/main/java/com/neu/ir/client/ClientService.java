package com.neu.ir.client;

import java.net.UnknownHostException;

import org.elasticsearch.client.Client;

public interface ClientService {
		
	Client getClient() throws UnknownHostException;
    
    void closeNode();
    

}
