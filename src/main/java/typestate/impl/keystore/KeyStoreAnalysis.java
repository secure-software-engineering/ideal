package typestate.impl.keystore;

import ideal.ResultReporter;
import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class KeyStoreAnalysis extends TypestateAnalysis {

  public KeyStoreAnalysis(InfoflowCFG icfg,ResultReporter<TypestateDomainValue> reporter) {
    super(new KeyStoreStateMachine(), icfg,reporter);
  }

  public KeyStoreAnalysis(InfoflowCFG icfg,ResultReporter<TypestateDomainValue> reporter, IDebugger<TypestateDomainValue> debugger) {
    super(new KeyStoreStateMachine(), icfg,reporter, debugger);
  }

}
