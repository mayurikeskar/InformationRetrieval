package com.neu.ir.index;

import java.io.IOException;
import java.util.Map;

import com.neu.ir.Document.Page;



public interface IndexData {
	
	public void saveIndex(Map<String, Page> mapOfvisitedPages) throws IOException;
	
	//public void addIndex(String fileName) throws IOException;
	
	//public boolean getIndex(String docId, Page page);
	
	public void getAllRecords() throws IOException;
	
	public void getMayuriData() throws IOException;

	public void dataForAss4() throws IOException;
}
