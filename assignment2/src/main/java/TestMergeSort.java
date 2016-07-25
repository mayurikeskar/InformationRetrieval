import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.neu.ir.document.Term;

public class TestMergeSort {

	public static void main(String[] args) {

		LinkedHashMap<String, Term> map2 = new LinkedHashMap<String, Term>();
		Term t1 = new Term ();
		t1.setTf(3);
		List li1 = new ArrayList<Integer>();
		li1.add(1);
		li1.add(2);
		li1.add(3);
		t1.setPositions(li1);


		Term t2 = new Term ();
		t2.setTf(3);
		List li2 = new ArrayList<Integer>();
		li2.add(1);
		li2.add(2);
		li2.add(3);
		t2.setPositions(li2);

		Term t3 = new Term ();
		t3.setTf(1);
		List li3 = new ArrayList<Integer>();
		li3.add(1);
		li3.add(2);
		li3.add(3);
		t3.setPositions(li3);
		//map2.put("3", t3);

		LinkedHashMap<String, Term> map1 = new LinkedHashMap<String, Term>();
		Term t4 = new Term ();
		t4.setTf(1);
		List li4 = new ArrayList<Integer>();
		li4.add(1);
		li4.add(2);
		li4.add(3);
		t4.setPositions(li4);


		Term t5 = new Term ();
		t5.setTf(1);
		List li5 = new ArrayList<Integer>();
		li5.add(1);
		li5.add(2);
		li5.add(3);
		t5.setPositions(li5);

		Term t6 = new Term ();
		t6.setTf(1);
		List li6 = new ArrayList<Integer>();
		li6.add(1);
		li6.add(2);
		li6.add(3);
		t6.setPositions(li6);
		
		map2.put("4", t4);
		map2.put("5", t5);
		//map1.put("6", t6);
		
		map1.put("1", t1);
		map1.put("2", t2);
		map1.put("6", t6);

		//mf.sortDocsByValues(mp, mp1);


		LinkedHashMap<String, Term> map3 = new LinkedHashMap<String, Term>();
		Set<Entry<String, Term>> set1 = map1.entrySet();
		Set<Entry<String, Term>> set2 = map2.entrySet();
		int i =0;
		int j =0;
		String s1, s2;
		int num1, num2;
		Term t;

		while(i < map1.size() && j < map2.size()){
			s1 = set1.toArray()[i].toString().split("=")[0];
			s2 = set2.toArray()[j].toString().split("=")[0];
			num1 = map1.get(s1).getTf();
			num2 = map2.get(s2).getTf();
			if(num1 >= num2){
				t = map1.get(s1);
				map3.put(s1, t);
				i++;
			} else{
				t = map2.get(s2);
				map3.put(s2, t);
				j++;
			}
		}
		while(i < map1.size()){
			s1 = set1.toArray()[i].toString().split("=")[0];
			t = map1.get(s1);
			map3.put(s1, t);
			i++;
		}
		while(j < map2.size()){
			s2 = set2.toArray()[j].toString().split("=")[0];
			t = map2.get(s2);
			map3.put(s2, t);
			j++;
		}

	/*	for(Map.Entry<String, Term> e : map3.entrySet()){
			System.out.println(e.getKey()+" -- "+e.getValue().getTf()+"--"+e.getValue().getPositions());
		}
	*/	
	}
}
