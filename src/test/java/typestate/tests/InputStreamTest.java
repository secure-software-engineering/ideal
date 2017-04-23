package typestate.tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Ignore;
import org.junit.Test;

import ideal.debug.IDebugger;
import ideal.debug.NullDebugger;
import test.IDEALTestingFramework;
import typestate.TypestateChangeFunction;
import typestate.TypestateDomainValue;
import typestate.impl.statemachines.InputStreamStateMachine;

@Ignore
public class InputStreamTest extends IDEALTestingFramework {

	@Test
	public void test1() throws IOException {
		InputStream inputStream = new FileInputStream("");
		inputStream.close();
		inputStream.read();
		mustBeInErrorState(inputStream);
	}

	@Test
	public void test2() throws IOException {
	    InputStream inputStream = new FileInputStream("");
	    inputStream.close();
	    inputStream.close();
	    inputStream.read();
	    mustBeInErrorState(inputStream);
	}

	@Test
	public void test3() throws IOException {
	    InputStream inputStream = new FileInputStream("");
	    inputStream.read();
	    inputStream.close();
	    mustBeInAcceptingState(inputStream);
	}

	@Override
	protected TypestateChangeFunction createTypestateChangeFunction() {
		return new InputStreamStateMachine();
	}
	
	@Override
	protected IDebugger<TypestateDomainValue> getDebugger() {
		return new NullDebugger<TypestateDomainValue>();
	}
}