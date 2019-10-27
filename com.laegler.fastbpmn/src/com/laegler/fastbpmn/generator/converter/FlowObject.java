/**
 * (c) Copyright 2013 by Itemis AG, Hamburg, Germany
 */
package com.laegler.fastbpmn.generator.converter;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.emf.ecore.EObject;

import com.laegler.fastbpmn.fastBpmn.FDiDiagramElement;
import com.laegler.fastbpmn.fastBpmn.FObject;

/**
 * Repository for saving all model objects for both BPMN 2.0 and FastBPMN
 * 
 * @author Thomas Laegler <thomas.laegler@googlemail.com>
 * @version 0.1
 * 
 */
public class FlowObject {

	/**
	 * Reference to BPMN 2.0 FlowElement
	 */
	private BaseElement bObjectRef;

	/**
	 * Reference to FastBPMN object
	 */
	private FObject fObjectRef;

	/**
	 * Reference to BPMN 2.0 DiagramElement
	 */
	private EObject bDiObjectRef;

	/**
	 * Reference to FastBPMN FDiDiagramElement
	 */
	private FDiDiagramElement fDiObjectRef;

	/**
	 * @param bObjectRef
	 * @param fObjectRef
	 * @param bDiObjectRef
	 * @param fDiObjectRef
	 */
	public FlowObject(BaseElement bObjectRef, FObject fObjectRef,
			EObject bDiObjectRef, FDiDiagramElement fDiObjectRef) {
		super();
		this.bObjectRef = bObjectRef;
		this.fObjectRef = fObjectRef;
		this.bDiObjectRef = bDiObjectRef;
		this.fDiObjectRef = fDiObjectRef;
	}

	/**
	 * @return the bObjectRef
	 */
	public BaseElement getbObjectRef() {
		return bObjectRef;
	}

	/**
	 * @return the fObjectRef
	 */
	public FObject getfObjectRef() {
		return fObjectRef;
	}

	/**
	 * @return the bDiObjectRef
	 */
	public EObject getbDiObjectRef() {
		return bDiObjectRef;
	}

	/**
	 * @return the fDiObjectRef
	 */
	public FDiDiagramElement getfDiObjectRef() {
		return fDiObjectRef;
	}

	/**
	 * @param bObjectRef
	 *            the bObjectRef to set
	 */
	public void setbObjectRef(BaseElement bObjectRef) {
		this.bObjectRef = bObjectRef;
	}

	/**
	 * @param fObjectRef
	 *            the fObjectRef to set
	 */
	public void setfObjectRef(FObject fObjectRef) {
		this.fObjectRef = fObjectRef;
	}

	/**
	 * @param bDiObjectRef
	 *            the bDiObjectRef to set
	 */
	public void setbDiObjectRef(EObject bDiObjectRef) {
		this.bDiObjectRef = bDiObjectRef;
	}

	/**
	 * @param fDiObjectRef
	 *            the fDiObjectRef to set
	 */
	public void setfDiObjectRef(FDiDiagramElement fDiObjectRef) {
		this.fDiObjectRef = fDiObjectRef;
	}

}