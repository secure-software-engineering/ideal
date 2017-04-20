package typestate.impl.fileanalysis;
import ideal.ResultReporter;
import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class FileMustBeClosedAnalysis extends TypestateAnalysis {
  public FileMustBeClosedAnalysis(ResultReporter<TypestateDomainValue> reporter) {
    super(new FileMustBeClosedStateMachine(), new InfoflowCFG(),reporter);
  }
  public FileMustBeClosedAnalysis(ResultReporter<TypestateDomainValue> reporter,IDebugger<TypestateDomainValue> debugger) {
	super(new FileMustBeClosedStateMachine(), new InfoflowCFG(),reporter, debugger);
  }
}
