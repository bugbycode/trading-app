package com.util.page;

import java.util.List;

public class SearchResult<T> {

	private List<T> list;
	
	private Page page;

	public SearchResult(List<T> list, Page page) {
		this.list = list;
		this.page = page;
	}

	public SearchResult() {
		
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public Page getPage() {
		return this.page;
	}

	public void setPage(Page page) {
		this.page = page;
	}
	
	
}
