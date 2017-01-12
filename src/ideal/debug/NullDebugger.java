package ideal.debug;

import java.util.Map;
import java.util.Set;

import boomerang.cache.AliasResults;
import heros.EdgeFunction;
import heros.solver.PathEdge;
import ideal.AnalysisSolver;
import ideal.flowfunctions.WrappedAccessGraph;
import ideal.pointsofaliasing.PointOfAlias;
import soot.Unit;

public class NullDebugger<V> implements IDebugger<V> {

	@Override
	public void computedSeeds(Map<PathEdge<Unit, WrappedAccessGraph>, EdgeFunction<V>> seedToInitivalValue) {

	}

	@Override
	public void beforeAnalysis() {

	}

	@Override
	public void startWithSeed(PathEdge<Unit, WrappedAccessGraph> seed) {

	}

	@Override
	public void startPhase1WithSeed(PathEdge<Unit, WrappedAccessGraph> seed, AnalysisSolver<V> solver) {

	}

	@Override
	public void startPhase2WithSeed(PathEdge<Unit, WrappedAccessGraph> s, AnalysisSolver<V> solver) {
	}

	@Override
	public void finishPhase1WithSeed(PathEdge<Unit, WrappedAccessGraph> seed, AnalysisSolver<V> solver) {

	}

	@Override
	public void finishPhase2WithSeed(PathEdge<Unit, WrappedAccessGraph> s, AnalysisSolver<V> solver) {

	}

	@Override
	public void finishWithSeed(PathEdge<Unit, WrappedAccessGraph> seed, boolean timeout, boolean isInErrorState, AnalysisSolver<V> solver) {

	}

	@Override
	public void afterAnalysis() {

	}

	@Override
	public void startAliasPhase(Set<PointOfAlias<V>> pointsOfAlias) {

	}

	@Override
	public void startForwardPhase(Set<PathEdge<Unit, WrappedAccessGraph>> worklist) {

	}

	@Override
	public void onAliasesComputed(WrappedAccessGraph boomerangAccessGraph, Unit curr, WrappedAccessGraph d1,
			AliasResults res) {

	}

	@Override
	public void onAliasTimeout(WrappedAccessGraph boomerangAccessGraph, Unit curr, WrappedAccessGraph d1) {

	}

	@Override
	public void beforeAlias(WrappedAccessGraph boomerangAccessGraph, Unit curr, WrappedAccessGraph d1) {

	}

	@Override
	public void killAsOfStrongUpdate(WrappedAccessGraph d1, Unit callSite, WrappedAccessGraph callNode,
			WrappedAccessGraph returnSideNode, WrappedAccessGraph returnSideNode2) {

	}

	@Override
	public void detectedStrongUpdate(Unit callSite, WrappedAccessGraph receivesUpdate) {

	}

	@Override
	public void onAnalysisTimeout(PathEdge<Unit, WrappedAccessGraph> seed) {

	}

	@Override
	public void solvePOA(PointOfAlias<V> p) {

	}

	@Override
	public void onNormalPropagation(WrappedAccessGraph sourceFact, Unit curr, Unit succ,WrappedAccessGraph d2) {

	}

}
