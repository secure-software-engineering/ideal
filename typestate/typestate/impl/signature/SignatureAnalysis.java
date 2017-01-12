package typestate.impl.signature;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class SignatureAnalysis extends TypestateAnalysis {

  public SignatureAnalysis(InfoflowCFG icfg) {
    super(new SignatureStateMachine(icfg), icfg);
  }

  public SignatureAnalysis(InfoflowCFG icfg,
		  IDebugger<TypestateDomainValue> debugger) {
    super(new SignatureStateMachine(icfg), icfg, debugger);
  }
}
