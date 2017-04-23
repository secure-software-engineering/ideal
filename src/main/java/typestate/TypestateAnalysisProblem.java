package typestate;

import java.util.Collection;

import boomerang.accessgraph.AccessGraph;
import ideal.DefaultIDEALAnalysisDefinition;
import ideal.IDEALAnalysisDefinition;
import ideal.edgefunction.AnalysisEdgeFunctions;
import soot.SootMethod;
import soot.Unit;

public abstract class TypestateAnalysisProblem extends DefaultIDEALAnalysisDefinition<TypestateDomainValue> {
	private TypestateChangeFunction func;

	@Override
	public AnalysisEdgeFunctions<TypestateDomainValue> edgeFunctions() {
		return new TypestateEdgeFunctions(getOrCreateTransitionFunctions());
	}

	private TypestateChangeFunction getOrCreateTransitionFunctions() {
		if(func == null)
			func = createTypestateChangeFunction();
		return func;
	}

	public abstract TypestateChangeFunction createTypestateChangeFunction();

	@Override
	public Collection<AccessGraph> generate(SootMethod method, Unit stmt, Collection<SootMethod> optional) {
		return getOrCreateTransitionFunctions().generate(method, stmt, optional);
	}

}
