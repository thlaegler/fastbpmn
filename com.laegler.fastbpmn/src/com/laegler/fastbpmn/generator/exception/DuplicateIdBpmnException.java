/**
 * (c) Copyright 2013 by Itemis AG, Hamburg, Germany
 */
package com.laegler.fastbpmn.generator.exception;

/**
 * NoStartEventFound Exception
 * 
 * @author Thomas Laegler <thomas.laegler@googlemail.com>
 * @version 0.1
 * 
 */
public class DuplicateIdBpmnException extends BpmnException {

	private static final long serialVersionUID = 1L;
	
	private static final String DESCRIPTION = "There are two elements of the same ID, which would come to an invalid BPMN-model. The duplicate ID is: ";

	/**
	 * 
	 */
	public DuplicateIdBpmnException() {
		super(DESCRIPTION + "unknown");
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public DuplicateIdBpmnException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(DESCRIPTION + message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DuplicateIdBpmnException(String message, Throwable cause) {
		super(DESCRIPTION + message, cause);
	}

	/**
	 * @param message
	 */
	public DuplicateIdBpmnException(String message) {
		super(DESCRIPTION + message);
	}

	/**
	 * @param cause
	 */
	public DuplicateIdBpmnException(Throwable cause) {
		super(cause);
	}

}
