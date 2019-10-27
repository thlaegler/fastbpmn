
package com.laegler.fastbpmn;

/**
 * Initialization support for running Xtext languages 
 * without equinox extension registry
 */
public class FastBpmnStandaloneSetup extends FastBpmnStandaloneSetupGenerated{

	public static void doSetup() {
		new FastBpmnStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}

