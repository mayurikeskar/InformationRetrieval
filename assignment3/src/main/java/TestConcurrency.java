/*import java.util.HashSet;
import java.util.Set;

import com.neu.ir.Document.Frontier;
import com.neu.ir.Document.Page;

public class TestConcurrency {
	
	static Frontier frontier = new Frontier();
	public static void main(String[] args) {
		Page p = new Page();
		p.setPageId(0);
		p.setUrl("www.wikipedia.com");
		p.setRawUrl("www.wikipedia.com");
		Set<String> l1 = new HashSet<String>();
		l1.add("abc");
		l1.add("def");
		l1.add("pqm");
		p.setIncomingLinks(l1);
		
		Page p1 = new Page();
		p1.setPageId(1);
		p1.setUrl("www.gmail.com");
		p1.setRawUrl("www.gmail.com");
		Set<String> l4 = new HashSet<String>();
		l4.add("123");
		l4.add("456");
		l4.add("789");
		l4.add("012");
		p1.setIncomingLinks(l4);
		
		Page p2 = new Page();
		p2.setPageId(2);
		p2.setUrl("www.google.com");
		p2.setRawUrl("www.google.com");
		Set<String> l2 = new HashSet<String>();
		l2.add("abc");
		l2.add("def");
		l2.add("pqm");
		l2.add("xyz");
		l2.add("jkl");
		p2.setIncomingLinks(l2);
		
		Page p3 = new Page();
		p3.setPageId(3);
		p3.setUrl("www.facebook.com");
		p3.setRawUrl("www.facebook.com");
		Set<String> l3 = new HashSet<String>();
		l3.add("abc");
		p3.setIncomingLinks(l3);
		
		
		frontier.enqueue(p);
		frontier.enqueue(p1);
		frontier.enqueue(p2);
		frontier.enqueue(p3);
		
		
		while(frontier.hasItems()){
			Page page = frontier.dequeue();
			System.out.println(page.getUrl());
			String baseUri = "www.gmail.com";
			
			Page newP = new Page();
			newP.setPageId(1);
			newP.setUrl("www.gmail.com");
			newP.setRawUrl("www.gmail.com");
			Set<String> newL = new HashSet<String>();
			newL.add("qqq");
			newP.setIncomingLinks(newL);
			Page temp = frontier.contains(baseUri);
			if(temp != null)
				frontier.update(newP, temp);
			
		}
	}
	
	
	public static void rearrange(){
		
		System.out.println("here");
	}

}
*/