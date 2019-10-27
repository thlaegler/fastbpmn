/**
 * (c) Copyright 2013 by Itemis AG, Hamburg, Germany
 */
package com.laegler.fastbpmn.generator.converter;

import org.eclipse.bpmn2.DocumentRoot;

import com.laegler.fastbpmn.fastBpmn.FDocumentRoot;

/**
 * Generator supertype
 * 
 * @author Thomas Laegler <thomas.laegler@googlemail.com>
 * @version 0.1
 * 
 */
public class Converter {

	/**
	 * FastBPMN <code>FDocumentRoot</code>
	 */
	protected FDocumentRoot fDocumentRoot;

	/**
	 * BPMN 2.0 <code>DocumentRoot</code>
	 */
	protected DocumentRoot bDocumentRoot;

	/**
	 * Repository to store all model objects
	 */
	protected ObjectRepository objectRepository;

	/**
	 * 
	 */
	public Converter() {
		super();
		this.objectRepository = new ObjectRepository();
	}

	/**
	 * @return the fDocumentRoot
	 */
	public FDocumentRoot getfDocumentRoot() {
		return fDocumentRoot;
	}

	/**
	 * @return the bDocumentRoot
	 */
	public DocumentRoot getbDocumentRoot() {
		return bDocumentRoot;
	}

}
