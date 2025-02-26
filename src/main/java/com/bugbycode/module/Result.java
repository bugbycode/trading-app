package com.bugbycode.module;

public class Result<T, E> {

	private T result;
	
	private E err;
	
	public Result (T result,E err){
		this.result = result;
		this.err = err;
	}

	public T getResult() {
		return result;
	}

	public E getErr() {
		return err;
	}
	
}
