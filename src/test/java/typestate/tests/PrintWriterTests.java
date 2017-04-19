package typestate.tests;

import org.junit.Test;

import ideal.Analysis;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateDomainValue;
import typestate.impl.printwriter.PrintWriterAnalysis;
import typestate.tests.base.TypestateTestingFramework;

public class PrintWriterTests extends TypestateTestingFramework {

  @Test
  public void test1() {
    expectAtLeastOneError("targets.printwriter.PrintWriterTarget1");
  }

  @Override
  protected Analysis<TypestateDomainValue> createAnalysis() {
    return new PrintWriterAnalysis(new InfoflowCFG());
  }

}