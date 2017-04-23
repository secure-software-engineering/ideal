package typestate.impl.outputstream;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.AbstractTypestateAnalysis;
import typestate.TypestateDomainValue;

public class OutputStreamAnalysis extends AbstractTypestateAnalysis {

  public OutputStreamAnalysis(InfoflowCFG icfg) {
    super(new OutputStreamStateMachine(icfg), icfg);
  }
  

  public OutputStreamAnalysis(InfoflowCFG icfg, IDebugger<TypestateDomainValue> debugger) {
    super(new OutputStreamStateMachine(icfg), icfg, debugger);
  }
}
