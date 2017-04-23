package typestate.impl.printstream.allocsite;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.AbstractTypestateAnalysis;
import typestate.TypestateDomainValue;

public class PrintStreamAnalysis extends AbstractTypestateAnalysis {

  public PrintStreamAnalysis(InfoflowCFG icfg) {
    super(new PrintStreamStateMachine(icfg), icfg);
  }

  public PrintStreamAnalysis(InfoflowCFG icfg,
		  IDebugger<TypestateDomainValue> debugger) {
    super(new PrintStreamStateMachine(icfg), icfg, debugger);
  }

}
