package typestate.impl.inputstream;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class InputStreamAnalysis extends TypestateAnalysis {

  public InputStreamAnalysis(InfoflowCFG icfg) {
    super(new InputStreamStateMachine(icfg), icfg);
  }
  

  public InputStreamAnalysis(InfoflowCFG icfg, IDebugger<TypestateDomainValue> debugger) {
    super(new InputStreamStateMachine(icfg), icfg, debugger);
  }
}
