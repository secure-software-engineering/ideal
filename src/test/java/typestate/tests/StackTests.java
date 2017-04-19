package typestate.tests;

import org.junit.Test;

import ideal.Analysis;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateDomainValue;
import typestate.impl.vector.VectorAnalysis;
import typestate.tests.base.TypestateTestingFramework;

public class StackTests extends TypestateTestingFramework {

  @Test
  public void test1() {
    expectNErrors("targets.stack.StackTarget1", 3);
  }

  @Test
  public void test2() {
    expectNErrors("targets.stack.StackTarget2", 0);
  }

  @Test
  public void test3() {
    expectNErrors("targets.stack.StackTarget3", 2);
  }

  @Test
  public void test4() {
    expectNErrors("targets.stack.StackTarget4", 2);
  }
  @Override
  protected Analysis<TypestateDomainValue> createAnalysis() {
    return new VectorAnalysis(new InfoflowCFG());
  }

}