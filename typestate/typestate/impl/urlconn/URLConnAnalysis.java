package typestate.impl.urlconn;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class URLConnAnalysis extends TypestateAnalysis {

  public URLConnAnalysis(InfoflowCFG icfg) {
    super(new URLConnStateMachine(icfg), icfg);
  }

  public URLConnAnalysis(InfoflowCFG icfg,
		  IDebugger<TypestateDomainValue> debugger) {
    super(new URLConnStateMachine(icfg), icfg, debugger);
  }

}
