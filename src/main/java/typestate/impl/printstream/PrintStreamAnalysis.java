package typestate.impl.printstream;

import ideal.ResultReporter;
import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class PrintStreamAnalysis extends TypestateAnalysis {

  public PrintStreamAnalysis(InfoflowCFG icfg, ResultReporter<TypestateDomainValue> reporter) {
    super(new PrintStreamStateMachine(icfg), icfg, reporter);
  }

  public PrintStreamAnalysis(InfoflowCFG icfg, ResultReporter<TypestateDomainValue> reporter,
		  IDebugger<TypestateDomainValue> debugger) {
    super(new PrintStreamStateMachine(icfg), icfg,reporter, debugger);
  }

}
