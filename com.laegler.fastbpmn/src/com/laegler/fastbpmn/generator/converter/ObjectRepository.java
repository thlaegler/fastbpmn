/**
 * (c) Copyright 2013 by Itemis AG, Hamburg, Germany
 */
package com.laegler.fastbpmn.generator.converter;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.bpmn2.Association;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.CallActivity;
import org.eclipse.bpmn2.CallableElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.InteractionNode;
import org.eclipse.bpmn2.MessageFlow;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.emf.ecore.EObject;

import com.laegler.fastbpmn.fastBpmn.FDiDiagramElement;
import com.laegler.fastbpmn.fastBpmn.FObject;
import com.laegler.fastbpmn.generator.exception.DuplicateIdBpmnException;

/**
 * Wrapper for object repository
 * 
 * @author Thomas Laegler <thomas.laegler@googlemail.com>
 * @version 0.1
 * 
 */
public class ObjectRepository {

	private HashMap<String, FlowObject> flowObjects;
	private HashMap<String, ArrayList<BaseElement>> specialCases;

	/**
	 * 
	 */
	public ObjectRepository() {
		super();
		flowObjects = new HashMap<String, FlowObject>();
		specialCases = new HashMap<String, ArrayList<BaseElement>>();
	}

	/**
	 * @return the flowObject
	 */
	public FlowObject addFlowObject(BaseElement bObject, FObject fObject) {
		return this.addFlowObject(bObject, fObject, null, null);
	}

	/**
	 * @return the flowObject
	 */
	public FlowObject addFlowObject(BaseElement bObject, FObject fObject,
			EObject bDiObject) {
		return this.addFlowObject(bObject, fObject, bDiObject, null);
	}

	/**
	 * @return the flowObject
	 */
	public FlowObject addFlowObject(BaseElement bObject, FObject fObject,
			EObject bDiObject, FDiDiagramElement fDiObject) {
		String key = bObject.getId();
		if (this.flowObjects.containsKey(key)) {
			try {
				throw new DuplicateIdBpmnException(key);
			} catch (DuplicateIdBpmnException e) {
				e.printStackTrace();
			}
		}
		this.flowObjects.put(key, new FlowObject(bObject, fObject, bDiObject,
				fDiObject));
		// Handle special cases
		if (this.specialCases.containsKey(key)) {
			for (BaseElement bBaseElement : this.specialCases.get(key)) {
				if (this.flowObjects.containsKey(key)) {
					// Association
					if (bBaseElement instanceof Association) {
						((Association) this.flowObjects.get(
								bBaseElement.getId()).getbObjectRef())
								.setTargetRef(bObject);
						// SequenceFlow
					} else if (bBaseElement instanceof SequenceFlow) {
						((SequenceFlow) this.flowObjects.get(
								bBaseElement.getId()).getbObjectRef())
								.setTargetRef((FlowNode) bObject);
						((FlowNode) bObject).getIncoming().add(
								(SequenceFlow) this.flowObjects.get(
										bBaseElement.getId()).getbObjectRef());
						// MessageFlow
					} else if (bBaseElement instanceof MessageFlow) {
						((MessageFlow) this.flowObjects.get(
								bBaseElement.getId()).getbObjectRef())
								.setTargetRef((InteractionNode) bObject);
						// CallActivity
					} else if (bBaseElement instanceof CallActivity) {
						((CallActivity) this.flowObjects.get(
								bBaseElement.getId()).getbObjectRef())
								.setCalledElementRef((CallableElement) bObject);
					}
				}
			}
		}
		return this.flowObjects.get(bObject.getId());
	}

	public void addSpecialCase(String key, BaseElement bBaseElement) {
		ArrayList<BaseElement> tempList = null;

		if (this.specialCases.containsKey(key)) {
			tempList = specialCases.get(key);
			if (tempList == null) {
				tempList = new ArrayList<BaseElement>();
			}
			tempList.add(bBaseElement);

		} else {
			tempList = new ArrayList<BaseElement>();
			tempList.add(bBaseElement);
		}

		specialCases.put(key, tempList);
	}

	/**
	 * @return the flowObjects
	 */
	public HashMap<String, FlowObject> getFlowObjects() {
		return flowObjects;
	}

	/**
	 * @return the specialCases
	 */
	public HashMap<String, ArrayList<BaseElement>> getSpecialCases() {
		return specialCases;
	}

}
