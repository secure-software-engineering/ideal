package typestate.tests;

import org.junit.Test;

import ideal.Analysis;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateDomainValue;
import typestate.impl.keystore.KeyStoreAnalysis;
import typestate.tests.base.TypestateTestingFramework;

public class KeystoreTests extends TypestateTestingFramework {

  @Test
  public void test1() {
    expectNErrors("keystore.KeyStoreTarget1", 0);
  }

  @Test
  public void test2() {
    expectNErrors("keystore.KeyStoreTarget2", 1);
  }

  @Test
  public void test3() {
    expectNErrors("keystore.KeyStoreTarget3", 0);
  }


  @Override
  protected Analysis<TypestateDomainValue> createAnalysis() {
    return new KeyStoreAnalysis(new InfoflowCFG());
  }

}