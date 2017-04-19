package typestate.tests;

import org.junit.Test;

import ideal.Analysis;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateDomainValue;
import typestate.impl.iteratoranalysis.HasNextAnalysis;
import typestate.tests.base.TypestateTestingFramework;

public class IteratorTests extends TypestateTestingFramework {

  @Test
  public void test1() {
    expectNErrors("targets.iterator.IteratorTarget1", 1);
  }

  @Test
  public void test2() {
    expectNErrors("targets.iterator.IteratorTarget2", 0);
  }

  @Test
  public void test3() {
    expectNErrors("targets.iterator.IteratorTarget3", 1);
  }

  @Test
  public void test4() {
    expectNErrors("targets.iterator.IteratorTarget4", 0);
  }


  @Test
  public void test5() {
    expectNErrors("targets.iterator.IteratorTarget5", 0);
  }


  @Test
  public void test6() {
    expectNErrors("targets.iterator.IteratorTarget6", 0);
  }

  @Test
  public void test7() {
    expectNErrors("targets.iterator.IteratorTarget7", 0);
  }

  @Test
  public void test8() {
    expectNErrors("targets.iterator.IteratorTarget8", 0);
  }

  @Test
  public void test9() {
    expectNErrors("targets.iterator.IteratorTarget9", 1);
  }
  @Override
  protected Analysis<TypestateDomainValue> createAnalysis() {
    return new HasNextAnalysis(new InfoflowCFG());
  }

}