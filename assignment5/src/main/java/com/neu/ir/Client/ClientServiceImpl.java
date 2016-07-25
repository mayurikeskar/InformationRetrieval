package com.neu.ir.Client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

public class ClientServiceImpl implements ClientService{
	private Node node;
	public Client getClient() throws UnknownHostException {
		Settings settings = Settings.settingsBuilder()
				.put("cluster.name", "12woodward").build();
		Client client = TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
		return client;
	}

	public void closeNode() {
		node.close();
	}


}
