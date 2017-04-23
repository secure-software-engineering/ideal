package typestate.tests;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.junit.Test;

import ideal.Analysis;
import ideal.ResultReporter;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import test.IDEALTestingFramework;
import typestate.TypestateDomainValue;
import typestate.impl.printstream.PrintStreamAnalysis;

public class PrintStreamTest extends IDEALTestingFramework {

	@Test
	public void test1() throws FileNotFoundException {
		PrintStream inputStream = new PrintStream("");
	    inputStream.close();
	    inputStream.flush();
	    mustBeInErrorState(inputStream);
	}

	@Override
	protected Analysis<TypestateDomainValue> createAnalysis(ResultReporter<TypestateDomainValue> reporter) {
		return new PrintStreamAnalysis(new InfoflowCFG(), reporter);
	}

}