package typestate.tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import ideal.Analysis;
import ideal.ResultReporter;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import test.IDEALTestingFramework;
import typestate.TypestateDomainValue;
import typestate.impl.inputstream.InputStreamAnalysis;

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
	protected Analysis<TypestateDomainValue> createAnalysis(ResultReporter<TypestateDomainValue> reporter) {
		return new InputStreamAnalysis(new InfoflowCFG(), reporter);
	}

}