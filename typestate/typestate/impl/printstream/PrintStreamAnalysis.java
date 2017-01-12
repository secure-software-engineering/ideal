package typestate.impl.printstream;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class PrintStreamAnalysis extends TypestateAnalysis {

  public PrintStreamAnalysis(InfoflowCFG icfg) {
    super(new PrintStreamStateMachine(icfg), icfg);
  }

  public PrintStreamAnalysis(InfoflowCFG icfg,
		  IDebugger<TypestateDomainValue> debugger) {
    super(new PrintStreamStateMachine(icfg), icfg, debugger);
  }

}
