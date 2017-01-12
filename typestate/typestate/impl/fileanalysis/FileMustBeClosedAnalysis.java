package typestate.impl.fileanalysis;
import ideal.debug.IDebugger;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public class FileMustBeClosedAnalysis extends TypestateAnalysis {
  public FileMustBeClosedAnalysis() {
    super(new FileMustBeClosedStateMachine(), new InfoflowCFG());
  }
  public FileMustBeClosedAnalysis(IDebugger<TypestateDomainValue> debugger) {
	super(new FileMustBeClosedStateMachine(), new InfoflowCFG(), debugger);
  }
}
