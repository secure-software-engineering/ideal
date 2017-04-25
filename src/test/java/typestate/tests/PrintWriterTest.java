package typestate.tests;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.junit.Test;

import test.ConcreteState;
import test.IDEALTestingFramework;
import typestate.TypestateChangeFunction;
import typestate.impl.statemachines.PrintWriterStateMachine;

public class PrintWriterTest extends IDEALTestingFramework {

	@Test
	public void test1() throws FileNotFoundException {
		PrintWriter inputStream = new PrintWriter("");
		inputStream.close();
		inputStream.flush();
		mustBeInErrorState(inputStream);
	}

	@Override
	protected TypestateChangeFunction<ConcreteState> createTypestateChangeFunction() {
		return new PrintWriterStateMachine();
	}

}