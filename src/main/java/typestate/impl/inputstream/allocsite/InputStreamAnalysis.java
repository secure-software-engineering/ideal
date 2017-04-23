package typestate.impl.inputstream.allocsite;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.AbstractTypestateAnalysis;
import typestate.TypestateDomainValue;

public class InputStreamAnalysis extends AbstractTypestateAnalysis {

  public InputStreamAnalysis(InfoflowCFG icfg) {
    super(new InputStreamStateMachine(icfg), icfg);
  }
  

  public InputStreamAnalysis(InfoflowCFG icfg, IDebugger<TypestateDomainValue> debugger) {
    super(new InputStreamStateMachine(icfg), icfg, debugger);
  }
}
