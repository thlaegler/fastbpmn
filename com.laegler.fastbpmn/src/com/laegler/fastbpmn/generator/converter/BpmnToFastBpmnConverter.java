/**
 * (c) Copyright 2013 by Itemis AG, Hamburg, Germany
 */
package com.laegler.fastbpmn.generator.converter;

import java.util.List;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.Artifact;
import org.eclipse.bpmn2.Association;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.CallActivity;
import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Group;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.ItemAwareElement;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.Message;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.TextAnnotation;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.laegler.fastbpmn.fastBpmn.FActivity;
import com.laegler.fastbpmn.fastBpmn.FAttachment;
import com.laegler.fastbpmn.fastBpmn.FCallActivity;
import com.laegler.fastbpmn.fastBpmn.FProcessPackageDecl;
import com.laegler.fastbpmn.fastBpmn.FDiDiagram;
import com.laegler.fastbpmn.fastBpmn.FDiEdge;
import com.laegler.fastbpmn.fastBpmn.FDiPlane;
import com.laegler.fastbpmn.fastBpmn.FDiShape;
import com.laegler.fastbpmn.fastBpmn.FDocumentRoot;
import com.laegler.fastbpmn.fastBpmn.FEvent;
import com.laegler.fastbpmn.fastBpmn.FFlowNode;
import com.laegler.fastbpmn.fastBpmn.FGateway;
import com.laegler.fastbpmn.fastBpmn.FGatewayType;
import com.laegler.fastbpmn.fastBpmn.FLane;
import com.laegler.fastbpmn.fastBpmn.FProcess;
import com.laegler.fastbpmn.fastBpmn.FSequenceFlow;
import com.laegler.fastbpmn.fastBpmn.FSubProcess;
import com.laegler.fastbpmn.fastBpmn.FTask;
import com.laegler.fastbpmn.fastBpmn.FTaskType;
import com.laegler.fastbpmn.fastBpmn.FastBpmnFactory;
import com.laegler.fastbpmn.generator.exception.NoStartEventFoundBpmnException;

/**
 * This class generates FastBPMN from BPMN 2.0. There is also a serialization
 * feature for exporting the FastBPMN object tree to a valid FastBPMN file with
 * the file extension *.fastbpmn.
 * 
 * @author Thomas Laegler <thomas.laegler@googlemail.com>
 * @version 0.1
 * 
 */
public class BpmnToFastBpmnConverter extends Converter {

