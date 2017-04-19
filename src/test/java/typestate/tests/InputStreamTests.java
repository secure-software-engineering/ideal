package typestate.tests;

import org.junit.Test;

import ideal.Analysis;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateDomainValue;
import typestate.impl.inputstream.InputStreamAnalysis;
import typestate.tests.base.TypestateTestingFramework;

public class InputStreamTests extends TypestateTestingFramework {

  @Test
  public void test1() {
    expectNErrors("targets.inputstream.InputStreamTarget1", 2);
  }

  @Test
  public void test2() {
    expectAtLeastOneError("targets.inputstream.InputStreamTarget2");
  }

  @Test
  public void test3() {
    expectNErrors("targets.inputstream.InputStreamTarget3", 0);
  }

  @Override
  protected Analysis<TypestateDomainValue> createAnalysis() {
    return new InputStreamAnalysis(new InfoflowCFG());
  }

}