package typestate.impl.iteratoranalysis.allocsite;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;
import typestate.impl.fileanalysis.FileMustBeClosedStateMachine;

public class HasNextAnalysis extends TypestateAnalysis {

  public HasNextAnalysis(InfoflowCFG cfg) {
    super(new HasNextStateMachine(cfg), cfg);
  }
  public HasNextAnalysis(InfoflowCFG cfg, IDebugger<TypestateDomainValue> debugger) {
	super(new HasNextStateMachine(cfg),cfg, debugger);
  }
}
