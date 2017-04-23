package typestate.impl.socket;

import ideal.ResultReporter;
import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class SocketAnalysis extends TypestateAnalysis {

	public SocketAnalysis(InfoflowCFG icfg, ResultReporter<TypestateDomainValue> reporter) {
		super(new SocketStateMachine(icfg), icfg, reporter);
	}

	public SocketAnalysis(InfoflowCFG icfg, ResultReporter<TypestateDomainValue> reporter,
			IDebugger<TypestateDomainValue> debugger) {
		super(new SocketStateMachine(icfg), icfg, reporter, debugger);
	}
}
