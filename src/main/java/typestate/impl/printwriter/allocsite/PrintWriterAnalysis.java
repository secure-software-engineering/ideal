package typestate.impl.printwriter.allocsite;

import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.AbstractTypestateAnalysis;
import typestate.TypestateDomainValue;

public class PrintWriterAnalysis extends AbstractTypestateAnalysis {

	public PrintWriterAnalysis(InfoflowCFG icfg) {
		super(new PrintWriterStateMachine(icfg), icfg);
	}

	public PrintWriterAnalysis(InfoflowCFG icfg, IDebugger<TypestateDomainValue> debugger) {
		super(new PrintWriterStateMachine(icfg), icfg, debugger);
	}

}
