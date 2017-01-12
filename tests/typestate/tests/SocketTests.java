package typestate.tests;

import org.junit.Test;

import ideal.Analysis;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateDomainValue;
import typestate.impl.socket.SocketAnalysis;
import typestate.tests.base.TypestateTestingFramework;

public class SocketTests extends TypestateTestingFramework {

  @Test
  public void test1() {
	  expectAtLeastOneError("socket.SocketTarget1");
  }

  @Test
  public void test2() {
    expectAtLeastOneError("socket.SocketTarget2");
  }

  @Test
  public void test3() {
    expectAtLeastOneError("socket.SocketTarget3");
  }

  @Test
  public void test4() {
    expectNErrors("socket.SocketTarget4", 0);
  }

  @Override
  protected Analysis<TypestateDomainValue> createAnalysis() {
    return new SocketAnalysis(new InfoflowCFG());
  }

}
