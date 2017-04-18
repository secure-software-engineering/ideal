package typestate.impl.printwriter;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class PrintWriterAnalysis extends TypestateAnalysis {

	public PrintWriterAnalysis(InfoflowCFG icfg) {
		super(new PrintWriterStateMachine(icfg), icfg);
	}

	public PrintWriterAnalysis(InfoflowCFG icfg, IDebugger<TypestateDomainValue> debugger) {
		super(new PrintWriterStateMachine(icfg), icfg, debugger);
	}

}
