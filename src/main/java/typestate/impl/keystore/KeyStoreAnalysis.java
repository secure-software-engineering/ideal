package typestate.impl.keystore;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class KeyStoreAnalysis extends TypestateAnalysis {

  public KeyStoreAnalysis(InfoflowCFG icfg) {
    super(new KeyStoreStateMachine(), icfg);
  }

  public KeyStoreAnalysis(InfoflowCFG icfg, IDebugger<TypestateDomainValue> debugger) {
    super(new KeyStoreStateMachine(), icfg, debugger);
  }

}
