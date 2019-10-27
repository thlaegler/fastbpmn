/**
 * (c) Copyright 2013 by Itemis AG, Hamburg, Germany
 */
package com.laegler.fastbpmn.generator.exception;

/**
 * BPMN Exception
 * 
 * @author Thomas Laegler <thomas.laegler@googlemail.com>
 * @version 0.1
 * 
 */
public class BpmnException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public BpmnException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public BpmnException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BpmnException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public BpmnException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public BpmnException(Throwable cause) {
		super(cause);
	}

}
