package ideal.debug;

import java.util.Map;
import java.util.Set;

import boomerang.AliasResults;
import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.AnalysisSolver;
import ideal.FactAtStatement;
import ideal.pointsofaliasing.PointOfAlias;
import soot.SootMethod;
import soot.Unit;

public class NullDebugger<V> implements IDebugger<V> {

	@Override
	public void beforeAnalysis() {

	}

	@Override
	public void startPhase2WithSeed(FactAtStatement seed, AnalysisSolver<V> solver) {
	}

	@Override
	public void finishWithSeed(PathEdge<Unit, AccessGraph> seed, boolean timeout, boolean isInErrorState, AnalysisSolver<V> solver) {

	}

	@Override
	public void afterAnalysis() {

	}

	@Override
	public void startAliasPhase(Set<PointOfAlias<V>> pointsOfAlias) {

	}

	@Override
	public void startForwardPhase(Set<PathEdge<Unit, AccessGraph>> worklist) {

	}

	@Override
	public void onAliasesComputed(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1,
			AliasResults res) {

	}

	@Override
	public void onAliasTimeout(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1) {

	}

	@Override
	public void beforeAlias(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1) {

	}

	@Override
	public void killAsOfStrongUpdate(AccessGraph d1, Unit callSite, AccessGraph callNode,
			Unit returnSite, AccessGraph returnSideNode2) {

	}

	@Override
	public void detectedStrongUpdate(Unit callSite, AccessGraph receivesUpdate) {

	}

	@Override
	public void onAnalysisTimeout(FactAtStatement seed) {

	}

	@Override
	public void solvePOA(PointOfAlias<V> p) {

	}

	@Override
	public void onNormalPropagation(AccessGraph sourceFact, Unit curr, Unit succ,AccessGraph d2) {

	}

	@Override
	public void addSummary(SootMethod methodToSummary, PathEdge<Unit, AccessGraph> summary) {
		
	}

	@Override
	public void normalFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		
	}

	@Override
	public void callFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		
	}

	@Override
	public void callToReturn(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		
	}

	@Override
	public void returnFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		
	}

	@Override
	public void setValue(Unit start, AccessGraph startFact, V value) {
		
	}

	@Override
	public void indirectFlowAtWrite(AccessGraph source, Unit curr, AccessGraph target) {
		
	}

	@Override
	public void indirectFlowAtCall(AccessGraph source, Unit curr, AccessGraph target) {
		
	}

	@Override
	public void startWithSeed(FactAtStatement seed) {
		
	}

	@Override
	public void startPhase1WithSeed(FactAtStatement seed, AnalysisSolver<V> solver) {
	}

	@Override
	public void finishPhase1WithSeed(FactAtStatement seed, AnalysisSolver<V> solver) {
		
	}

	@Override
	public void finishPhase2WithSeed(FactAtStatement seed, AnalysisSolver<V> solver) {
		
	}

}
