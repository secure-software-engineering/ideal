package typestate.impl.urlconn;

import ideal.ResultReporter;
import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class URLConnAnalysis extends TypestateAnalysis {

  public URLConnAnalysis(InfoflowCFG icfg,ResultReporter<TypestateDomainValue> reporter) {
    super(new URLConnStateMachine(icfg), icfg,reporter);
  }

  public URLConnAnalysis(InfoflowCFG icfg,ResultReporter<TypestateDomainValue> reporter,
		  IDebugger<TypestateDomainValue> debugger) {
    super(new URLConnStateMachine(icfg), icfg,reporter, debugger);
  }

}
