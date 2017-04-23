package typestate.tests;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.junit.Test;

import test.IDEALTestingFramework;
import typestate.TypestateChangeFunction;
import typestate.impl.statemachines.PrintStreamStateMachine;

public class PrintStreamTest extends IDEALTestingFramework {

	@Test
	public void test1() throws FileNotFoundException {
		PrintStream inputStream = new PrintStream("");
		inputStream.close();
		inputStream.flush();
		mustBeInErrorState(inputStream);
	}

	@Override
	protected TypestateChangeFunction createTypestateChangeFunction() {
		return new PrintStreamStateMachine();
	}
}