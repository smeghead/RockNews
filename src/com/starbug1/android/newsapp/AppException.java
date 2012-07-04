package com.starbug1.android.newsapp;

public class AppException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AppException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AppException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public AppException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public AppException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

}
