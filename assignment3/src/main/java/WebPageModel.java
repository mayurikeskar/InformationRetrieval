import java.util.*;

/**
 * Created by ct37238 on 6/19/16.
 */
public class WebPageModel implements Comparable<WebPageModel> {
    String rawUrl;
    Set<String> in_links;
    Set<String> out_links;
    String text;
    String html_Source;
    String title;
    String docno;
    String HTTPheader;
    String author = "Alekhya";
    int depth;

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getDocno() {
        return docno;
    }

    public void setDocno(String docno) {
        this.docno = docno;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHTTPheader() {
        return HTTPheader;
    }

    public void setHTTPheader(String HTTPheader) {
        this.HTTPheader = HTTPheader;
    }

    
    public String getHtml_Source() {
        return html_Source;
    }

    public void setHtml_Source(String html_Source) {
        this.html_Source = html_Source;
    }

    public WebPageModel(String url) {
        this.rawUrl = url;
        this.docno = removeProtocol(url);
        in_links = new HashSet<String>();
        out_links = new HashSet<String>();
    }

    private String removeProtocol(String url) {

        String urlWithoutProtocol = canonicalizeUrl(url).substring(url.indexOf("://")+3);
        return urlWithoutProtocol;


    }
    
    public String canonicalizeUrl(String url){
    	
    	if(url.contains("#"))
    		url = url.substring(0,url.indexOf("#"));
    	
    	if(url.contains("?"))
    		url = url.substring(0,url.indexOf("?"));
    	return url;
    }
    public String getRawUrl() {
        return rawUrl;
    }

    public void setRawUrl(String url) {
        this.rawUrl = url;
    }

    public Set<String> getIn_links() {
        return in_links;
    }

    public void setIn_links(Set<String> in_links) {
        this.in_links = in_links;
    }

    public Set<String> getOut_links() {
        return out_links;
    }

    public void setOut_links(Set<String> out_links) {
        this.out_links = out_links;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int compareTo(WebPageModel o) {
        return ((WebPageModel)o).getIn_links().size() - this.getIn_links().size();
    }
}
