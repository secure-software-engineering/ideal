package typestate.impl.pipedoutputstream;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class PipedOutputStreamAnalysis extends TypestateAnalysis {

  public PipedOutputStreamAnalysis(InfoflowCFG icfg) {
    super(new PipedOutputStreamStateMachine(icfg), icfg);
  }
  

  public PipedOutputStreamAnalysis(InfoflowCFG icfg, IDebugger<TypestateDomainValue> debugger) {
    super(new PipedOutputStreamStateMachine(icfg), icfg, debugger);
  }
}
