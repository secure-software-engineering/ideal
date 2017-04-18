package typestate.impl.socket;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class SocketAnalysis extends TypestateAnalysis {

  public SocketAnalysis(InfoflowCFG icfg) {
    super(new SocketStateMachine(icfg), icfg);
  }

  public SocketAnalysis(InfoflowCFG icfg,
		  IDebugger<TypestateDomainValue> debugger) {
    super(new SocketStateMachine(icfg), icfg, debugger);
  }
}
