package com.example.task.globalexception;

public class ListEmptyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ListEmptyException() {
		super("The list is empty.");
	}

	public ListEmptyException(String message) {
		super(message);
	}

	public ListEmptyException(String message, Throwable cause) {
		super(message, cause);
	}

	public ListEmptyException(Throwable cause) {
		super(cause);
	}
}
