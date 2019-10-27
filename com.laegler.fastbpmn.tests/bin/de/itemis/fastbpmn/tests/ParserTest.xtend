package com.laegler.fastbpmn.tests

import com.laegler.fastbpmn.FastBpmnInjectorProvider
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.junit.runner.RunWith

@InjectWith(typeof(FastBpmnInjectorProvider))
@RunWith(typeof(XtextRunner))
class ParserTest {

//    @Inject extension ParseHelper<Root> parser
//    @Inject extension ValidationTestHelper
//
//    @Test
//    def void testSimpleExample() {
//        parser.parse('''
//        process EinProcess
//        	start event EinStartEvent
//        	task EinTask
//        	end event EinEndEvent
//        end process
//        ''').assertNoErrors
//    }
//
//    @Test
//    def void testGatewayExample() {
//        parser.parse('''
//        process EinProcess
//        	start event EinStartEvent
//        	parallel gateway EinGateway
//        		option ErsteMoeglichkeit
//        			task Task1
//        		option ZweiteMoeglichkeit
//        			task Task2
//        		end gateway
//        	task EinTask
//        	end event EinEndEvent
//        end process
//        ''').assertNoErrors
//    }
}