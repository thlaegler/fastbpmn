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
public class NoStartEventFoundBpmnException extends BpmnException {

	private static final long serialVersionUID = 1L;

	private static final String DESCRIPTION = "There is no StartEvent!";

	/**
	 * 
	 */
	public NoStartEventFoundBpmnException() {
		super(DESCRIPTION);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public NoStartEventFoundBpmnException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(DESCRIPTION + message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoStartEventFoundBpmnException(String message, Throwable cause) {
		super(DESCRIPTION + message, cause);
	}

	/**
	 * @param message
	 */
	public NoStartEventFoundBpmnException(String message) {
		super(DESCRIPTION + message);
	}

	/**
	 * @param cause
	 */
	public NoStartEventFoundBpmnException(Throwable cause) {
		super(cause);
	}

}
