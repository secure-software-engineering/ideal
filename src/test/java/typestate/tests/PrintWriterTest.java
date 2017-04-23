package typestate.tests;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.junit.Test;

import ideal.Analysis;
import ideal.ResultReporter;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import test.IDEALTestingFramework;
import typestate.TypestateDomainValue;
import typestate.impl.printwriter.PrintWriterAnalysis;

public class PrintWriterTest extends IDEALTestingFramework {

  @Test
  public void test1() throws FileNotFoundException {
	    PrintWriter inputStream = new PrintWriter("");
	    inputStream.close();
	    inputStream.flush();
	    mustBeInErrorState(inputStream);
  }

  @Override
  protected Analysis<TypestateDomainValue> createAnalysis(ResultReporter<TypestateDomainValue> reporter) {
    return new PrintWriterAnalysis(new InfoflowCFG(), reporter);
  }

}