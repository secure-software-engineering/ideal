package typestate;

import java.util.Set;

import com.google.common.collect.Table.Cell;

import boomerang.accessgraph.AccessGraph;
import ideal.Analysis;
import ideal.ResultReporter;
import ideal.debug.IDebugger;
import soot.SootMethod;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;

public class TypestateAnalysis extends Analysis<TypestateDomainValue> {

	public TypestateAnalysis(TypestateChangeFunction func, InfoflowCFG cfg, ResultReporter<TypestateDomainValue> resultReporter) {
		super(new TypestateAnalysisProblem(func), cfg,resultReporter);
	}

	public TypestateAnalysis(TypestateChangeFunction func, InfoflowCFG cfg, ResultReporter<TypestateDomainValue> resultReporter, IDebugger<TypestateDomainValue> debugger) {
		super(new TypestateAnalysisProblem(func), cfg, resultReporter,debugger);
	}

	public Set<Cell<SootMethod, AccessGraph, TypestateDomainValue>> getErrors() {
		return ((TypestateAnalysisProblem) problem).getErrors();
	}

	public ResultCollection getPathEdgesAtEndOfMethods() {
		return ((TypestateAnalysisProblem) problem).getPathEdgesAtEndOfMethods();
	}
}
