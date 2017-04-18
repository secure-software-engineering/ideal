package typestate.tests;

import org.junit.Test;

import ideal.Analysis;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateDomainValue;
import typestate.impl.printstream.PrintStreamAnalysis;
import typestate.tests.base.TypestateTestingFramework;

public class PrintStreamTests extends TypestateTestingFramework {

  @Test
  public void test1() {
    expectAtLeastOneError("printstream.PrintStreamTarget1");
  }

  @Override
  protected Analysis<TypestateDomainValue> createAnalysis() {
    return new PrintStreamAnalysis(new InfoflowCFG());
  }

}