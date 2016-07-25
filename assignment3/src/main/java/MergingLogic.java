
import com.google.gson.Gson;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.lucene.index.TwoPhaseCommitTool.execute;

/**
 * Created by ct37238 on 6/22/16.
 */
public class MergingLogic {
    public static void main(String args[]) throws UnknownHostException {

        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "12woodward").build();
        Client client = TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));

        for (int i = 0; i < 20365; i++) {

            SearchResponse resp = client.prepareSearch("web_crawler")
                    .setQuery(QueryBuilders.matchAllQuery())
                    .setFrom(i)
                    .setSize(1)
                    .execute()
                    .actionGet();


            for (SearchHit hit : resp.getHits()) {
                Map<String, Object> map = hit.getSource();

                String docNo = (String) map.get("docno");

                GetResponse respMerge = client.prepareGet("cityview", "page", docNo.trim())
                        .execute()
                        .actionGet();

                if (respMerge.isExists()) {
                    Map<String, Object> mapMerged = respMerge.getSourceAsMap();

                    ArrayList<String> arrayListInLinks1 = (ArrayList<String>) mapMerged.get("in_links");
                    ArrayList<String> arrayListInLinks2 = (ArrayList<String>) map.get("in_links");

                    arrayListInLinks1.addAll(arrayListInLinks2);

                    Set<String> setInLinks = new HashSet<String>(arrayListInLinks1);

                    ArrayList<String> arrayListOutLinks1 = (ArrayList<String>) mapMerged.get("out_links");
                    ArrayList<String> arrayListOutLinks2 = (ArrayList<String>) map.get("out_links");

                    arrayListOutLinks1.addAll(arrayListOutLinks2);

                    Set<String> setOutLinks = new HashSet<String>(arrayListOutLinks1);

                    WebPageModel webPageModel = new WebPageModel(docNo);

                    webPageModel.setDocno(docNo);
                    webPageModel.setRawUrl(mapMerged.get("rawUrl").toString());
                    webPageModel.setIn_links(setInLinks);
                    webPageModel.setOut_links(setOutLinks);
                    webPageModel.setText(mapMerged.get("text").toString());
                    webPageModel.setTitle(mapMerged.get("title").toString());
                    webPageModel.author = map.get("author")+" "+ mapMerged.get("author").toString();
                    webPageModel.setHtml_Source(mapMerged.get("html_Source").toString());
                    webPageModel.setHTTPheader(mapMerged.get("HTTPheader").toString());
                    webPageModel.setDepth((Integer) mapMerged.get("depth"));


                    IndexRequest indexRequest = new IndexRequest("cityview", "page", webPageModel.getDocno().trim());
                    indexRequest.source(new Gson().toJson(webPageModel));
                    IndexResponse response = client.index(indexRequest).actionGet();
                    if(response.isCreated())
                    	System.out.println("true1");

                } else {
                    WebPageModel webPageModel = new WebPageModel(docNo);
                    webPageModel.setDocno(docNo);
                    webPageModel.setRawUrl(map.get("rawUrl").toString());

                    Set<String> setInLinks = new HashSet<String>((ArrayList<String>) map.get("in_links"));
                    webPageModel.setIn_links(setInLinks);

                    Set<String> setOutLinks = new HashSet<String>((ArrayList<String>) map.get("out_links"));
                    webPageModel.setOut_links(setOutLinks);

                    webPageModel.setText(map.get("text").toString());
                    webPageModel.setTitle(map.get("title").toString());
                    webPageModel.setHtml_Source(map.get("html_Source").toString());
                    webPageModel.setHTTPheader(map.get("HTTPHeader").toString());
                    webPageModel.setDepth((Integer) map.get("depth"));

                    IndexRequest indexRequest = new IndexRequest("cityview", "page", webPageModel.getDocno().trim());
                    indexRequest.source(new Gson().toJson(webPageModel));
                    IndexResponse response = client.index(indexRequest).actionGet();
                    if(response.isCreated())
                    	System.out.println("true2");
                }

            }


        }
    }

}
