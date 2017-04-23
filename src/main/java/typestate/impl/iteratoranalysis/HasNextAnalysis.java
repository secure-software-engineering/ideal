package typestate.impl.iteratoranalysis;

import ideal.ResultReporter;
import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class HasNextAnalysis extends TypestateAnalysis {

  public HasNextAnalysis(InfoflowCFG cfg, ResultReporter<TypestateDomainValue> reporter) {
    super(new HasNextStateMachine(cfg), cfg, reporter);
  }
  public HasNextAnalysis(InfoflowCFG cfg, ResultReporter<TypestateDomainValue> reporter, IDebugger<TypestateDomainValue> debugger) {
	    super(new HasNextStateMachine(cfg), cfg, reporter, debugger);
	  }
}