	public FDocumentRoot _initMapping(DocumentRoot bDocumentRoot) {
		this.bDocumentRoot = bDocumentRoot;

		try {
			this.interpreteBDocumentRoot(bDocumentRoot);
		} catch (NoStartEventFoundBpmnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return this.fDocumentRoot;
	}

	/**
	 * Builds FastBPMN <code>FCallActivity</code> from BPMN 2.0
	 * <code>CallActivity</code>
	 * 
	 * @param bCallActivity
	 */
	private FCallActivity createFCallActivity(CallActivity bCallActivity) {
		FCallActivity fCallActivity = FastBpmnFactory.eINSTANCE
				.createFCallActivity();
		fCallActivity.setName(bCallActivity.getId());
		fCallActivity.setLabel(bCallActivity.getName());
		return fCallActivity;
	}

	/**
	 * Builds FastBPMN <code>FProcessPackageDecl</code> from BPMN 2.0
	 * <code>Definitions</code>
	 * 
	 * @param bDefinitions
	 */
	private FProcessPackageDecl createFProcessPackageDecl(
			Definitions bDefinitions) {
		FProcessPackageDecl FProcessPackageDecl = FastBpmnFactory.eINSTANCE
				.createFProcessPackageDecl();
		FProcessPackageDecl.setName(bDefinitions.getId());
		// FProcessPackageDecl.setLabel(bDefinitions.getName());
		return FProcessPackageDecl;
	}

	// /**
	// * Builds FastBPMN <code>FDiagram</code> from BPMN 2.0
	// * <code>BPMNDiagram</code>
	// *
	// * @param bGateway
	// * @return
	// */
	// private FDiDiagram createFDiDiagram(BPMNDiagram bDiDiagram) {
	// FDiDiagram fDiDiagram = FastBpmnFactory.eINSTANCE.createFDiDiagram();
	// fDiDiagram.setName(bDiDiagram.getId());
	// fDiDiagram.setLabel(bDiDiagram.getName());
	// return fDiDiagram;
	// }
	//
	// private FDiPlane createFDiPlane(BPMNPlane bDiPlane) {
	// FDiPlane fDiPlane = FastBpmnFactory.eINSTANCE.createFDiPlane();
	// fDiPlane.setName(bDiPlane.getId());
	// // fDiPlane.setBpmnElement(this.getFProcess());
	// return fDiPlane;
	// }
	//
	// private FDiShape createFDiShape(Object bDiShape) {
	// // TODO
	// // private FDiShape createFDiShape(BPMNShape bDiShape) {
	// FDiShape fDiShape = FastBpmnFactory.eINSTANCE.createFDiShape();
	// // fDiShape.setName(bDiShape.getId());
	// return fDiShape;
	// }
	//
	// private FDiEdge createFDiEdge(Object bDiEdge) {
	// // private FDiEdge createFDiEdge(BPMNShape bDiEdge) {
	// FDiEdge fDiEdge = FastBpmnFactory.eINSTANCE.createFDiEdge();
	// // fDiEdge.setName(bDiEdge.getId());
	// return fDiEdge;
	// }

	/**
	 * Builds FastBPMN <code>FDocumentRoot</code> from BPMN 2.0
	 * <code>DocumentRoot</code>
	 * 
	 * @param bDocumentRoot
	 * @return
	 */
	private FDocumentRoot createFDocumentRoot(DocumentRoot bDocumentRoot) {
		FDocumentRoot fDocumentRoot = FastBpmnFactory.eINSTANCE
				.createFDocumentRoot();
		return fDocumentRoot;
	}

	/**
	 * Builds FastBPMN <code>FEvent</code> from BPMN 2.0 <code>Event</code>
	 * 
	 * @param bEvent
	 * @return
	 */
	private FEvent createFEvent(Event bEvent) {
		FEvent fEvent = FastBpmnFactory.eINSTANCE.createFEvent();
		fEvent.setName(bEvent.getId());
		fEvent.setLabel(bEvent.getName());
		// TODO: Event genau definieren, auch durch EventDefinition
		// ((ThrowEvent) bEvent).g
		// if(bEvent instanceof ThrowEvent) {
		// if(bEvent instanceof EndEvent) {
		// fEvent.setEventSequenceType(FEventSequenceType.ENDING);
		// } else if (bEvent instanceof IntermediateThrowEvent) {
		// fEvent.setEventType(FEventType.)
		// } else {
		// fEvent.setEventSequenceType(FEventSequenceType.THROWING);
		// }
		// } else if(bEvent instanceof CatchEvent) {
		// fEvent.setEventSequenceType(FEventSequenceType.CATCHING);
		// } else {
		// fEvent.setEventSequenceType(FEventSequenceType.);
		// }
		// fEvent.setEventSequenceType(FEventModifierType.STARTING);
		return fEvent;
	}

	/**
	 * Builds FastBPMN <code>FGateway</code> from BPMN 2.0 <code>Gateway</code>
	 * 
	 * @param bGateway
	 * @return
	 */
	private FGateway createFGateway(Gateway bGateway) {
		FGateway fGateway = FastBpmnFactory.eINSTANCE.createFGateway();
		fGateway.setName(bGateway.getId());
		fGateway.setLabel(bGateway.getName());
		switch (bGateway.eClass().getClassifierID()) {
		case Bpmn2Package.INCLUSIVE_GATEWAY:
			fGateway.setGatewayType(FGatewayType.INCLUSIVE);
			break;
		case Bpmn2Package.EXCLUSIVE_GATEWAY:
			fGateway.setGatewayType(FGatewayType.EXCLUSIVE);
			break;
		case Bpmn2Package.PARALLEL_GATEWAY:
			fGateway.setGatewayType(FGatewayType.PARALLEL);
			break;
		case Bpmn2Package.EVENT_BASED_GATEWAY:
			fGateway.setGatewayType(FGatewayType.EVENTBASED);
			break;
		case Bpmn2Package.COMPLEX_GATEWAY:
			fGateway.setGatewayType(FGatewayType.COMPLEX);
			break;
		default:
			fGateway.setGatewayType(FGatewayType.COMPLEX);
			break;
		}
		return fGateway;
	}

	/**
	 * Builds FastBPMN <code>FLane</code> from BPMN 2.0 <code>Lane</code>
	 * 
	 * @param bLane
	 * @return
	 */
	private FLane createFLane(Lane bLane) {
		FLane fLane = FastBpmnFactory.eINSTANCE.createFLane();
		fLane.setName(bLane.getId());
		fLane.setLabel(bLane.getName());
		return fLane;
	}

	/**
	 * Builds FastBPMN <code>FProcess</code> from BPMN 2.0 <code>Process</code>
	 * 
	 * @param bProcess
	 * @return
	 */
	private FProcess createFProcess(Process bProcess) {
		FProcess fProcess = FastBpmnFactory.eINSTANCE.createFProcess();
		fProcess.setName(bProcess.getId());
		fProcess.setLabel(bProcess.getName());
		return fProcess;
	}

	/**
	 * Builds FastBPMN <code>FSequenceFlow</code> from BPMN 2.0
	 * <code>SequenceFlow</code>
	 * 
	 * @param bSequenceFlow
	 * @return
	 */
	private FSequenceFlow createFSequenceFlow(SequenceFlow bSequenceFlow) {
		FSequenceFlow fSequenceFlow = FastBpmnFactory.eINSTANCE
				.createFSequenceFlow();
		fSequenceFlow.setName(bSequenceFlow.getId());
		fSequenceFlow.setLabel(bSequenceFlow.getName());
		// fSequenceFlow.setSourceRef(value);
		// fSequenceFlow.setTargetRef(bSequenceFlow.);
		return fSequenceFlow;
	}

	// /**
	// * Searches for first appearance of BPMN 2.0 <code>Definitions</code> in
	// * <code>this.bDocumentRoot</code>.
	// *
	// * @return null, if BPMN element not bound
	// */
	// private Definitions getBDefinitions() {
	// return this.bDocumentRoot.getDefinitions();
	// }

	// /**
	// * Searches for first appearance of BPMN 2.0 Diagram
	// * <code>BPMNDiagram</code>
	// *
	// * @return
	// */
	// private BPMNDiagram getBDiagram() {
	// return this.getBDefinitions().getDiagrams().get(0);
	// }

	// /**
	// * Searches for first appearance of BPMN 2.0 Diagram
	// <code>BPMNPlane</code>
	// *
	// * @return
	// */
	// private BPMNPlane getBPlane() {
	// return this.getBDiagram().getPlane();
	// }

	/**
	 * Builds FastBPMN <code>FSubProcess</code> from BPMN 2.0
	 * <code>SubProcess</code>
	 * 
	 * @param bSubProcess
	 */
	private FSubProcess createFSubProcess(SubProcess bSubProcess) {
		FSubProcess fSubProcess = FastBpmnFactory.eINSTANCE.createFSubProcess();
		fSubProcess.setName(bSubProcess.getId());
		fSubProcess.setLabel(bSubProcess.getName());
		return fSubProcess;
	}

	/**
	 * Builds FastBPMN <code>FTask</code> from BPMN 2.0 <code>Task</code>
	 * 
	 * @param bTask
	 */
	private FTask createFTask(Task bTask) {
		FTask fTask = FastBpmnFactory.eINSTANCE.createFTask();
		fTask.setName(bTask.getId());
		fTask.setLabel(bTask.getName());
		// fTask.setTaskType(FTaskType.BLANK);
		return fTask;
	}

	/**
	 * Searches for first appearance of FastBPMN
	 * <code>FProcessPackageDecl</code> in <code>FDocumentRoot</code>.
	 * 
	 * @return null, if BPMN element not bound
	 */
	private FProcessPackageDecl getFProcessPackageDecl() {
		return this.fDocumentRoot.getPackages().get(0);
	}

	/**
	 * Searches for first appearance of FastBPMN <code>FProcess</code> in
	 * <code>FProcessPackageDecl</code>.
	 * 
	 * @return
	 */
	// private FProcess getFProcess() {
	// for (FRootElement fRootElement : this.getFProcessPackageDecl()
	// .getRootElements()) {
	// if (fRootElement instanceof FProcess) {
	// return (FProcess) fRootElement;
	// }
	// }
	// return null;
	// }

	/**
	 * Interprets BPMN 2.0 <code>Activity</code> and directs it to correct
	 * build, interprete or discriminate method(s).
	 * 
	 * @param bActivity
	 */
	private FActivity interpreteBActivity(Activity bActivity) {
		if (bActivity instanceof Task) {
			return this.interpreteBTask((Task) bActivity);
		} else if (bActivity instanceof SubProcess) {
			return this.interpreteBSubProcess((SubProcess) bActivity);
		} else if (bActivity instanceof CallActivity) {
			return this.interpreteBCallActivity((CallActivity) bActivity);
		}
		return null;
	}

	/**
	 * Interprets BPMN 2.0 <code>Artifact</code> and directs it to correct
	 * build, interprete or discriminate method(s).
	 * 
	 * @param bArtifact
	 * @return
	 */
	private FAttachment interpreteBArtifact(Artifact bArtifact) {
		if (bArtifact instanceof TextAnnotation) {
			// return this.interpreteBTextAnnotation((TextAnnotation)
			// bArtifact);
		} else if (bArtifact instanceof Association) {
			// return this.interpreteBAssociation((Association) bArtifact);
		} else if (bArtifact instanceof Group) {
			// return this.interpreteBGroup((Group) bArtifact);
		}
		return null;
	}

	/**
	 * Interprets BPMN 2.0 <code>CallActivity</code> and directs it to correct
	 * build, interprete or discriminate method(s).
	 * 
	 * @param bCallActivity
	 * @return
	 */
	private FCallActivity interpreteBCallActivity(CallActivity bCallActivity) {
		// Semantic
		FCallActivity fCallActivity = this.createFCallActivity(bCallActivity);

		// Add/Set/Return
		return fCallActivity;
	}

	/**
	 * Interprets BPMN 2.0 <code>Definitions</code> and directs it to correct
	 * build, interprete or discriminate method(s).
	 * 
	 * @param bDefinitions
	 * @return
	 * @throws NoStartEventFoundBpmnException
	 */
	private FProcessPackageDecl interpreteBDefinitions(Definitions bDefinitions)
			throws NoStartEventFoundBpmnException {
		// Semantic
		FProcessPackageDecl FProcessPackageDecl = this
				.createFProcessPackageDecl(bDefinitions);

		// Add/Set
		this.fDocumentRoot.getPackages().add(FProcessPackageDecl);

		// Child iteration: RootElements
		this.interpreteBRootElements(bDefinitions.getRootElements());
		// for(EList<RooteElement> rootElements :
		// bDefinitions.getRootElements()) {
		//
		// }

		// Next iteration - RootElements
		// for (RootElement bRootElement : bDefinitions.getRootElements()) {
		// FProcessPackageDecl.getRootElements().add(
		// this.interpreteBRootElement(bRootElement));
		// }

		// Next iteration - Diagram
		// for (BPMNDiagram bDiDiagram : bDefinitions.getDiagrams()) {
		// this.getFProcessPackageDecl().getDiagrams()
		// .add(this.interpreteBDiDiagram(bDiDiagram));
		// }

		// Add/Set/Return
		return FProcessPackageDecl;

		// this.interpreteBRootElements(bDefinitions.getRootElements());

		// this.interpreteBDiagrams(bDefinitions.getDiagrams());

		// PROCESS
		// Process fProcess = this.createFProcess(bDefinitions);
		// // Track back
		// FProcessPackageDecl.getBpmnElements().add(fProcess);
		// // Add/Set
		// bDefinitions.getRootElements().add(fProcess);
		// results.add(fProcess);

		// LANESET
		// LaneSet fLaneSet = this.createFLaneSet(bDefinitions);
		// // Track back
		// FProcessPackageDecl.getBpmnElements().add(fLaneSet);
		// // Add/Set
		// fProcess.getLaneSets().add(fLaneSet);
		// results.add(fLaneSet);

		// IOSPECIFICATION
		// InputOutputSpecification fIoSpecification = this
		// .createFInputOutputSpecification(bDefinitions);
		// // Add/Set
		// bProcess.setIoSpecification(fIoSpecification);
		// results.add(fIoSpecification);

		// Next Iteration
		// FlowNode fPreviousFlowNode = null;
		// FlowNode fTargetFlowNode = null;
		// for (RootElement bRootElement : bDefinitions.getRootElements()) {
		// // TODO: use interpreteBRootElement() and interpreteBFlowNode()
		// switch (bRootElement.eClass().getClassifierID()) {
		// case Bpmn2Package.PROCESS:
		// results = this.interpreteBProcess((Process) bRootElement);
		// break;
		// case BpmnDiPackage.BPMN_DIAGRAM:
		// results = this.interpreteBProcess((Process) bRootElement);
		// break;
		// }
		// }
		// return results;
	}

	private void interpreteBRootElements(List<RootElement> bRootElements) {
		for (RootElement bRootElement : bRootElements) {
			this.interpreteBRootElement(bRootElement);
		}
	}

	private void interpreteBRootElement(RootElement bRootElement) {
		if (bRootElement instanceof Process) {

		} else if (bRootElement instanceof Collaboration) {
			try {
				this.interpreteBProcess((Process) bRootElement);
			} catch (NoStartEventFoundBpmnException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (bRootElement instanceof Message) {
			this.interpreteBMessage((Message) bRootElement);
		} else if (bRootElement instanceof ItemDefinition) {
			this.interpreteBItemDefinition((ItemDefinition) bRootElement);
		}
	}

	private void interpreteBItemDefinition(ItemDefinition bRootElement) {
		// TODO Auto-generated method stub
		
	}

	private void interpreteBMessage(Message bRootElement) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Interprets FastBPMN <code>FDocumentRoot</code> and directs it to correct
	 * build, interprete or discriminate method(s).
	 * 
	 * @param bDocumentRoot
	 * @return
	 * @throws NoStartEventFoundBpmnException
	 */
	private FDocumentRoot interpreteBDocumentRoot(DocumentRoot bDocumentRoot)
			throws NoStartEventFoundBpmnException {
		// Semantic
		this.fDocumentRoot = this.createFDocumentRoot(bDocumentRoot);
		// Next Iteration
		this.interpreteBDefinitions(bDocumentRoot.getDefinitions());
		// Add/Set/Return
		return this.fDocumentRoot;
	}

	/**
	 * Interprets BPMN 2.0 <code>Event</code> and directs it to correct build,
	 * interprete or discriminate method(s).
	 * 
	 * @param Event
	 * @return
	 */
	private FEvent interpreteBEvent(Event bEvent) {
		// Semantic
		FEvent fEvent = this.createFEvent(bEvent);

		// Add/Set/Return
		return fEvent;
		// EList<EObject> result = new BasicEList<EObject>();
		// // Semantic
		// FEvent fEvent = this.createFEvent(bEvent);
		// // Track back
		// fEvent.getBpmnElements().add(fEvent);
		// // Add / Set
		// // this.getBProcess().getFlowElements().add(fEvent);
		// // Next iteration
		// // for (Attachment bAttachment : bEvent.getAttachments()) {
		// // this.interpreteBAttachment(bAttachment);
		// // }
		// result.add(fEvent);
		// return result;
	}

	private EList<FFlowNode> interpreteBFlowElement(FlowNode bCurrentNode) {
		// Build FFlowNode
		EList<FFlowNode> fFlowNodes = new BasicEList<FFlowNode>();
		FFlowNode fFlowNode = interpreteBFlowNode(bCurrentNode);
		fFlowNodes.add(fFlowNode);

		// following nodes
		EList<FlowNode> bFlowNodes = new BasicEList<FlowNode>();
		for (SequenceFlow bSequenceFlow : bCurrentNode.getOutgoing()) {
			bFlowNodes.add(bSequenceFlow.getTargetRef());
		}
		if (bFlowNodes.size() == 1) {
			fFlowNodes.addAll(interpreteBFlowElement(bFlowNodes.get(0)));
		}
		// TODO: in interpreteBGateway() muss Aufruf von
		// interpreteBFlowElement() erfolgen

		return fFlowNodes;
	}

	/**
	 * Interprets FastBPMN <code>FFlowNode</code> and directs it to correct
	 * build, interprete or discriminate method(s).
	 * 
	 * @param bFlowNode
	 * @return
	 */
	private FFlowNode interpreteBFlowNode(FlowNode bFlowNode) {
		if (bFlowNode instanceof Activity) {
			return this.interpreteBActivity((Activity) bFlowNode);
		} else if (bFlowNode instanceof Event) {
			return this.interpreteBEvent((Event) bFlowNode);
		} else if (bFlowNode instanceof Gateway) {
			// return this.interpreteBGateway((Gateway) bFlowNode);
		}
		return null;
	}

	/**
	 * Interprets BPMN 2.0 <code>Gateway</code> and directs it to correct build,
	 * interprete or discriminate method(s).
	 * 
	 * @param bGateway
	 * @return
	 */
	private FGateway interpreteBGateway(Gateway bGateway) {
		FGateway fGateway = this.createFGateway(bGateway);
		// this.objectRepository.addFlowObjectNode(bGateway.getId(), bGateway,
		// fGateway);
		return fGateway;
	}

	/**
	 * Interprets BPMN 2.0 <code>InputOutputSpecification</code> and directs it
	 * to correct build, interprete or discriminate method(s).
	 * 
	 * @param ioSpecification
	 * @return
	 */
	private Object interpreteBIoSpecification(
			InputOutputSpecification ioSpecification) {
		return null;
	}

	/**
	 * Interprets BPMN 2.0 <code>ItemAwareElement</code> and directs it to
	 * correct build, interprete or discriminate method(s).
	 * 
	 * @param bItemAwareElement
	 * @return
	 */
	private EList<EObject> interpreteBItemAwareElement(
			ItemAwareElement bItemAwareElement) {
		return null;
	}

	/**
	 * Interprets BPMN 2.0 <code>Lane</code> and directs it to correct build,
	 * interprete or discriminate method(s).
	 * 
	 * @param bLane
	 * @return
	 */
	private FLane interpreteBLane(Lane bLane) {
		FLane fLane = this.createFLane(bLane);
		// this.objectRepository.addFlowObjectNode(bLane.getId(), bLane, fLane);
		return fLane;
	}

	/**
	 * Interprets BPMN 2.0 <code>LaneSet</code> and directs it to correct build,
	 * interprete or discriminate method(s).
	 * 
	 * @param bLaneSet
	 * @return
	 */
	private EList<FLane> interpreteBLaneSet(LaneSet bLaneSet) {
		EList<FLane> fLanes = new BasicEList<FLane>();
		// for (Lane bLane : bLaneSet.getLanes()) {
		// fLanes.add(this.interpreteBLane(bLane));
		// }
		return fLanes;
	}

	/**
	 * Interprets BPMN 2.0 <code>Process</code> and directs it to correct build,
	 * interprete or discriminate method(s).
	 * 
	 * @param bProcess
	 * @return
	 * @throws NoStartEventFoundBpmnException
	 */
	private FProcess interpreteBProcess(Process bProcess)
			throws NoStartEventFoundBpmnException {
		// Build Process
		FProcess fProcess = createFProcess(bProcess);

		// Find StartEvent
		StartEvent bStartEvent = null;
		for (FlowElement bFlowElement : bProcess.getFlowElements()) {
			// bFlowElementsMap.put(bFlowElement.getId(), bFlowElement);
			if (bFlowElement instanceof StartEvent) {
				bStartEvent = (StartEvent) bFlowElement;
				break;
			}
		}
		if (bStartEvent == null) {
			throw new NoStartEventFoundBpmnException();
		}

		// Interprete FlowElements !RECURSIVE! starting with StartEvent
		fProcess.getFlowNodes()
				.addAll(this.interpreteBFlowElement(bStartEvent));

		// Interprete LaneSets
		// for (LaneSet bLaneSet : bProcess.getLaneSets()) {
		// this.getFProcessPackageDecl().getRootElements()
		// .addAll(this.interpreteBLaneSet(bLaneSet));
		// }

		// Diagram
		// FDiShape fDiShape = this.createFDiShape(bProcess);
		// fDiShape.setBounds(this.createBDiBounds(bProcess));

		// Add/Set/Return
		this.objectRepository.addFlowObject(bProcess, fProcess, null);
		return fProcess;
	}

	/**
	 * Interprets BPMN 2.0 <code>RootElement</code> and directs it to correct
	 * build, interprete or discriminate method(s).
	 * 
	 * @param bRootElement
	 * @return
	 * @throws NoStartEventFoundBpmnException
	 */
	// private FRootElement interpreteBRootElement(RootElement bRootElement)
	// throws NoStartEventFoundBpmnException {
	// if (bRootElement instanceof Process) {
	// return interpreteBProcess((Process) bRootElement);
	// }
	// return null;
	// }

	/**
	 * Interprets BPMN 2.0 <code>SequenceFlow</code> and directs it to correct
	 * build, interprete or discriminate method(s).
	 * 
	 * @param bSequenceFlow
	 * @return
	 */
	private FSequenceFlow interpreteBSequenceFlow(SequenceFlow bSequenceFlow) {
		// Semantic
		FSequenceFlow fSequenceFlow = this.createFSequenceFlow(bSequenceFlow);

		// Add/Set/Return
		return fSequenceFlow;
	}

	/**
	 * Interprets BPMN 2.0 <code>SubProcess</code> and directs it to correct
	 * build, interprete or discriminate method(s).
	 * 
	 * @param bSubProcess
	 * @return
	 */
	private FSubProcess interpreteBSubProcess(SubProcess bSubProcess) {
		// Semantic
		FSubProcess fSubProcess = this.createFSubProcess(bSubProcess);

		// Add/Set/Return
		return fSubProcess;
	}

	/**
	 * Interprets BPMN 2.0 <code>Task</code> and directs it to correct build,
	 * interprete or discriminate method(s).
	 * 
	 * @param bTask
	 * @return
	 */
	private FTask interpreteBTask(Task bTask) {
		// Semantic
		FTask fTask = this.createFTask(bTask);

		// Add/Set/Return
		return fTask;
	}

}
