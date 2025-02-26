package com.util.page;

public class Page {

	private long totalCount;
	
	private int limit;
	
	private long startIndex;

	public Page() {
		
	}

	public Page(long totalCount, long startIndex, int limit) {
		this.totalCount = totalCount;
		this.limit = limit;
		this.startIndex = startIndex;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public long getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(long startIndex) {
		this.startIndex = startIndex;
	}
	
	public long getPageCount() {
		long pageCount = this.totalCount / this.limit;
		if(this.totalCount % this.limit > 0) {
			pageCount += 1;
		}
		return pageCount;
	}
	
	public long getCurrentPage() {
		//起始记录数 = 每页显示的条数 * 当前页 - 每页显示的条数
		// 当前页 = （起始记录数 + 每页显示条数）/ 每页显示条数
		return (this.startIndex + this.limit) / this.limit;
	}
}
