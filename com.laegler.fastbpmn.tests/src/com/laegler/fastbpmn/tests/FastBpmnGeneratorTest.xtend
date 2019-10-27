package com.laegler.fastbpmn.tests

import com.laegler.fastbpmn.FastBpmnInjectorProvider
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.junit.runner.RunWith
import com.google.inject.Inject
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.junit4.util.ParseHelper
import com.laegler.fastbpmn.fastBpmn.FDocumentRoot
import org.junit.Test
import org.eclipse.xtext.generator.InMemoryFileSystemAccess

@RunWith(typeof(XtextRunner))
@InjectWith(typeof(FastBpmnInjectorProvider))
class FastBpmnGeneratorTest {
	
	@Inject IGenerator underTest
	@Inject ParseHelper<FDocumentRoot> parseHelper 
	
	@Test
	def test() {
		val model = parseHelper.parse('''
		process SendeProzess of participant Sender
			start event StartEvent
			task Task1
			send message EineNeuigkeit to Task1
			task Task2
			parallel gateway Gateway
				option Option2_1
					task Task2_1
				option Option2_2
					task Task2_2
					parallel gateway InnerGateway
						option Optino2_2_1
							task Task2_2_1
						option Option2_2_2
							task Task2_2_2
					end gateway
			end gateway
			task Task3
			annotation EineBemerkung "Hallo dies ist eine Bemerkung"
			end event EndEvent
		end process
		
		process EmpfangsProzess
			task Empfangen
		end process
		
		participant Sender
		participant Empfaenger
		''')
		val fsa = new InMemoryFileSystemAccess()
		underTest.doGenerate(model.eResource, fsa)
		println(fsa.files)
//		assertEquals(2,fsa.files.size)
//		assertTrue(fsa.files.containsKey(IFileSystemAccess::DEFAULT_OUTPUT+"Alice.java"))
//		assertEquals(
//			'''
//			public class Alice {
//				
//			}
//			'''.toString, fsa.files.get(IFileSystemAccess::DEFAULT_OUTPUT+"Alice.java").toString
//		)
//		assertTrue(fsa.files.containsKey(IFileSystemAccess::DEFAULT_OUTPUT+"Bob.java"))
//		assertEquals(
//			'''
//			public class Bob {
//				
//			}
//			'''.toString, fsa.files.get(IFileSystemAccess::DEFAULT_OUTPUT+"Bob.java").toString)
		
	}
	
}
