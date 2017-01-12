package typestate.tests;

import org.junit.Test;

import ideal.Analysis;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateDomainValue;
import typestate.impl.urlconn.URLConnAnalysis;
import typestate.tests.base.TypestateTestingFramework;

public class URLConnTests extends TypestateTestingFramework {

  @Test
  public void test1() {
    expectNErrors("urlconn.URLConnTarget1", 2);
  }

  @Test
  public void test2() {
    expectNErrors("urlconn.URLConnTarget2", 0);
  }

  @Override
  protected Analysis<TypestateDomainValue> createAnalysis() {
    return new URLConnAnalysis(new InfoflowCFG());
  }

}