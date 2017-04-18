package typestate.tests;

import org.junit.Test;

import ideal.Analysis;
import typestate.TypestateDomainValue;
import typestate.impl.fileanalysis.FileMustBeClosedAnalysis;
import typestate.tests.base.TypestateTestingFramework;

public class FileMustBeClosedTests extends TypestateTestingFramework {

  @Test
  public void test1() {
    expectNErrors("file.Target1", 0);
  }

  @Test
  public void test2a() {
    expectNErrors("file.Target2", 0);
  }

  @Test
  public void test2b() {
    expectNFacts("file.Target2", 3);
  }


  @Test
  public void test3a() {
    expectNFacts("file.Target3", 3);
  }

  @Test
  public void test3b() {
    expectNErrors("file.Target3", 0);
  }

  @Test
  public void test4a() {
    expectNErrors("file.Target4", 3);
  }

  @Test
  public void test5() {
    expectNErrors("file.Target5", 0);
  }

  @Test
  public void test6() {
    expectNErrors("file.Target6", 4);
  }

  @Test
  public void test7() {
    expectNErrors("file.Target7", 3);
  }

  @Test
  public void test8() {
    expectNErrors("file.Target8", 6);
  }

  @Test
  public void summaryTest() {
    expectNErrors("file.SummaryTarget1", 0);
  }

  @Test
  public void test9() {
    expectNErrors("file.Target9", 0);
  }

  @Test
  public void test12() {
    expectNErrors("file.Target12", 2);
  }

  @Test
  public void test13() {
    expectNErrors("file.Target13", 0);
  }

  @Test
  public void test14() {
    expectNErrors("file.Target14", 0);
  }

  @Test
  public void test15() {
    expectNErrors("file.Target15", 0);
  }

  @Test
  public void test16() {
    expectNErrors("file.Target16", 0);
  }
  @Test
  public void test28() {
    expectNErrors("file.Target28", 0);
  }
  @Test
  public void test29() {
    expectNErrors("file.Target29", 5);
  }
  @Test
  public void test17() {
    expectNErrors("file.Target17", 0);
  }

  @Test
  public void test18() {
    expectNErrors("file.Target18", 5);
  }

  @Test
  public void test19() {
    expectAtLeastOneError("file.Target19");
  }

  @Test
  public void test20() {
    expectNErrors("file.Target20", 5);
  }

  @Test
  public void test22() {
    expectNErrors("file.Target22", 0);
  }

  @Test
  public void test23() {
    expectNErrors("file.Target23", 4);
  }

  @Test
  public void test24() {
    expectNErrors("file.Target24", 5);
  }

  @Test
  public void test25() {
    expectNErrors("file.Target25", 3);
  }
  @Test
  public void test26() {
    expectNErrors("file.Target26", 2);
  }

  @Test
  public void test27() {
    expectNErrors("file.Target27", 3);
  }
  @Test
  public void test11() {
    expectNErrors("file.Target11", 2);
  }

  @Test
  public void test10() {
    expectNErrors("file.Target10", 2);
  }
  @Test
  public void test30() {
    expectNErrors("file.Target30", 0);
  }
  @Override
  protected Analysis<TypestateDomainValue> createAnalysis() {
    return new FileMustBeClosedAnalysis();
  }

}
