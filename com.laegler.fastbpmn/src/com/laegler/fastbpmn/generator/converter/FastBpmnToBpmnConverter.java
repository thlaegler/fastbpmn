/**
 * (c) Copyright 2013 by Itemis AG, Hamburg, Germany
 */
package com.laegler.fastbpmn.generator.converter;

import java.util.List;
import java.util.Random;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.Artifact;
import org.eclipse.bpmn2.Association;
import org.eclipse.bpmn2.AssociationDirection;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.CallActivity;
import org.eclipse.bpmn2.CallableElement;
import org.eclipse.bpmn2.CatchEvent;
import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.InteractionNode;
import org.eclipse.bpmn2.ItemAwareElement;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.Message;
import org.eclipse.bpmn2.MessageFlow;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ProcessType;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.TextAnnotation;
import org.eclipse.bpmn2.ThrowEvent;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.bpmn2.di.BpmnDiFactory;
import org.eclipse.dd.dc.Bounds;
import org.eclipse.dd.dc.DcFactory;
import org.eclipse.dd.dc.Point;
import org.eclipse.dd.di.DiagramElement;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import com.laegler.fastbpmn.fastBpmn.FActivity;
import com.laegler.fastbpmn.fastBpmn.FAnnotation;
import com.laegler.fastbpmn.fastBpmn.FAttachment;
import com.laegler.fastbpmn.fastBpmn.FCallActivity;
import com.laegler.fastbpmn.fastBpmn.FDataIO;
import com.laegler.fastbpmn.fastBpmn.FDataIOType;
import com.laegler.fastbpmn.fastBpmn.FDocumentRoot;
import com.laegler.fastbpmn.fastBpmn.FDocumentation;
import com.laegler.fastbpmn.fastBpmn.FEvent;
import com.laegler.fastbpmn.fastBpmn.FFlow;
import com.laegler.fastbpmn.fastBpmn.FFlowNode;
import com.laegler.fastbpmn.fastBpmn.FGateway;
import com.laegler.fastbpmn.fastBpmn.FGatewayConverging;
import com.laegler.fastbpmn.fastBpmn.FGatewayDiverging;
import com.laegler.fastbpmn.fastBpmn.FLane;
import com.laegler.fastbpmn.fastBpmn.FOption;
import com.laegler.fastbpmn.fastBpmn.FOptionFlowNode;
import com.laegler.fastbpmn.fastBpmn.FPool;
import com.laegler.fastbpmn.fastBpmn.FProcess;
import com.laegler.fastbpmn.fastBpmn.FProcessPackageDecl;
import com.laegler.fastbpmn.fastBpmn.FSendData;
import com.laegler.fastbpmn.fastBpmn.FSendMessage;
import com.laegler.fastbpmn.fastBpmn.FSubProcess;
import com.laegler.fastbpmn.fastBpmn.FTask;
import com.laegler.fastbpmn.generator.exception.NoShapeFoundBpmnException;

/**
 * This class generates BPMN 2.0 from FastBPMN. There is also a serialization
 * feature for exporting the BPMN 2.0 object tree to a valid xml-based BPMN 2.0
 * file with the file extension *.bpmn.
 * 
 * @author Thomas Laegler <thomas.laegler@googlemail.com>
 * @version 0.1
 * 
 */
public class FastBpmnToBpmnConverter extends Converter {

	private final int GRID_X = 150;
	private final int GRID_Y = 100;
	private final int BORDER_X = 50;
	private final int BORDER_Y = 50;

	private EList<FFlowNode> _extractFFlowsNodeFromFOptionFlowNodes(
			EList<FOptionFlowNode> fOptionFlowNodes) {
		EList<FFlowNode> fFlowNodes = new BasicEList<FFlowNode>();
		for (FOptionFlowNode fOptionFlowNode : fOptionFlowNodes) {
			if (fOptionFlowNode instanceof FActivity) {
				fFlowNodes.add((FActivity) fOptionFlowNode);
			} else if (fOptionFlowNode instanceof FEvent) {
				fFlowNodes.add((FEvent) fOptionFlowNode);
			} else if (fOptionFlowNode instanceof FGatewayDiverging) {
				fFlowNodes.add((FGatewayDiverging) fOptionFlowNode);
			}
		}
		return fFlowNodes;
	}

	private FlowNode _extractFirstFlowNode(EList<BaseElement> bSubFlowElements) {
		for (int i = 0; i <= bSubFlowElements.size() - 1; i++) {
			if (bSubFlowElements.get(i) instanceof FlowNode) {
				return (FlowNode) bSubFlowElements.get(i);
			}
		}
		return null;
	}

	private String _extractLabel(String name, String label) {
		if (label == null) {
			return name;
		}
		return label;
	}

	private FlowNode _extractLastFlowNode(EList<BaseElement> bSubFlowElements) {
		for (int i = bSubFlowElements.size() - 1; i >= 0; i--) {
			if (bSubFlowElements.get(i) instanceof FlowNode) {
				return (FlowNode) bSubFlowElements.get(i);
			}
		}
		return null;
	}

	private String _extractName(String name, String label) {
		if (name == null) {
			if (label == null) {
				return this._generateRandomId();
			}
			// label = label.replaceAll("[^A-Za-z0-9]", "");
			label = label.replaceAll("\\W", "");
			if (label.length() < 12) {
				return label;
			}
			return label.substring(0, 12);
		}
		return name.replaceAll("\\W", "");
	}

