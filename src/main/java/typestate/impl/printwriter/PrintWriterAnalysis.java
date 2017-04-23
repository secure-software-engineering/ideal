package typestate.impl.printwriter;

import ideal.ResultReporter;
import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class PrintWriterAnalysis extends TypestateAnalysis {

	public PrintWriterAnalysis(InfoflowCFG icfg, ResultReporter<TypestateDomainValue> reporter) {
		super(new PrintWriterStateMachine(icfg), icfg, reporter);
	}

}
