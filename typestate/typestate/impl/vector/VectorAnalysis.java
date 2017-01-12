package typestate.impl.vector;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class VectorAnalysis extends TypestateAnalysis {

	public VectorAnalysis(InfoflowCFG icfg) {
		super(new VectorStateMachine(icfg), icfg);
	}

	public VectorAnalysis(InfoflowCFG icfg, IDebugger<TypestateDomainValue> debugger) {
		super(new VectorStateMachine(icfg), icfg, debugger);
	}

}
