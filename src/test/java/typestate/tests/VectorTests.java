package typestate.tests;

import org.junit.Test;

import ideal.Analysis;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateDomainValue;
import typestate.impl.vector.VectorAnalysis;
import typestate.tests.base.TypestateTestingFramework;

public class VectorTests extends TypestateTestingFramework {

  @Test
  public void test1() {
    expectNErrors("vector.VectorTarget1", 2);
  }

  @Test
  public void test2() {
    expectNErrors("vector.VectorTarget2", 0);
  }

  @Test
  public void test3() {
    expectNErrors("vector.VectorTarget3", 2);
  }

  @Test
  public void test4() {
    expectNErrors("vector.VectorTarget4", 2);
  }
  @Override
  protected Analysis<TypestateDomainValue> createAnalysis() {
    return new VectorAnalysis(new InfoflowCFG());
  }

}