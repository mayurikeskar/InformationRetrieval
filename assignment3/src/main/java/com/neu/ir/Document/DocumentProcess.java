package com.neu.ir.Document;

import java.util.List;

public interface DocumentProcess {

	public void retrievePage() throws Exception;

	public void insertSeeds(List<String> seeds);
	
}
