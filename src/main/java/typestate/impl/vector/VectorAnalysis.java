package typestate.impl.vector;

import ideal.ResultReporter;
import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class VectorAnalysis extends TypestateAnalysis {

	public VectorAnalysis(InfoflowCFG icfg,ResultReporter<TypestateDomainValue> reporter) {
		super(new VectorStateMachine(icfg), icfg, reporter);
	}

	public VectorAnalysis(InfoflowCFG icfg, ResultReporter<TypestateDomainValue> reporter, IDebugger<TypestateDomainValue> debugger) {
		super(new VectorStateMachine(icfg), icfg, reporter, debugger);
	}

}
