/**
 * (c) Copyright 2013 by Itemis AG, Hamburg, Germany
 */
package com.laegler.fastbpmn.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.AbstractFileSystemAccess;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

import com.laegler.fastbpmn.fastBpmn.FDocumentRoot;
import com.laegler.fastbpmn.generator.converter.FastBpmnToBpmnConverter;

/**
 * This class generates BPMN 2.0 from FastBPMN. There is also a serialization
 * feature for exporting the BPMN 2.0 object tree to a valid xml-based BPMN 2.0
 * file with the file extension *.bpmn.
 * 
 * @author Thomas Laegler <thomas.laegler@googlemail.com>
 * @version 0.1
 * 
 */
public class FastBpmnGenerator implements IGenerator {

	private final String FILE_EXTENSION_BPMN = "bpmn";
	private final String FILE_EXTENSION_FASTBPMN = "fastbpmn";

	/**
	 * FastBPMN DocumentRoot <code>FDocumentRoot</code>
	 */
	private FDocumentRoot fDocumentRoot;

	/**
	 * BPMN 2.0 DocumentRoot <code>DocumentRoot</code>
	 */
	private DocumentRoot bDocumentRoot;

	/**
	 * FastBPMN to BPMN 2.0 mapping class
	 */
	private FastBpmnToBpmnConverter fastBpmnToBpmnConverter;

	/**
	 * BPMN 2.0 to FastBPMN mapping class
	 */
//	private BpmnToFastBpmnConverter bpmnToFastBpmnConverter;

	/**
	 * Raw file name without prefix
	 */
	private String fileName;

	private AbstractFileSystemAccess fileSystemAccess;

	/**
	 * Public method initiates BPMN 2.0 file generation and backward/reverse
	 * generation of FastBPMN file
	 * 
	 * @param resource
	 * @param fsa
	 */
	public void doGenerate(Resource resource, IFileSystemAccess fsa) {
		// Initialization
		this.fileName = this.extractFileName(resource);
		this.fileSystemAccess = (AbstractFileSystemAccess) fsa;

		// ------------------------------------------------ //
		// Initiate iterative mapping: FastBPMN -> BPMN 2.0 //
		// ------------------------------------------------ //

		// Extract FastBPMN FDocumentRoot
		this.fDocumentRoot = (FDocumentRoot) IterableExtensions
				.<EObject> head(resource.getContents());
		// this.fDocumentRoot = this.parseFastBpmn();
		// Convert FastBPMN FDocumentRoot to BPMN 2.0 DocumentRoot
		this.bDocumentRoot = this.getFastBpmnToBpmnConverter()._initMapping(
				this.fDocumentRoot);
		// Serialize BPMN 2.0 DocumentRoot and write file
		this.fileSystemAccess.generateFile(this.fileName + "."
				+ this.FILE_EXTENSION_BPMN, this.serializeBpmn());

		// -------------------------------------------------------- //
		// Initiate reverse iterative mapping: BPMN 2.0 -> FastBPMN //
		// -------------------------------------------------------- //

		// Parse BPMN 2.0 file to BPMN 2.0 DocumentRoot
//		this.bDocumentRoot = this.parseBpmn();
		// Convert BPMN 2.0 DocumentRoot to FastBPMN FDocumentRoot
//		this.fDocumentRoot = this.getBpmnToFastBpmnMapping()._initMapping(
//				this.bDocumentRoot);
		// Serialize FastBPMN FDocumentRoot and write file
//		this.fileSystemAccess.generateFile(this.fileName + "."
//				+ this.FILE_EXTENSION_FASTBPMN, this.serializeFastBpmn());
	}

	/**
	 * get file name from FastBPMN source file
	 * 
	 * @param aResource
	 * @return
	 */
	private String extractFileName(Resource aResource) {
		String name = aResource.getURI().lastSegment();
		int indexOf = name.indexOf(".");
		return name.substring(0, indexOf);
	}

	/**
	 * @return the bpmnToFastBpmnMapping
	 */
//	private BpmnToFastBpmnMapping getBpmnToFastBpmnMapping() {
//		if (this.bpmnToFastBpmnMapping == null) {
//			this.bpmnToFastBpmnMapping = new BpmnToFastBpmnMapping();
//		}
//		return this.bpmnToFastBpmnMapping;
//	}

	/**
	 * @return the fastBpmnToBpmnMapping
	 */
	private FastBpmnToBpmnConverter getFastBpmnToBpmnConverter() {
//		if (this.fastBpmnToBpmnMapping == null) {
			this.fastBpmnToBpmnConverter = new FastBpmnToBpmnConverter();
//		}
		return this.fastBpmnToBpmnConverter;
	}

	/**
	 * Parse a BPMN 2.0 file (*.bpmn)
	 * 
	 * @return
	 */
	private DocumentRoot parseBpmn() {
		URI uri = this.fileSystemAccess.getURI(this.fileName + "."
				+ this.FILE_EXTENSION_BPMN);

		// ResourceSet resourceSet = new ResourceSetImpl();
		// final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(
		// resourceSet.getPackageRegistry());
		// resourceSet.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA,
		// extendedMetaData);
		// resourceSet
		// .getResourceFactoryRegistry()
		// .getExtensionToFactoryMap()
		// .put(Resource.Factory.Registry.DEFAULT_EXTENSION,
		// new Bpmn2ResourceFactoryImpl());
		// Resource resource = resourceSet.getResource(uri, true);
		// DocumentRoot documentRoot = (DocumentRoot)
		// resource.getContents().get(0);
		//
		// return documentRoot;

		Resource resource = new Bpmn2ResourceFactoryImpl().createResource(uri);

		resource.unload();

		try {
			resource.load(null);
			return (DocumentRoot) resource.getContents().get(0);
		} catch (IOException e) {
			System.err
					.println("Exception occured while loading the resource file: "
							+ e.getMessage());
		}
		return null;
	}

	// /**
	// * Parse a FastBPMN file (*.fastbpmn)
	// *
	// * @return
	// */
	// private FDocumentRoot parseFastBpmn() {
	// return this.fDocumentRoot;
	// }

	/**
	 * Serializes BPMN 2.0 <code>DocumentRoot</code> to xml-based String
	 * 
	 * @return Content of BPMN 2.0 xml-based file as String
	 */
	private String serializeBpmn() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Map<?, ?> options = new HashMap<Object, Object>();

		Resource resource = new Bpmn2ResourceFactoryImpl().createResource(URI
				.createURI("platform:/resource/./" + this.fileName + "."
						+ this.FILE_EXTENSION_BPMN));
		resource.getContents().add(this.bDocumentRoot);

		try {
			resource.save(output, options);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(output.toString());
		return output.toString();
	}

	/**
	 * Serializes FastBPMN <code>FDocumentRoot</code> to String
	 * 
	 * @return Content of FastBPMN file as String
	 */
	private String serializeFastBpmn() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Map<?, ?> options = new HashMap<Object, Object>();

		XtextResourceSet resourceSet = new XtextResourceSet();
		XtextResource resource = (XtextResource) resourceSet.createResource(URI
				.createURI("test.fastbpmn"));
		resource.getContents().add(this.fDocumentRoot);

		try {
			resource.save(output, options);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.print(output.toString());
		return output.toString();

		// Injector injector = Guice.createInjector(new
		// FastBpmnRuntimeModule());
		// Serializer serializer = injector.getInstance(Serializer.class);
		// String s = serializer.serialize(this.fDocumentRoot);
		//
		// System.out.print(s);
		// return s;
	}

}