	private String _generateRandomId() {
		Random rng = new Random();
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int length = 12;
		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(rng.nextInt(characters.length()));
		}
		return new String(text);
	}

	public DocumentRoot _initMapping(FDocumentRoot fDocumentRoot) {
		this.fDocumentRoot = fDocumentRoot;

		this.interpreteFDocumentRoot(fDocumentRoot);
		this._layout();

		return this.bDocumentRoot;
	}

	private void _layout() {
		// Arrange Nodes
		for (FProcessPackageDecl fPackage : this.fDocumentRoot.getPackages()) {
			// LayoutQuadrant q = new LayoutQuadrant(0, 0, 0, 0);
			// this._layoutProcess();
			for (FProcess fProcess : fPackage.getProcesses()) {
				LayoutQuadrant q = new LayoutQuadrant(0, 0, 0, 0);
				if (!fProcess.getPools().isEmpty()) {
					this._layoutPools(fProcess.getPools(), q);
				} else if (!fProcess.getFlowNodes().isEmpty()) {
					this._layoutFlowNodes(fProcess.getFlowNodes(), q);
				}

			}
		}

		// Arrange Edges
		for (String key : this.objectRepository.getFlowObjects().keySet()) {
			FlowObject fo = this.objectRepository.getFlowObjects().get(key);
			if (fo.getbDiObjectRef() instanceof DiagramElement) {
				this._layoutEdges(fo);
			}
		}
	}

	private LayoutQuadrant _layoutActivity(FFlowNode fFlowNode,
			LayoutQuadrant qIn) {
		// Clone
		LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
				qIn.getStartY(), qIn.getEndX(), qIn.getEndY());

		// Create Activity
		this._layoutBounds(fFlowNode.getName(), this.GRID_X * qOut.getStartX(),
				this.GRID_Y * qOut.getStartY());

		// After
		qOut.setEndX(qOut.getStartX() + 1);
		qOut.setEndY(qOut.getStartY() + 1);

		return qOut;
	}

	private void _layoutBounds(String name, int x, int y) {
		Bounds bBounds = ((BPMNShape) this.objectRepository.getFlowObjects()
				.get(name).getbDiObjectRef()).getBounds();
		bBounds.setX(this.BORDER_X + x);
		bBounds.setY(this.BORDER_Y + y);
		((BPMNShape) this.objectRepository.getFlowObjects().get(name)
				.getbDiObjectRef()).setBounds(bBounds);
	}

	// private LayoutQuadrant _layoutGateway(FGatewayDiverging gateway,
	// LayoutQuadrant qIn) {
	// // Clone
	// LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
	// qIn.getStartY(), qIn.getEndX(), qIn.getEndY());
	// // Before child recursion
	// qOut.setStartX(qOut.getStartX() + 1);
	// // Child iteration
	// qOut = this._layoutOptions(gateway.getOptions(), qOut);
	// // After Child iteration
	// qOut.setStartY(qOut.getStartY() - 1);
	// qOut.setEndX(qOut.getEndX() + 1);
	//
	// // Converging Gateway
	// String gatewayConvergingName = ((FGatewayDiverging) gateway)
	// .getGatewayConverging().getName();
	// if (gatewayConvergingName == null) {
	// gatewayConvergingName = gateway.getName() + "_Converging";
	// }
	// Bounds bGatewayConvergingBounds = ((BPMNShape) this.objectRepository
	// .getFlowObjects().get(gatewayConvergingName).getbDiObjectRef())
	// .getBounds();
	// bGatewayConvergingBounds.setX(this.BORDER_X
	// + (this.GRID_X * (qOut.getEndX())));
	// bGatewayConvergingBounds.setY(this.BORDER_Y
	// + (this.GRID_Y * (qIn.getStartY())));
	// ((BPMNShape) this.objectRepository.getFlowObjects()
	// .get(gatewayConvergingName).getbDiObjectRef())
	// .setBounds(bGatewayConvergingBounds);
	// qOut.setStartX(qOut.getStartX() + 1);
	// qOut.setEndX(qOut.getEndX() + 1);
	// return qOut;
	// }

	private void _layoutBounds(String name, int x, int y, int width, int height) {
		if (this.objectRepository.getFlowObjects().containsKey(name)) {
			Bounds bBounds = ((BPMNShape) this.objectRepository
					.getFlowObjects().get(name).getbDiObjectRef()).getBounds();
			bBounds.setX(this.BORDER_X + x);
			bBounds.setY(this.BORDER_Y + y);
			bBounds.setWidth(width);
			bBounds.setHeight(height);
			((BPMNShape) this.objectRepository.getFlowObjects().get(name)
					.getbDiObjectRef()).setBounds(bBounds);
		} else {
			try {
				throw new NoShapeFoundBpmnException(name);
			} catch (NoShapeFoundBpmnException e) {
				e.printStackTrace();
			}
		}
	}

	private void _layoutEdges(FlowObject fo) {
		if ((fo.getbObjectRef() instanceof SequenceFlow)
				|| (fo.getbObjectRef() instanceof Association)
				|| (fo.getbObjectRef() instanceof MessageFlow)) {
			String sourceFlowNodeId = null;
			String targetFlowNodeId = null;
			FlowObject edgeObject = null;
			// Arrange Diagram Edge
			if (fo.getbObjectRef() instanceof SequenceFlow) {
				SequenceFlow bSequenceFlow = (SequenceFlow) fo.getbObjectRef();
				sourceFlowNodeId = ((SequenceFlow) fo.getbObjectRef())
						.getSourceRef().getId();
				targetFlowNodeId = ((SequenceFlow) fo.getbObjectRef())
						.getTargetRef().getId();
				edgeObject = this.objectRepository.getFlowObjects().get(
						bSequenceFlow.getId());
			} else if (fo.getbObjectRef() instanceof Association) {
				Association bAssociation = (Association) fo.getbObjectRef();
				sourceFlowNodeId = ((Association) fo.getbObjectRef())
						.getSourceRef().getId();
				targetFlowNodeId = ((Association) fo.getbObjectRef())
						.getTargetRef().getId();
				edgeObject = this.objectRepository.getFlowObjects().get(
						bAssociation.getId());
			} else if (fo.getbObjectRef() instanceof MessageFlow) {
				MessageFlow bMessageFlow = (MessageFlow) fo.getbObjectRef();
				sourceFlowNodeId = ((BaseElement) bMessageFlow.getSourceRef())
						.getId();
				targetFlowNodeId = ((BaseElement) bMessageFlow.getTargetRef())
						.getId();
				edgeObject = this.objectRepository.getFlowObjects().get(
						bMessageFlow.getId());
			}
			// Source
			BPMNShape sourceDiagramElement = (BPMNShape) this.objectRepository
					.getFlowObjects().get(sourceFlowNodeId).getbDiObjectRef();
			((BPMNEdge) fo.getbDiObjectRef())
					.setSourceElement(sourceDiagramElement);
			// Target
			BPMNShape targetDiagramElement = (BPMNShape) this.objectRepository
					.getFlowObjects().get(targetFlowNodeId).getbDiObjectRef();
			((BPMNEdge) fo.getbDiObjectRef())
					.setTargetElement(targetDiagramElement);

			// Arrange Diagram Waypoints of Edge
			int firstX = ((int) sourceDiagramElement.getBounds().getX() + 25);
			int firstY = ((int) sourceDiagramElement.getBounds().getY() + 25);
			int lastX = ((int) targetDiagramElement.getBounds().getX());
			int lastY = ((int) targetDiagramElement.getBounds().getY() + 25);
			Point firstPoint = this.createBDiPoint(firstX, firstY);
			Point lastPoint = this.createBDiPoint(lastX, lastY);

			List<Point> waypoints = ((BPMNEdge) edgeObject.getbDiObjectRef())
					.getWaypoint();

			if (firstX != lastX && firstY != lastY
					&& !(fo.getbObjectRef() instanceof MessageFlow)) {
				Point middlePoint;
				if (firstY > lastY) {
					middlePoint = this.createBDiPoint(lastX + 25, firstY);
					firstPoint.setX(firstPoint.getX() + 25);
					lastPoint.setX(lastPoint.getX() + 25);
					lastPoint.setY(lastPoint.getY() + 25);
					// lastPoint.setX(lastPoint.getX() + 25);
				} else {
					middlePoint = this.createBDiPoint(firstX, lastY);
					// firstPoint.setX(firstPoint.getX());
					firstPoint.setY(firstPoint.getY() + 25);
					// lastPoint.setX(lastPoint.getX() - 25);
					// lastPoint.setY(lastPoint.getY() + 25);
				}
				waypoints.add(firstPoint);
				waypoints.add(middlePoint);
				waypoints.add(lastPoint);
			} else if (fo.getbObjectRef() instanceof MessageFlow) {
				// firstPoint.setX(firstPoint.getX() - 25);
				firstPoint.setY(firstPoint.getY() + 25);
				lastPoint.setX(lastPoint.getX() + 25);
				lastPoint.setY(lastPoint.getY() - 25);
				waypoints.add(firstPoint);
				waypoints.add(lastPoint);
			} else {
				waypoints.add(firstPoint);
				waypoints.add(lastPoint);
			}
		}
	}

	private LayoutQuadrant _layoutEvent(FEvent fFlowNode, LayoutQuadrant qIn) {
		// Clone
		LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
				qIn.getStartY(), qIn.getEndX(), qIn.getEndY());

		// Create Activity
		this._layoutBounds(fFlowNode.getName(), this.GRID_X * qOut.getStartX(),
				this.GRID_Y * qOut.getStartY());

		// After
		qOut.setEndX(qOut.getStartX() + 1);
		qOut.setEndY(qOut.getStartY() + 1);

		return qOut;
	}

	private LayoutQuadrant _layoutFlowNode(FFlowNode fFlowNode,
			LayoutQuadrant qIn) {
		// Clone
		LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
				qIn.getStartY(), qIn.getEndX(), qIn.getEndY());

		if (fFlowNode instanceof FTask) {
			qOut = this._layoutActivity(fFlowNode, qOut);
		} else if (fFlowNode instanceof FCallActivity) {
			qOut = this._layoutActivity(fFlowNode, qOut);
		} else if (fFlowNode instanceof FSubProcess) {
			qOut = this._layoutSubProcess((FSubProcess) fFlowNode, qOut);
		} else if (fFlowNode instanceof FEvent) {
			qOut = this._layoutEvent((FEvent) fFlowNode, qOut);
		} else if (fFlowNode instanceof FGatewayDiverging) {
			qOut = this._layoutGateway(((FGatewayDiverging) fFlowNode), qOut);
		} else if (fFlowNode instanceof FLane) {
			if (qOut.getEndY() != 0) {
				qOut.setStartY(qOut.getEndY() + 1);
			}
			qOut = this._layoutLane((FLane) fFlowNode, qOut);
			qOut.setStartX(0);
			qOut.setEndX(0);
		}

		// After
		// qOut.setEndX(qOut.getEndX() + 1);
		// qOut.setEndY(qOut.getEndY() + 1);

		return qOut;
	}

	// private LayoutQuadrant _layoutEvent(FFlowNode fFlowNode, LayoutQuadrant
	// qIn) {
	// // Clone
	// LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
	// qIn.getStartY(), qIn.getEndX(), qIn.getEndY());
	// this._layoutBounds(fFlowNode.getName(), qOut.getStartX(),
	// qIn.getStartY());
	// qOut.setEndX(qOut.getEndX() + 1);
	// qOut.setEndY(qIn.getEndY() + 1);
	// return qOut;
	// }

	private LayoutQuadrant _layoutFlowNodes(EList<FFlowNode> fFlowNodes,
			LayoutQuadrant qIn) {
		// Clone
		LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
				qIn.getStartY(), qIn.getEndX(), qIn.getEndY());

		// Create FFlowNodes
		int maxY = qIn.getEndY();
		for (FFlowNode fFlowNode : (fFlowNodes)) {
			qOut.setStartY(qIn.getStartY());
			qOut = this._layoutFlowNode(fFlowNode, qOut);
			if (qOut.getEndY() > maxY) {
				maxY = qOut.getEndY();
			}
			qOut.setStartX(qOut.getEndX());
			// qOut.setStartY(qOut.getEndY());
		}

		// After
		qOut.setStartX(qIn.getStartX());
		qOut.setStartY(qIn.getStartY());
		qOut.setEndY(maxY);
		// qOut.setStartX(qIn.getStartX());
		// qOut.setStartY(qOut.getEndY());

		return qOut;
	}

	private LayoutQuadrant _layoutGateway(FGatewayDiverging fGatewayDiverging,
			LayoutQuadrant qIn) {
		// Clone
		LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
				qIn.getStartY(), qIn.getEndX(), qIn.getEndY());

		// Before (Diverging Gateway)
		this._layoutBounds(fGatewayDiverging.getName(),
				this.GRID_X * qOut.getStartX(), this.GRID_Y * qOut.getStartY());
		// Before
		qOut.setStartX(qOut.getStartX() + 1);
		qOut.setEndX(qOut.getEndX() + 1);
		// qOut.setEndY(qIn.getEndY() + 1);

		// Create Options
		qOut = this._layoutOptions(fGatewayDiverging.getOptions(), qOut);

		// After (Converging Gateway)
		qOut.setStartX(qOut.getEndX());
		qOut.setStartY(qIn.getStartY());
		String gatewayConvergingName = fGatewayDiverging.getGatewayConverging()
				.getName();
		if (gatewayConvergingName == null) {
			gatewayConvergingName = fGatewayDiverging.getName() + "_Converging";
		}
		this._layoutBounds(gatewayConvergingName,
				this.GRID_X * qOut.getStartX(), this.GRID_Y * qIn.getStartY());
		// After
		qOut.setStartX(qIn.getStartX());
		// qOut.setStartY(qIn.getStartY());
		qOut.setEndX(qOut.getEndX() + 1);
		// qOut.setEndY(qOut.getEndY() + 1);

		return qOut;
	}

	private LayoutQuadrant _layoutLane(FLane fLane, LayoutQuadrant qIn) {
		// Clone
		LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
				qIn.getStartY(), qIn.getEndX(), qIn.getEndY());

		// Create FlowNodes
		qOut = this._layoutFlowNodes(fLane.getFlowNodes(), qOut);

		// Create Lane
		qOut.setStartX(0);
		// qOut.setEndY(qOut.getEndY());
		this._layoutBounds(
				fLane.getName(),
				(this.GRID_X * qIn.getStartX()) - (this.GRID_X / 2),
				(this.GRID_Y * qIn.getStartY()) - (this.GRID_Y / 2),
				(this.GRID_X * (qOut.getEndX() - qOut.getStartX()) + (this.GRID_X / 2)),
				(this.GRID_Y * (qOut.getEndY() - qOut.getStartY()) + (this.GRID_Y / 2)));

		// After
		// qOut.setEndX(0);
		// qOut.setStartY(qIn.getStartY());
		return qOut;
	}

	private LayoutQuadrant _layoutOption(FOption fOption, LayoutQuadrant qIn) {
		// Clone
		LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
				qIn.getStartY(), qIn.getEndX(), qIn.getEndY());

		// Create FFlowNodes
		qOut = this._layoutFlowNodes(
				this._extractFFlowsNodeFromFOptionFlowNodes(fOption
						.getFlowNodes()), qOut);

		// After
		qOut.setStartX(qIn.getStartX());
		qOut.setStartY(qIn.getStartY());

		return qOut;
	}

	private LayoutQuadrant _layoutOptions(EList<FOption> options,
			LayoutQuadrant qIn) {
		// Clone
		LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
				qIn.getStartY(), qIn.getEndX(), qIn.getEndY());

		// Before
		for (FOption fOption : options) {
			if (fOption.getFlowNodes().isEmpty()) {
				qOut.setStartY(qOut.getStartY() + 1);
				qOut.setEndY(qOut.getEndY() + 1);
				break;
			}
		}

		// Create Options
		int maxX = qOut.getEndX();
		int maxY = qOut.getEndY();
		for (FOption fOption : options) {
			if (!fOption.getFlowNodes().isEmpty()) {
				qOut = this._layoutOption(fOption, qOut);

				if (qOut.getEndX() > maxX) {
					maxX = qOut.getEndX();
				}
				if (qOut.getEndY() > maxY) {
					maxY = qOut.getEndY();
				}
				qOut.setStartY(qOut.getEndY());
				qOut.setEndY(qOut.getEndY());
			}
		}

		// After
		qOut.setStartX(qIn.getStartX());
		qOut.setStartY(qIn.getStartY());
		qOut.setEndX(maxX);
		qOut.setEndY(maxY);

		return qOut;
	}

	private LayoutQuadrant _layoutPool(FPool fPool, LayoutQuadrant qIn) {
		// Clone
		LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
				qIn.getStartY(), qIn.getEndX(), qIn.getEndY());

		// Create FlowNodes
		// qOut.setStartX(qOut.getStartX() + 1);
		// qOut.setEndX(qOut.getEndX() + 1);
		// qOut.setStartY(qOut.getStartY() + 1);
		// qOut.setEndY(qOut.getEndY() + 1);
		qOut = this._layoutFlowNodes(fPool.getFlowNodes(), qOut);

		// Create Pool
		qOut.setStartX(0);
		qOut.setEndY(qOut.getEndY());
		this._layoutBounds(
				fPool.getName() + "_Participant",
				(this.GRID_X * qIn.getStartX()) - (this.GRID_X / 2),
				(this.GRID_Y * qIn.getStartY()) - (this.GRID_Y / 2),
				(this.GRID_X * (qOut.getEndX() - qOut.getStartX()) + (this.GRID_X / 2)),
				(this.GRID_Y * (qOut.getEndY() - qOut.getStartY()) + (this.GRID_Y / 2)));

		// After
		// qOut.setStartX(qIn.getStartX());
		// qOut.setStartY(qIn.getStartY());

		return qOut;
	}

	// TODO
	private LayoutQuadrant _layoutPools(EList<FPool> pools, LayoutQuadrant qIn) {
		// Clone
		LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
				qIn.getStartY(), qIn.getEndX(), qIn.getEndY());

		// Create Pools
		for (FPool fPool : pools) {
			qOut = this._layoutPool(fPool, qOut);
			qOut.setStartY(qOut.getEndY() + 1);
		}

		// After
		// qOut.setStartX(0);
		// qOut.setStartY(qIn.getStartY());

		return qOut;
	}

	private LayoutQuadrant _layoutSubProcess(FSubProcess fSubProcess,
			LayoutQuadrant qIn) {
		// Clone
		LayoutQuadrant qOut = new LayoutQuadrant(qIn.getStartX(),
				qIn.getStartY(), qIn.getEndX(), qIn.getEndY());

		// Create FlowNodes
		qOut = this._layoutFlowNodes(fSubProcess.getFlowNodes(), qOut);

		// Create Lane
		// qOut.setStartX(0);
		// qOut.setEndY(qOut.getEndY());
		this._layoutBounds(
				fSubProcess.getName(),
				(this.GRID_X * qIn.getStartX()) - (this.GRID_X / 2),
				(this.GRID_Y * qIn.getStartY()) - (this.GRID_Y / 2),
				(this.GRID_X * (qOut.getEndX() - qOut.getStartX()) + (this.GRID_X / 2)),
				(this.GRID_Y * (qOut.getEndY() - qOut.getStartY()) + (this.GRID_Y / 2)));

		// After
		// qOut.setEndX(0);
		// qOut.setStartY(qIn.getStartY());
		return qOut;
	}

	private Association createBAssociation(String id) {
		Association bAssociation = Bpmn2Factory.eINSTANCE.createAssociation();
		bAssociation.setId(id + "_Association");
		bAssociation.setAssociationDirection(AssociationDirection.ONE);
		return bAssociation;
	}

	private CallActivity createBCallActivity(FCallActivity fCallActivity) {
		CallActivity bCallActivity = Bpmn2Factory.eINSTANCE
				.createCallActivity();
		bCallActivity.setId(fCallActivity.getName());
		bCallActivity.setName(this._extractLabel(fCallActivity.getName(),
				fCallActivity.getLabel()));
		return bCallActivity;
	}

	private Collaboration createBCollaboration(FProcess fProcess) {
		Collaboration bCollaboration = Bpmn2Factory.eINSTANCE
				.createCollaboration();
		bCollaboration.setId(fProcess.getName());
		bCollaboration.setName(this._extractLabel(fProcess.getName(),
				fProcess.getLabel()));
		return bCollaboration;
	}

	private ItemAwareElement createBDataIO(FDataIO fDataIo) {
		ItemAwareElement bDataIo;
		String id = this._extractLabel(fDataIo.getName(), fDataIo.getLabel());
		if (fDataIo.getDataIoType() == FDataIOType.INPUT) {
			bDataIo = Bpmn2Factory.eINSTANCE.createDataInput();
			bDataIo.setId(fDataIo.getName() + "_DataInput");
			((DataInput) bDataIo).setName(id);
		} else {
			bDataIo = Bpmn2Factory.eINSTANCE.createDataOutput();
			bDataIo.setId(fDataIo.getName() + "_DataInput");
			((DataOutput) bDataIo).setName(id);
		}
		return bDataIo;
	}

	private DataObject createBDataObject(FSendData fSendData) {
		DataObject bDataObject = Bpmn2Factory.eINSTANCE.createDataObject();
		bDataObject.setId(fSendData.getName() + "_DataObject");
		bDataObject.setName(this._extractLabel(fSendData.getName(),
				fSendData.getLabel()));
		return bDataObject;
	}

	private Definitions createBDefinitions(
			FProcessPackageDecl fProcessPackageDecl) {
		Definitions bDefinitions = Bpmn2Factory.eINSTANCE.createDefinitions();
		bDefinitions.setId(fProcessPackageDecl.getName());
		// bDefinitions.setName(fProcessPackageDecl.getLabel());
		bDefinitions.setExporter("YAKINDU Requirements");
		bDefinitions.setExporterVersion("0.1");
		bDefinitions.setExpressionLanguage("");
		bDefinitions.setTargetNamespace("");
		bDefinitions.setTypeLanguage("http://www.w3.org/2001/XMLSchema");
		return bDefinitions;
	}

	private Bounds createBDiBounds(BaseElement bFlowNode) {
		Bounds bDiBounds = DcFactory.eINSTANCE.createBounds();
		if (bFlowNode instanceof Activity) {
			bDiBounds.setWidth(100);
			bDiBounds.setHeight(50);
		} else if (bFlowNode instanceof Event) {
			bDiBounds.setWidth(50);
			bDiBounds.setHeight(50);
		} else if (bFlowNode instanceof Gateway) {
			bDiBounds.setWidth(50);
			bDiBounds.setHeight(50);
		} else {
			bDiBounds.setWidth(50);
			bDiBounds.setHeight(50);
		}
		bDiBounds.setX(0);
		bDiBounds.setY(this.GRID_Y * 2);
		return bDiBounds;
	}

	private BPMNDiagram createBDiDiagram(String id, String name) {
		BPMNDiagram bDiDiagram = BpmnDiFactory.eINSTANCE.createBPMNDiagram();
		bDiDiagram.setId("BPMNDiagram_" + id);
		bDiDiagram.setName(this._extractLabel(id, name));
		// BPMNPlane bDiPlane = this.createBDiPlane(id);
		// bDiDiagram.setPlane(bDiPlane);
		return bDiDiagram;
	}

	private BPMNEdge createBDiEdge(String id, BaseElement bBaseElement) {
		BPMNEdge bDiEdge = BpmnDiFactory.eINSTANCE.createBPMNEdge();
		bDiEdge.setId("BPMNEdge_" + id);
		bDiEdge.setBpmnElement(bBaseElement);
		// bDiEdge.getWaypoint().add(this.createBDiPoint(1, 1));
		// bDiEdge.getWaypoint().add(this.createBDiPoint(1, 1));
		return bDiEdge;
	}

	private BPMNPlane createBDiPlane(String id) {
		BPMNPlane bDiPlane = BpmnDiFactory.eINSTANCE.createBPMNPlane();
		bDiPlane.setId("BPMNPlane_" + id);
		return bDiPlane;
	}

	private Point createBDiPoint(int x, int y) {
		Point bDiPoint = DcFactory.eINSTANCE.createPoint();
		bDiPoint.setX(x);
		bDiPoint.setY(y);
		return bDiPoint;
	}

	private BPMNShape createBDiShape(BaseElement bBaseElement) {
		BPMNShape bDiShape = BpmnDiFactory.eINSTANCE.createBPMNShape();
		bDiShape.setId("BPMNShape_" + bBaseElement.getId());
		bDiShape.setBpmnElement(bBaseElement);
		bDiShape.setIsExpanded(false);
		bDiShape.setIsHorizontal(true);
		bDiShape.setBounds(this.createBDiBounds(bBaseElement));
		return bDiShape;
	}

	private Documentation createBDocumentation(FDocumentation fDocumentation) {
		Documentation bDocumentation = Bpmn2Factory.eINSTANCE
				.createDocumentation();
		bDocumentation.setId("Documentation_"
				+ this._extractName(fDocumentation.getName(),
						fDocumentation.getText()));
		bDocumentation.setText(fDocumentation.getText());
		return bDocumentation;
	}

	/**
	 * Creates BPMN 2.0 <code>DocumentRoot</code> from FastBPMN
	 * <code>FDocumentRoot</code>
	 * 
	 * @param fDocumentRoot
	 */
	private DocumentRoot createBDocumentRoot(FDocumentRoot fDocumentRoot) {
		DocumentRoot bDocumentRoot = Bpmn2Factory.eINSTANCE
				.createDocumentRoot();
		return bDocumentRoot;
	}

	private Event createBEvent(FEvent fEvent) {
		Event bEvent = null;
		switch (fEvent.getEventType()) {
		case EVENT:
			bEvent = Bpmn2Factory.eINSTANCE.createIntermediateCatchEvent();
			break;
		case CATCH:
			bEvent = Bpmn2Factory.eINSTANCE.createIntermediateCatchEvent();
			break;
		case END:
			bEvent = Bpmn2Factory.eINSTANCE.createEndEvent();
			break;
		case INTERMEDIATE_CATCH:
			bEvent = Bpmn2Factory.eINSTANCE.createIntermediateCatchEvent();
			break;
		case INTERMEDIATE_THROW:
			bEvent = Bpmn2Factory.eINSTANCE.createIntermediateThrowEvent();
			break;
		case START:
			bEvent = Bpmn2Factory.eINSTANCE.createStartEvent();
			break;
		case THROW:
			bEvent = Bpmn2Factory.eINSTANCE.createImplicitThrowEvent();
			break;
		}

		if (fEvent.getEventDefinitionType() != null) {
			EventDefinition bEventDefinition = null;
			switch (fEvent.getEventDefinitionType()) {
			case BLANK:
				break;
			case CANCEL:
				bEventDefinition = Bpmn2Factory.eINSTANCE
						.createCancelEventDefinition();
				break;
			case COMPENSATION:
				bEventDefinition = Bpmn2Factory.eINSTANCE
						.createCompensateEventDefinition();
				break;
			case CONDITIONAL:
				bEventDefinition = Bpmn2Factory.eINSTANCE
						.createConditionalEventDefinition();
				break;
			case ERROR:
				bEventDefinition = Bpmn2Factory.eINSTANCE
						.createErrorEventDefinition();
				break;
			case ESCALATION:
				bEventDefinition = Bpmn2Factory.eINSTANCE
						.createEscalationEventDefinition();
				break;
			case LINK:
				bEventDefinition = Bpmn2Factory.eINSTANCE
						.createLinkEventDefinition();
				break;
			case MESSAGE:
				bEventDefinition = Bpmn2Factory.eINSTANCE
						.createMessageEventDefinition();
				break;
			case MULTIPLE:
			case PARALLEL_MULTIPLE:
				break;
			case SIGNAL:
				bEventDefinition = Bpmn2Factory.eINSTANCE
						.createSignalEventDefinition();
				break;
			case TERMINATE:
				bEventDefinition = Bpmn2Factory.eINSTANCE
						.createTerminateEventDefinition();
				break;
			case TIMER:
				bEventDefinition = Bpmn2Factory.eINSTANCE
						.createTimerEventDefinition();
				break;
			}
			if (bEventDefinition != null) {
				String eventDefinitionName = fEvent.getEventType().getLiteral();
				eventDefinitionName = Character.toUpperCase(eventDefinitionName
						.charAt(0)) + eventDefinitionName.substring(1);
				bEventDefinition.setId(fEvent.getName() + "_"
						+ eventDefinitionName + "EventDefinition");
				if (bEvent instanceof ThrowEvent) {
					((ThrowEvent) bEvent).getEventDefinitions().add(
							bEventDefinition);
				} else {
					((CatchEvent) bEvent).getEventDefinitions().add(
							bEventDefinition);
				}
			}
		}
		bEvent.setId(fEvent.getName());
		bEvent.setName(this._extractLabel(fEvent.getName(), fEvent.getLabel()));
		return bEvent;
	}

	private Gateway createBGateway(FGateway fGateway) {
		Gateway bGateway;
		switch (fGateway.getGatewayType()) {
		case BLANK:
			bGateway = Bpmn2Factory.eINSTANCE.createComplexGateway();
			break;
		case PARALLEL:
			bGateway = Bpmn2Factory.eINSTANCE.createParallelGateway();
			break;
		case INCLUSIVE:
			bGateway = Bpmn2Factory.eINSTANCE.createInclusiveGateway();
			break;
		case EXCLUSIVE:
			bGateway = Bpmn2Factory.eINSTANCE.createExclusiveGateway();
			break;
		case EVENTBASED:
			bGateway = Bpmn2Factory.eINSTANCE.createEventBasedGateway();
			break;
		case COMPLEX:
			bGateway = Bpmn2Factory.eINSTANCE.createComplexGateway();
			break;
		default:
			bGateway = Bpmn2Factory.eINSTANCE.createComplexGateway();
			break;
		}
		bGateway.setId(fGateway.getName());
		bGateway.setName(this._extractLabel(fGateway.getName(),
				fGateway.getLabel()));
		return bGateway;
	}

	private InputOutputSpecification createBIoSpecification(String id) {
		InputOutputSpecification bIoSpecification = Bpmn2Factory.eINSTANCE
				.createInputOutputSpecification();
		bIoSpecification.setId(id);
		return bIoSpecification;
	}

	private ItemDefinition createBItemDefinition(FSendMessage fSendMessage) {
		ItemDefinition bItemDefinition = Bpmn2Factory.eINSTANCE
				.createItemDefinition();
		bItemDefinition.setId(fSendMessage.getName());
		bItemDefinition.setStructureRef(fSendMessage.getDataType());
		// bItemDefinition.setStructureRef(fSendMessage.getDataType().getName());
		return bItemDefinition;
	}

	private Lane createBLane(FLane fLane) {
		Lane bLane = Bpmn2Factory.eINSTANCE.createLane();
		bLane.setId(fLane.getName());
		bLane.setName(this._extractLabel(fLane.getName(), fLane.getLabel()));
		return bLane;
	}

	private LaneSet createBLaneSet(FPool fPool) {
		LaneSet bLaneSet = Bpmn2Factory.eINSTANCE.createLaneSet();
		bLaneSet.setId(fPool.getName());
		bLaneSet.setName(this._extractLabel(fPool.getName(), fPool.getLabel()));
		return bLaneSet;
	}

	private LaneSet createBLaneSet(FProcess fProcess) {
		LaneSet bLaneSet = Bpmn2Factory.eINSTANCE.createLaneSet();
		bLaneSet.setId(fProcess.getName());
		bLaneSet.setName(this._extractLabel(fProcess.getName(),
				fProcess.getLabel()));
		return bLaneSet;
	}

	private Message createBMessage(FSendMessage fSendMessage) {
		Message bMessage = Bpmn2Factory.eINSTANCE.createMessage();
		bMessage.setId(fSendMessage.getName());
		bMessage.setName(this._extractLabel(fSendMessage.getName(),
				fSendMessage.getLabel()));
		return bMessage;
	}

	private MessageFlow createBMessageFlow(FSendMessage fSendMessage) {
		MessageFlow bMessageFlow = Bpmn2Factory.eINSTANCE.createMessageFlow();
		bMessageFlow.setId(fSendMessage.getName());
		bMessageFlow.setName(this._extractLabel(fSendMessage.getName(),
				fSendMessage.getLabel()));
		return bMessageFlow;
	}

	private Participant createBParticipant(FPool fPool) {
		Participant bParticipant = Bpmn2Factory.eINSTANCE.createParticipant();
		bParticipant.setId(fPool.getName() + "_Participant");
		bParticipant.setName(this._extractLabel(fPool.getName(),
				fPool.getLabel()));
		return bParticipant;
	}

	private Process createBProcess(FPool fPool) {
		Process bProcess = Bpmn2Factory.eINSTANCE.createProcess();
		bProcess.setId(fPool.getName());
		bProcess.setName(this._extractLabel(fPool.getName(), fPool.getLabel()));
		bProcess.setProcessType(ProcessType.NONE);
		return bProcess;
	}

	private Process createBProcess(FProcess fProcess) {
		Process bProcess = Bpmn2Factory.eINSTANCE.createProcess();
		bProcess.setId(fProcess.getName());
		bProcess.setName(this._extractLabel(fProcess.getName(),
				fProcess.getLabel()));
		bProcess.setProcessType(ProcessType.NONE);
		return bProcess;
	}

	private SequenceFlow createBSequenceFlow(FlowNode bSourceFlowNode,
			FlowNode bTargetFlowNode, String id, String name) {
		SequenceFlow bSequenceFlow = Bpmn2Factory.eINSTANCE
				.createSequenceFlow();
		if (id == null) {
			id = bSourceFlowNode.getId() + "_to_" + bTargetFlowNode.getId()
					+ "_SequenceFlow";
		}
		bSequenceFlow.setId(id);
		bSequenceFlow.setName(name);
		bSequenceFlow.setSourceRef(bSourceFlowNode);
		bSequenceFlow.setTargetRef(bTargetFlowNode);
		bSourceFlowNode.getOutgoing().add(bSequenceFlow);
		bTargetFlowNode.getIncoming().add(bSequenceFlow);
		return bSequenceFlow;
	}

	private SubProcess createBSubProcess(FSubProcess fSubProcess) {
		SubProcess bSubProcess = Bpmn2Factory.eINSTANCE.createSubProcess();
		bSubProcess.setId(fSubProcess.getName());
		bSubProcess.setName(this._extractLabel(fSubProcess.getName(),
				fSubProcess.getLabel()));
		return bSubProcess;
	}

	private Task createBTask(FTask fTask) {
		Task bTask;
		switch (fTask.getTaskType()) {
		case BLANK:
			bTask = Bpmn2Factory.eINSTANCE.createTask();
			break;
		case BUSINESSRULE:
			bTask = Bpmn2Factory.eINSTANCE.createBusinessRuleTask();
			break;
		case MANUAL:
			bTask = Bpmn2Factory.eINSTANCE.createManualTask();
			break;
		case RECEIVE:
			bTask = Bpmn2Factory.eINSTANCE.createReceiveTask();
			break;
		case SCRIPT:
			bTask = Bpmn2Factory.eINSTANCE.createScriptTask();
			break;
		case SEND:
			bTask = Bpmn2Factory.eINSTANCE.createSendTask();
			break;
		case SERVICE:
			bTask = Bpmn2Factory.eINSTANCE.createServiceTask();
			break;
		case USER:
			bTask = Bpmn2Factory.eINSTANCE.createUserTask();
			break;
		default:
			bTask = Bpmn2Factory.eINSTANCE.createTask();
			break;
		}
		bTask.setId(fTask.getName());
		bTask.setName(this._extractLabel(fTask.getName(), fTask.getLabel()));
		return bTask;
	}

	private TextAnnotation createBTextAnnotation(FAnnotation fAnnotation) {
		TextAnnotation bTextAnnotation = Bpmn2Factory.eINSTANCE
				.createTextAnnotation();
		bTextAnnotation.setId("TextAnnotation_"
				+ this._extractName(fAnnotation.getName(),
						fAnnotation.getText()));
		bTextAnnotation.setText(fAnnotation.getText());
		return bTextAnnotation;
	}

	private Collaboration getBCollaboration() {
		for (RootElement bRootElement : this.getBDefinitions()
				.getRootElements()) {
			if (bRootElement instanceof Collaboration) {
				return (Collaboration) bRootElement;
			}
		}
		return null;
	}

	private Definitions getBDefinitions() {
		return this.bDocumentRoot.getDefinitions();
	}

	private BPMNDiagram getBDiDiagram() {
		int i = this.getBDefinitions().getDiagrams().size() - 1;
		return this.getBDefinitions().getDiagrams().get(i);
	}

	private BPMNPlane getBDiPlane() {
		return this.getBDiDiagram().getPlane();
	}

	private InputOutputSpecification getBIoSpecification() {
		return this.getBProcess().getIoSpecification();
	}

	private LaneSet getBLaneSet() {
		int i = this.getBProcess().getLaneSets().size() - 1;
		return this.getBProcess().getLaneSets().get(i);
	}

	private Process getBProcess() {
		Process bLastProcess = null;
		for (RootElement bRootElement : this.getBDefinitions()
				.getRootElements()) {
			if (bRootElement instanceof Process) {
				bLastProcess = (Process) bRootElement;
			}
		}
		return bLastProcess;
	}

	private EList<BaseElement> interpreteFActivity(FActivity fActivity) {
		if (fActivity instanceof FTask) {
			return this.interpreteFTask((FTask) fActivity);
		} else if (fActivity instanceof FSubProcess) {
			return this.interpreteFSubProcess((FSubProcess) fActivity);
		} else if (fActivity instanceof FCallActivity) {
			return this.interpreteFCallActivity((FCallActivity) fActivity);
		}
		return null;
	}

	private EList<BaseElement> interpreteFAnnotation(FAnnotation fAnnotation,
			FlowObject flowObject) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();

		// Create BPMN element(s)
		fAnnotation.setName(this._extractName(fAnnotation.getName(),
				fAnnotation.getText()));
		TextAnnotation bTextAnnotation = this
				.createBTextAnnotation(fAnnotation);
		Association bAssociation = this.createBAssociation(fAnnotation
				.getName());
		bAssociation.setAssociationDirection(AssociationDirection.NONE);
		bAssociation.setSourceRef(flowObject.getbObjectRef());
		bAssociation.setTargetRef(bTextAnnotation);

		// Create BPMN Diagram element(s)
		BPMNShape bDiShape = this.createBDiShape(bTextAnnotation);
		BPMNEdge bDiEdge = this.createBDiEdge(bAssociation.getId(),
				bAssociation);
		this.getBDiPlane().getPlaneElement().add(bDiShape);
		this.getBDiPlane().getPlaneElement().add(bDiEdge);

		// Add element(s) to repository
		// FlowObject flowObjectTextAnnotation =
		this.objectRepository.addFlowObject(bTextAnnotation, fAnnotation,
				bDiShape);
		// FlowObject flowObjectAssociation =
		this.objectRepository.addFlowObject(bAssociation, fAnnotation, bDiEdge);

		// Set BPMN-object
		bFlowElements.add(bTextAnnotation);
		bFlowElements.add(bAssociation);

		// Return BPMN element(s)
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFAttachment(FAttachment fAttachment,
			FlowObject flowObject) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();
		if (fAttachment instanceof FSendMessage) {
			return this.interpreteFSendMessage((FSendMessage) fAttachment,
					flowObject);
		} else if (fAttachment instanceof FSendData) {
			return this
					.interpreteFSendData((FSendData) fAttachment, flowObject);
		} else if (fAttachment instanceof FDataIO) {
			return this.interpreteFDataIO((FDataIO) fAttachment, flowObject);
		} else if (fAttachment instanceof FAnnotation) {
			return this.interpreteFAnnotation((FAnnotation) fAttachment,
					flowObject);
		} else if (fAttachment instanceof FDocumentation) {
			flowObject
					.getbObjectRef()
					.getDocumentation()
					.add(this.interpreteFDocumentation(
							(FDocumentation) fAttachment, flowObject));
		} else if (fAttachment instanceof FFlow) {
			return this.interpreteFFlow((FFlow) fAttachment, flowObject);
		}
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFAttachments(
			EList<FAttachment> fAttachments, FlowObject flowObject) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();
		for (FAttachment fAttachment : fAttachments) {
			bFlowElements.addAll(this.interpreteFAttachment(fAttachment,
					flowObject));
		}
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFCallActivity(
			FCallActivity fCallActivity) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();

		// Create BPMN element(s)
		CallActivity bCallActivity = this.createBCallActivity(fCallActivity);
		String calledElement = fCallActivity.getCalledElement().getName();
		if (fCallActivity.getName() == null && fCallActivity.getLabel() == null) {
			fCallActivity.setName("CallAt" + calledElement);
			fCallActivity.setLabel("Call: " + calledElement);
		}
		fCallActivity.setName(this._extractName(fCallActivity.getName(),
				fCallActivity.getLabel()));
		// Handle Special Case
		if (this.objectRepository.getFlowObjects().containsKey(calledElement)) {
			bCallActivity
					.setCalledElementRef((CallableElement) this.objectRepository
							.getFlowObjects().get(calledElement)
							.getbObjectRef());
		} else {
			this.objectRepository.addSpecialCase(calledElement, bCallActivity);
		}

		// Create BPMN Diagram element(s)
		BPMNShape bDiShape = this.createBDiShape(bCallActivity);
		this.getBDiPlane().getPlaneElement().add(bDiShape);

		// Add element(s) to repository
		FlowObject flowObjectCallActivity = this.objectRepository
				.addFlowObject(bCallActivity, fCallActivity, bDiShape);

		// Child iteration: FAttachment
		bFlowElements.addAll(this.interpreteFAttachments(
				fCallActivity.getAttachments(), flowObjectCallActivity));

		// Set BPMN-object
		bFlowElements.add(bCallActivity);

		// Return BPMN element(s)
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFDataIO(FDataIO fDataIo,
			FlowObject flowObject) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();

		// Create BPMN element(s)
		fDataIo.setName(this._extractName(fDataIo.getName(), fDataIo.getLabel()));
		Association bAssociation = this.createBAssociation(fDataIo.getName());
		ItemAwareElement bDataIo = this.createBDataIO(fDataIo);
		// TODO: Handle DataType
		// bDataIo.setItemSubjectRef(arg0);
		if (fDataIo.getDataIoType() == FDataIOType.INPUT) {
			this.getBIoSpecification().getDataInputs().add((DataInput) bDataIo);
			bAssociation.setSourceRef(bDataIo);
			bAssociation.setTargetRef(flowObject.getbObjectRef());
		} else {
			this.getBIoSpecification().getDataOutputs()
					.add((DataOutput) bDataIo);
			bAssociation.setSourceRef(flowObject.getbObjectRef());
			bAssociation.setTargetRef(bDataIo);
		}

		// Create BPMN Diagram element(s)
		BPMNShape bDiShape = this.createBDiShape(bDataIo);
		BPMNEdge bDiEdge = this.createBDiEdge(bAssociation.getId(),
				bAssociation);
		this.getBDiPlane().getPlaneElement().add(bDiShape);
		this.getBDiPlane().getPlaneElement().add(bDiEdge);

		// Add element(s) to repository
		// FlowObject flowObjectDataIo =
		this.objectRepository.addFlowObject(bDataIo, fDataIo, bDiShape);
		// FlowObject flowObjectDataAssociation =
		this.objectRepository.addFlowObject(bAssociation, fDataIo, bDiEdge);

		// Set BPMN-object
		// bFlowElements.add(bDataIo);
		bFlowElements.add(bAssociation);

		// Return BPMN element(s)
		return bFlowElements;
	}

	private Documentation interpreteFDocumentation(
			FDocumentation fDocumentation, FlowObject flowObject) {
		// Create BPMN element(s)
		fDocumentation.setName(this._extractName(fDocumentation.getName(),
				fDocumentation.getText()));
		Documentation bDocumentation = this
				.createBDocumentation(fDocumentation);

		// Return BPMN element(s)
		return bDocumentation;
	}

	private void interpreteFDocumentRoot(FDocumentRoot fDocumentRoot) {
		// Create BPMN element(s)
		DocumentRoot bDocumentRoot = this.createBDocumentRoot(fDocumentRoot);
		this.bDocumentRoot = bDocumentRoot;

		// Child iteration: Package
		this.interpreteFProcessPackageDecl(fDocumentRoot.getPackages().get(0));
	}

	private EList<BaseElement> interpreteFEvent(FEvent fEvent) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();

		// Create BPMN element(s)
		fEvent.setName(this._extractName(fEvent.getName(), fEvent.getLabel()));
		Event bEvent = this.createBEvent(fEvent);

		// Create BPMN Diagram element(s)
		BPMNShape bDiShape = this.createBDiShape(bEvent);
		this.getBDiPlane().getPlaneElement().add(bDiShape);

		// Add element(s) to repository
		FlowObject flowObjectEvent = this.objectRepository.addFlowObject(
				bEvent, fEvent, bDiShape);

		// Child iteration: FAttachment
		bFlowElements.addAll(this.interpreteFAttachments(
				fEvent.getAttachments(), flowObjectEvent));

		// Set BPMN-object
		bFlowElements.add(bEvent);

		// Return BPMN element(s)
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFFlow(FFlow fFlow,
			FlowObject flowObject) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();

		// Create BPMN element(s)
		SequenceFlow bSequenceFlow = this.createBSequenceFlow(
				((FlowNode) flowObject.getbObjectRef()),
				((FlowNode) flowObject.getbObjectRef()), fFlow.getName(),
				fFlow.getLabel());
		((FlowNode) flowObject.getbObjectRef()).getOutgoing()
				.add(bSequenceFlow);

		fFlow.setName(this._extractName(fFlow.getName(), fFlow.getLabel()));
		String targetNode = fFlow.getTargetRef().getName();
		if (this.objectRepository.getFlowObjects().containsKey(targetNode)) {
			bSequenceFlow.setTargetRef((FlowNode) this.objectRepository
					.getFlowObjects().get(targetNode).getbObjectRef());
			((FlowNode) this.objectRepository.getFlowObjects().get(targetNode)
					.getbObjectRef()).getIncoming().add(bSequenceFlow);
		} else {
			this.objectRepository.addSpecialCase(targetNode, bSequenceFlow);
		}
		((FlowNode) flowObject.getbObjectRef()).getOutgoing()
				.add(bSequenceFlow);

		// Create BPMN Diagram element(s)
		BPMNEdge bDiEdge = this.createBDiEdge(bSequenceFlow.getId(),
				bSequenceFlow);
		this.getBDiPlane().getPlaneElement().add(bDiEdge);

		// Add element(s) to repository
		this.objectRepository.addFlowObject(bSequenceFlow, fFlow, bDiEdge);

		// Set BPMN-object
		bFlowElements.add(bSequenceFlow);

		// Return BPMN element(s)
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFFlowNode(FFlowNode fFlowNode) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();
		if (fFlowNode instanceof FActivity) {
			bFlowElements.addAll(this
					.interpreteFActivity((FActivity) fFlowNode));
		} else if (fFlowNode instanceof FEvent) {
			bFlowElements.addAll(this.interpreteFEvent((FEvent) fFlowNode));
		} else if (fFlowNode instanceof FGateway) {
			bFlowElements.addAll(this.interpreteFGateway((FGateway) fFlowNode));
		} else if (fFlowNode instanceof FLane) {
			bFlowElements.addAll(this.interpreteFLane((FLane) fFlowNode));
		}
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFFlowNodes(EList<FFlowNode> fFlowNodes) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();
		FlowElement bCurrentFlowElement = null;
		FlowElement bPreviousFlowElement = null;
		SequenceFlow bSequenceFlow = null;
		for (FFlowNode fFlowNode : fFlowNodes) {
			EList<BaseElement> bSubFlowElements = (EList<BaseElement>) this
					.interpreteFFlowNode(fFlowNode);
			if (!(fFlowNode instanceof FLane)) {
				// Get first FlowNode of SubFlowElements
				bCurrentFlowElement = this
						._extractFirstFlowNode(bSubFlowElements);

				// Make SeuquenceFlow
				if ((bPreviousFlowElement != null)
						&& (bCurrentFlowElement != null)) {
					bSequenceFlow = this.createBSequenceFlow(
							(FlowNode) bPreviousFlowElement,
							(FlowNode) bCurrentFlowElement, null, null);
					BPMNEdge bDiEdge = this.createBDiEdge(
							bSequenceFlow.getId(), bSequenceFlow);
					this.getBDiPlane().getPlaneElement().add(bDiEdge);
					this.objectRepository.addFlowObject(bSequenceFlow, null,
							bDiEdge);
					bFlowElements.add(bSequenceFlow);
				}

				// Get last FlowNode of SubFlowElements
				bPreviousFlowElement = this
						._extractLastFlowNode(bSubFlowElements);
			}
			bFlowElements.addAll(bSubFlowElements);
		}
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFGateway(FGateway fGateway) {
		if (fGateway instanceof FGatewayDiverging) {
			return this
					.interpreteFGatewayDiverging((FGatewayDiverging) fGateway);
		} else if (fGateway instanceof FGatewayConverging) {
			return this
					.interpreteFGatewayConverging((FGatewayConverging) fGateway);
		}
		return null;
	}

	private EList<BaseElement> interpreteFGatewayConverging(
			FGatewayConverging fGatewayConverging) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();

		// Create BPMN element(s)
		fGatewayConverging.setName(this._extractName(
				fGatewayConverging.getName(), fGatewayConverging.getLabel()));
		Gateway bGatewayConverging = this.createBGateway(fGatewayConverging);

		// Create BPMN Diagram element(s)
		BPMNShape bDiShape = this.createBDiShape(bGatewayConverging);
		this.getBDiPlane().getPlaneElement().add(bDiShape);

		// Add element(s) to repository
		FlowObject flowObjectTask = this.objectRepository.addFlowObject(
				bGatewayConverging, fGatewayConverging, bDiShape);

		// Child iteration: FAttachment
		bFlowElements.addAll(this.interpreteFAttachments(
				fGatewayConverging.getAttachments(), flowObjectTask));

		// Set BPMN-object
		bFlowElements.add(bGatewayConverging);

		// Return BPMN element(s)
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFGatewayDiverging(
			FGatewayDiverging fGatewayDiverging) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();

		// Create BPMN element(s)
		fGatewayDiverging.setName(this._extractName(
				fGatewayDiverging.getName(), fGatewayDiverging.getLabel()));
		Gateway bGatewayDiverging = this.createBGateway(fGatewayDiverging);
		bGatewayDiverging.setGatewayDirection(GatewayDirection.DIVERGING);
		fGatewayDiverging.getGatewayConverging().setGatewayType(
				fGatewayDiverging.getGatewayType());
		if (fGatewayDiverging.getGatewayConverging().getName() == null) {
			fGatewayDiverging.getGatewayConverging().setName(
					fGatewayDiverging.getName() + "_Converging");
		}
		Gateway bGatewayConverging = this.createBGateway(fGatewayDiverging
				.getGatewayConverging());
		bGatewayConverging.setGatewayDirection(GatewayDirection.CONVERGING);

		// Create BPMN Diagram element(s)
		BPMNShape bDiShapeDiverging = this.createBDiShape(bGatewayDiverging);
		BPMNShape bDiShapeConverging = this.createBDiShape(bGatewayConverging);
		this.getBDiPlane().getPlaneElement().add(bDiShapeDiverging);
		this.getBDiPlane().getPlaneElement().add(bDiShapeConverging);

		// Add element(s) to repository
		this.objectRepository.addFlowObject(bGatewayDiverging,
				fGatewayDiverging, bDiShapeDiverging);
		this.objectRepository.addFlowObject(bGatewayConverging,
				fGatewayDiverging, bDiShapeConverging);

		// Child iteration: FOption
		EList<BaseElement> bGatwayFlowElements = new BasicEList<BaseElement>();
		EList<BaseElement> bSubFlowElements;
		SequenceFlow bIncomingSequenceFlow = null;
		SequenceFlow bOutgoingSequenceFlow = null;

		for (FOption fOption : fGatewayDiverging.getOptions()) {
			bSubFlowElements = (EList<BaseElement>) this
					.interpreteFFlowNodes(this
							._extractFFlowsNodeFromFOptionFlowNodes(fOption
									.getFlowNodes()));
			String label = this._extractLabel(fOption.getName(),
					fOption.getLabel());

			if (bSubFlowElements.size() == 0) {
				// If there is no FlowNode for this Option then create direct
				// Flow to converging Gateway
				SequenceFlow bSequenceFlow = this.createBSequenceFlow(
						bGatewayDiverging, bGatewayConverging,
						fOption.getName(), label);
				BPMNEdge bDiEdge = this.createBDiEdge(bSequenceFlow.getId(),
						bSequenceFlow);
				this.getBDiPlane().getPlaneElement().add(bDiEdge);
				this.objectRepository.addFlowObject(bSequenceFlow, null,
						bDiEdge);
				bGatwayFlowElements.add(bSequenceFlow);
			} else {
				// First SequenceFlow
				FlowNode firstFlowNode = this
						._extractFirstFlowNode(bSubFlowElements);
				bIncomingSequenceFlow = this.createBSequenceFlow(
						bGatewayDiverging, firstFlowNode, fOption.getName(),
						label);
				BPMNEdge bDiEdgeIncoming = this.createBDiEdge(
						bIncomingSequenceFlow.getId(), bIncomingSequenceFlow);
				this.getBDiPlane().getPlaneElement().add(bDiEdgeIncoming);
				this.objectRepository.addFlowObject(bIncomingSequenceFlow,
						null, bDiEdgeIncoming);
				bGatwayFlowElements.add(bIncomingSequenceFlow);

				// In between FlowNodes
				bGatwayFlowElements.addAll(bSubFlowElements);

				// Final SequenceFlow
				FlowNode lastFlowNode = this
						._extractLastFlowNode(bSubFlowElements);
				bOutgoingSequenceFlow = this.createBSequenceFlow(lastFlowNode,
						bGatewayConverging, null, null);
				BPMNEdge bDiEdgeOutgoing = this.createBDiEdge(
						bOutgoingSequenceFlow.getId(), bOutgoingSequenceFlow);
				this.getBDiPlane().getPlaneElement().add(bDiEdgeOutgoing);
				this.objectRepository.addFlowObject(bOutgoingSequenceFlow,
						null, bDiEdgeOutgoing);
				bGatwayFlowElements.add(bOutgoingSequenceFlow);
			}

			// Default Option
			if (fOption.isDefault()) {
				if (bGatewayDiverging instanceof InclusiveGateway) {
					((InclusiveGateway) bGatewayDiverging)
							.setDefault(bIncomingSequenceFlow);
				}
				if (bGatewayDiverging instanceof ExclusiveGateway) {
					((ExclusiveGateway) bGatewayDiverging)
							.setDefault(bIncomingSequenceFlow);
				}
			}

			bSubFlowElements = null;
		}

		// Set BPMN-object
		bFlowElements.add(bGatewayDiverging);
		bFlowElements.addAll(bGatwayFlowElements);
		bFlowElements.add(bGatewayConverging);

		// Return BPMN element(s)
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFLane(FLane fLane) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();

		// Create BPMN element(s)
		fLane.setName(this._extractName(fLane.getName(), fLane.getLabel()));
		Lane bLane = this.createBLane(fLane);

		// Create BPMN Diagram element(s)
		BPMNShape bDiShape = this.createBDiShape(bLane);
		this.getBDiPlane().getPlaneElement().add(bDiShape);

		// Add element(s) to repository
		// FlowObject flowObjectLane =
		this.objectRepository.addFlowObject(bLane, fLane, bDiShape);

		// Child iteration: FFlowNode
		bFlowElements = (EList<BaseElement>) this.interpreteFFlowNodes(fLane
				.getFlowNodes());
		for (BaseElement bBaseElement : bFlowElements) {
			if (bBaseElement instanceof FlowNode) {
				bLane.getFlowNodeRefs().add((FlowNode) bBaseElement);
			}
		}

		// Set BPMN-object
		this.getBLaneSet().getLanes().add(bLane);

		// Return BPMN element(s)
		return bFlowElements;
	}

	private void interpreteFPool(FPool fPool) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();
		// EList<RootElement> bRootElements = new BasicEList<RootElement>();

		// Create BPMN element(s)
		// Process
		Process bPoolProcess = this.createBProcess(fPool);
		// Participant
		Participant bParticipant = this.createBParticipant(fPool);
		bParticipant.setProcessRef(bPoolProcess);
		this.getBCollaboration().getParticipants().add(bParticipant);
		// LaneSet
		LaneSet bLaneSet = this.createBLaneSet(fPool);
		bPoolProcess.getLaneSets().add(bLaneSet);
		this.getBDefinitions().getRootElements().add(bPoolProcess);

		// Create BPMN Diagram element(s)
		BPMNShape bDiShape = this.createBDiShape(bParticipant);
		this.getBDiPlane().getPlaneElement().add(bDiShape);

		// Add element(s) to repository
		this.objectRepository.addFlowObject(bParticipant, fPool, bDiShape);

		// Child iteration: InputOutputSpecification
		bPoolProcess.setIoSpecification(this.createBIoSpecification(fPool
				.getName()));

		// Child iteration: FlowNodes
		bFlowElements = (EList<BaseElement>) this.interpreteFFlowNodes(fPool
				.getFlowNodes());
		for (BaseElement bBaseElement : bFlowElements) {
			if (bBaseElement instanceof FlowElement) {
				bPoolProcess.getFlowElements().add((FlowElement) bBaseElement);
			} else if (bBaseElement instanceof Artifact) {
				bPoolProcess.getArtifacts().add((Artifact) bBaseElement);
			}
		}
	}

	private void interpreteFProcess(FProcess fProcess) {
		// EList<RootElement> bRootElements = new BasicEList<RootElement>();
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();
		fProcess.setName(this._extractName(fProcess.getName(),
				fProcess.getLabel()));

		// Create BPMN Diagram element(s)
		BPMNDiagram bDiDiagram = this.createBDiDiagram(fProcess.getName(),
				fProcess.getLabel());
		BPMNPlane bDiPlane = this.createBDiPlane(fProcess.getName());
		bDiDiagram.setPlane(bDiPlane);
		this.getBDefinitions().getDiagrams().add(bDiDiagram);

		if (!fProcess.getPools().isEmpty()) {
			// Create BPMN element(s)
			Collaboration bCollaboration = this.createBCollaboration(fProcess);
			this.getBDefinitions().getRootElements().add(bCollaboration);

			// Create BPMN Diagram element(s)
			bDiPlane.setBpmnElement(bCollaboration);

			// Add element(s) to repository
			this.objectRepository.addFlowObject(bCollaboration, fProcess,
					bDiDiagram);

			// Child iteration: Pools
			for (FPool fPool : fProcess.getPools()) {
				this.interpreteFPool(fPool);
				// bRootElements.add(this.interpreteFPool(fPool));
			}
		} else if (!fProcess.getFlowNodes().isEmpty()) {

			// Create BPMN element(s)
			Process bProcess = this.createBProcess(fProcess);
			// LaneSet
			LaneSet bLaneSet = this.createBLaneSet(fProcess);
			bProcess.getLaneSets().add(bLaneSet);
			this.getBDefinitions().getRootElements().add(bProcess);

			// Create BPMN Diagram element(s)
			bDiPlane.setBpmnElement(bProcess);

			// Add element(s) to repository
			this.objectRepository.addFlowObject(bProcess, fProcess, bDiDiagram);

			// Child iteration: InputOutputSpecification
			bProcess.setIoSpecification(this.createBIoSpecification(fProcess
					.getName()));

			// Child iteration: FlowNodes
			bFlowElements = (EList<BaseElement>) this
					.interpreteFFlowNodes(fProcess.getFlowNodes());
			for (BaseElement bBaseElement : bFlowElements) {
				if (bBaseElement instanceof FlowElement) {
					bProcess.getFlowElements().add((FlowElement) bBaseElement);
				} else if (bBaseElement instanceof Artifact) {
					bProcess.getArtifacts().add((Artifact) bBaseElement);
				}
			}
		}
	}

	private Definitions interpreteFProcessPackageDecl(
			FProcessPackageDecl fProcessPackageDecl) {
		// Create BPMN element(s)
		Definitions bDefinitions = this.createBDefinitions(fProcessPackageDecl);
		this.bDocumentRoot.setDefinitions(bDefinitions);

		// Add element(s) to repository
		this.objectRepository.addFlowObject(bDefinitions, fProcessPackageDecl);

		// Child iteration: Processes
		for (FProcess fProcess : fProcessPackageDecl.getProcesses()) {
			this.interpreteFProcess(fProcess);
		}

		// Return BPMN element(s)
		return bDefinitions;
	}

	private EList<BaseElement> interpreteFSendData(FSendData fSendData,
			FlowObject flowObject) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();

		// Create BPMN element(s)
		fSendData.setName(this._extractName(fSendData.getName(),
				fSendData.getLabel()));
		DataObject bDataObject = this.createBDataObject(fSendData);
		// TODO: Handle DataType
		// bDataObject.setItemSubjectRef(arg0);
		bDataObject.setId(fSendData.getName() + "_DataObject");
		Association bSourceAssociation = this.createBAssociation(fSendData
				.getName());
		bSourceAssociation.setId(fSendData.getName() + "_SourceAssociation");
		bSourceAssociation.setSourceRef(flowObject.getbObjectRef());
		bSourceAssociation.setTargetRef(bDataObject);
		Association bTargetAssociation = this.createBAssociation(fSendData
				.getName());
		bTargetAssociation.setId(fSendData.getName() + "_TargetAssociation");
		bTargetAssociation.setSourceRef(bDataObject);
		// Handle special case
		String targetNode = fSendData.getReceivingElement().getName();
		if (this.objectRepository.getFlowObjects().containsKey(targetNode)) {
			bTargetAssociation.setTargetRef(this.objectRepository
					.getFlowObjects().get(targetNode).getbObjectRef());
		} else {
			this.objectRepository
					.addSpecialCase(targetNode, bTargetAssociation);
		}

		// Create BPMN Diagram element(s)
		BPMNShape bDiShape = this.createBDiShape(bDataObject);
		BPMNEdge bDiEdgeSourceAssociation = this.createBDiEdge(
				bSourceAssociation.getId(), bSourceAssociation);
		BPMNEdge bDiEdgeTargetAssociation = this.createBDiEdge(
				bTargetAssociation.getId(), bTargetAssociation);
		this.getBDiPlane().getPlaneElement().add(bDiShape);
		this.getBDiPlane().getPlaneElement().add(bDiEdgeSourceAssociation);
		this.getBDiPlane().getPlaneElement().add(bDiEdgeTargetAssociation);

		// Add element(s) to repository
		this.objectRepository.addFlowObject(bDataObject, fSendData, bDiShape);
		this.objectRepository.addFlowObject(bSourceAssociation, fSendData,
				bDiEdgeSourceAssociation);
		this.objectRepository.addFlowObject(bTargetAssociation, fSendData,
				bDiEdgeTargetAssociation);

		// Set BPMN-object
		bFlowElements.add(bDataObject);
		bFlowElements.add(bSourceAssociation);
		bFlowElements.add(bTargetAssociation);

		// Return BPMN element(s)
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFSendMessage(
			FSendMessage fSendMessage, FlowObject flowObject) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();

		// Create BPMN element(s)
		fSendMessage.setName(this._extractName(fSendMessage.getName(),
				fSendMessage.getLabel()));
		// Create ItemDefinition
		ItemDefinition bItemDefinition = this
				.createBItemDefinition(fSendMessage);
		bItemDefinition
				.setId(fSendMessage.getName() + "_MessageItemDefinition");
		// Create Message
		Message bMessage = this.createBMessage(fSendMessage);
		bMessage.setId(fSendMessage.getName() + "_Message");
		bMessage.setItemRef(bItemDefinition);
		// Create MessageFlow
		MessageFlow bMessageFlow = this.createBMessageFlow(fSendMessage);
		bMessageFlow.setId(fSendMessage.getName() + "_MessageFlow");
		bMessageFlow.setId(fSendMessage.getName());
		bMessageFlow.setMessageRef(bMessage);
		bMessageFlow.setSourceRef((InteractionNode) flowObject.getbObjectRef());
		// Handle special case
		String targetNode = "";
		if (fSendMessage.getReceivingElement() instanceof FTask) {
			targetNode = ((FTask) fSendMessage.getReceivingElement()).getName();
		} else if (fSendMessage.getReceivingElement() instanceof FEvent) {
			targetNode = ((FEvent) fSendMessage.getReceivingElement())
					.getName();
		} else if (fSendMessage.getReceivingElement() instanceof FPool) {
			targetNode = ((FPool) fSendMessage.getReceivingElement()).getName()
					+ "_Participant";
		}
		if (this.objectRepository.getFlowObjects().containsKey(targetNode)) {
			if (this.objectRepository.getFlowObjects().get(targetNode)
					.getbObjectRef() instanceof InteractionNode)
				bMessageFlow
						.setTargetRef((InteractionNode) this.objectRepository
								.getFlowObjects().get(targetNode)
								.getbObjectRef());
		} else {
			this.objectRepository.addSpecialCase(targetNode, bMessageFlow);
		}

		// Create BPMN Diagram element(s)
		// BPMNShape bDiShape = this.createBDiShape(bMessage);
		BPMNEdge bDiEdge = this.createBDiEdge(bMessageFlow.getId(),
				bMessageFlow);
		// this.getBDiPlane().getPlaneElement().add(bDiShape);
		this.getBDiPlane().getPlaneElement().add(bDiEdge);

		// Add element(s) to repository
		this.objectRepository
				.addFlowObject(bMessageFlow, fSendMessage, bDiEdge);
		this.objectRepository.addFlowObject(bMessage, fSendMessage);
		// ,
		// bDiShape);

		// Set BPMN-object
		this.getBDefinitions().getRootElements().add(bItemDefinition);
		this.getBDefinitions().getRootElements().add(bMessage);
		this.getBCollaboration().getMessageFlows().add(bMessageFlow);

		// Return BPMN element(s)
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFSubProcess(FSubProcess fSubProcess) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();

		// Create BPMN element(s)
		fSubProcess.setName(this._extractName(fSubProcess.getName(),
				fSubProcess.getLabel()));
		SubProcess bSubProcess = this.createBSubProcess(fSubProcess);

		// Create BPMN Diagram element(s)
		BPMNShape bDiShape = this.createBDiShape(bSubProcess);
		this.getBDiPlane().getPlaneElement().add(bDiShape);

		// Add element(s) to repository
		FlowObject flowObjectSubProcess = this.objectRepository.addFlowObject(
				bSubProcess, fSubProcess, bDiShape);

		// Child iteration: FFlowNode
		EList<FFlowNode> fFlowNodes = fSubProcess.getFlowNodes();
		EList<BaseElement> bSubFlowElements = (EList<BaseElement>) this
				.interpreteFFlowNodes(fFlowNodes);
		for (BaseElement bBaseElement : bSubFlowElements) {
			if (bBaseElement instanceof FlowElement) {
				bSubProcess.getFlowElements().add((FlowElement) bBaseElement);
			} else if (bBaseElement instanceof Artifact) {
				bSubProcess.getArtifacts().add((Artifact) bBaseElement);
			}
		}

		// Child iteration: FAttachment
		bFlowElements.addAll(this.interpreteFAttachments(
				fSubProcess.getAttachments(), flowObjectSubProcess));

		// Set BPMN-object
		bFlowElements.add(bSubProcess);

		// Return BPMN element(s)
		return bFlowElements;
	}

	private EList<BaseElement> interpreteFTask(FTask fTask) {
		EList<BaseElement> bFlowElements = new BasicEList<BaseElement>();

		// Create BPMN element(s)
		fTask.setName(this._extractName(fTask.getName(), fTask.getLabel()));
		Task bTask = this.createBTask(fTask);

		// Create BPMN Diagram element(s)
		BPMNShape bDiShape = this.createBDiShape(bTask);
		this.getBDiPlane().getPlaneElement().add(bDiShape);

		// Add element(s) to repository
		FlowObject flowObjectTask = this.objectRepository.addFlowObject(bTask,
				fTask, bDiShape);

		// Child iteration: FAttachment
		bFlowElements.addAll(this.interpreteFAttachments(
				fTask.getAttachments(), flowObjectTask));

		// Set BPMN-object
		bFlowElements.add(bTask);

		// Return BPMN element(s)
		return bFlowElements;
	}
}
