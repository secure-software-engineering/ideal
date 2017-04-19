package typestate.tests;

import org.junit.Test;

import ideal.Analysis;
import typestate.TypestateDomainValue;
import typestate.impl.fileanalysis.FileMustBeClosedAnalysis;
import typestate.tests.base.TypestateTestingFramework;

public class FileMustBeClosedTests extends TypestateTestingFramework {

  @Test
  public void test1() {
    expectNErrors("targets.file.Target1", 0);
  }

  @Test
  public void test2a() {
    expectNErrors("targets.file.Target2", 0);
  }

  @Test
  public void test2b() {
    expectNFacts("targets.file.Target2", 3);
  }


  @Test
  public void test3a() {
    expectNFacts("targets.file.Target3", 3);
  }

  @Test
  public void test3b() {
    expectNErrors("targets.file.Target3", 0);
  }

  @Test
  public void test4a() {
    expectNErrors("targets.file.Target4", 3);
  }

  @Test
  public void test5() {
    expectNErrors("targets.file.Target5", 0);
  }

  @Test
  public void test6() {
    expectNErrors("targets.file.Target6", 4);
  }

  @Test
  public void test7() {
    expectNErrors("targets.file.Target7", 3);
  }

  @Test
  public void test8() {
    expectNErrors("targets.file.Target8", 6);
  }

  @Test
  public void summaryTest() {
    expectNErrors("targets.file.SummaryTarget1", 0);
  }

  @Test
  public void test9() {
    expectNErrors("targets.file.Target9", 0);
  }

  @Test
  public void test12() {
    expectNErrors("targets.file.Target12", 2);
  }

  @Test
  public void test13() {
    expectNErrors("targets.file.Target13", 0);
  }

  @Test
  public void test14() {
    expectNErrors("targets.file.Target14", 0);
  }

  @Test
  public void test15() {
    expectNErrors("targets.file.Target15", 0);
  }

  @Test
  public void test16() {
    expectNErrors("targets.file.Target16", 0);
  }
  @Test
  public void test28() {
    expectNErrors("targets.file.Target28", 0);
  }
  @Test
  public void test29() {
    expectNErrors("targets.file.Target29", 5);
  }
  @Test
  public void test17() {
    expectNErrors("targets.file.Target17", 0);
  }

  @Test
  public void test18() {
    expectNErrors("targets.file.Target18", 5);
  }

  @Test
  public void test19() {
    expectAtLeastOneError("targets.file.Target19");
  }

  @Test
  public void test20() {
    expectNErrors("targets.file.Target20", 5);
  }

  @Test
  public void test22() {
    expectNErrors("targets.file.Target22", 0);
  }

  @Test
  public void test23() {
    expectNErrors("targets.file.Target23", 4);
  }

  @Test
  public void test24() {
    expectNErrors("targets.file.Target24", 5);
  }

  @Test
  public void test25() {
    expectNErrors("targets.file.Target25", 3);
  }
  @Test
  public void test26() {
    expectNErrors("targets.file.Target26", 2);
  }

  @Test
  public void test27() {
    expectNErrors("targets.file.Target27", 3);
  }
  @Test
  public void test11() {
    expectNErrors("targets.file.Target11", 2);
  }

  @Test
  public void test10() {
    expectNErrors("targets.file.Target10", 2);
  }
  @Test
  public void test30() {
    expectNErrors("targets.file.Target30", 0);
  }
  @Override
  protected Analysis<TypestateDomainValue> createAnalysis() {
    return new FileMustBeClosedAnalysis();
  }

}
