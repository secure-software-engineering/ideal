package ideal.debug;

import java.util.Map;
import java.util.Set;

import boomerang.AliasResults;
import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.InterproceduralCFG;
import heros.solver.IDEDebugger;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.AnalysisSolver;
import ideal.FactAtStatement;
import ideal.IFactAtStatement;
import ideal.pointsofaliasing.PointOfAlias;
import soot.SootMethod;
import soot.Unit;

public interface IDebugger<V>
		extends IDEDebugger<Unit, AccessGraph, SootMethod, V, InterproceduralCFG<Unit, SootMethod>> {

	void beforeAnalysis();

	void startWithSeed(IFactAtStatement seed);

	void startPhase1WithSeed(IFactAtStatement seed, AnalysisSolver<V> solver);

	void startPhase2WithSeed(IFactAtStatement seed, AnalysisSolver<V> solver);

	void finishPhase1WithSeed(IFactAtStatement seed, AnalysisSolver<V> solver);

	void finishPhase2WithSeed(IFactAtStatement seed, AnalysisSolver<V> solver);

	void finishWithSeed(PathEdge<Unit, AccessGraph> seed, boolean timeout, boolean isInErrorState,
			AnalysisSolver<V> solver);

	void afterAnalysis();

	void startAliasPhase(Set<PointOfAlias<V>> pointsOfAlias);

	void startForwardPhase(Set<PathEdge<Unit, AccessGraph>> worklist);

	void onAliasesComputed(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1, AliasResults res);

	void onAliasTimeout(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1);

	void beforeAlias(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1);

	void killAsOfStrongUpdate(AccessGraph d1, Unit callSite, AccessGraph callNode, Unit returnSite,
			AccessGraph returnSideNode2);

	void detectedStrongUpdate(Unit callSite, AccessGraph receivesUpdate);

	void onAnalysisTimeout(IFactAtStatement seed);

	void solvePOA(PointOfAlias<V> p);

	void onNormalPropagation(AccessGraph d1, Unit curr, Unit succ, AccessGraph source);

	void indirectFlowAtWrite(AccessGraph source, Unit curr, AccessGraph target);

	void indirectFlowAtCall(AccessGraph source, Unit curr, AccessGraph target);

}
