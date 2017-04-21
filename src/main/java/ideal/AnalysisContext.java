package ideal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.AliasFinder;
import boomerang.AliasResults;
import boomerang.BoomerangOptions;
import boomerang.accessgraph.AccessGraph;
import boomerang.context.IContextRequester;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.debug.IDebugger;
import ideal.edgefunction.AnalysisEdgeFunctions;
import ideal.pointsofaliasing.AbstractPointOfAlias;
import ideal.pointsofaliasing.CallSite;
import ideal.pointsofaliasing.Event;
import ideal.pointsofaliasing.InstanceFieldWrite;
import ideal.pointsofaliasing.NullnessCheck;
import ideal.pointsofaliasing.PointOfAlias;
import ideal.pointsofaliasing.ReturnEvent;
import soot.Scene;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.BackwardsInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class AnalysisContext<V> {

  /**
   * Global debugger object.
   */
	public final IDebugger<V> debugger;
	private Set<PointOfAlias<V>> poas = new HashSet<>();
	private boolean idePhase;
	private Multimap<PointOfAlias<V>, AccessGraph> callSiteToFlows = HashMultimap.create();
	private Multimap<Unit,AccessGraph> callSiteToStrongUpdates = HashMultimap.create();
	private Set<Pair<Pair<Unit, Unit>, AccessGraph>> nullnessBranches = new HashSet<>();
	private IInfoflowCFG icfg;
	private AnalysisSolver<V> solver;
	private AnalysisEdgeFunctions<V> edgeFunc;
	private Multimap<Unit, AccessGraph> eventAtCallSite = HashMultimap.create();


	public AnalysisContext(IInfoflowCFG icfg, BackwardsInfoflowCFG bwicfg, AnalysisEdgeFunctions<V> edgeFunc,
			IDebugger<V> debugger) {
		this.icfg = icfg;
		this.debugger = debugger;
		this.edgeFunc = edgeFunc;
	}

	public void setSolver(AnalysisSolver<V> solver) {
		this.solver = solver;
	}

	public AnalysisEdgeFunctions<V> getEdgeFunctions() {
		return edgeFunc;
	}

	public boolean addPOA(PointOfAlias<V> poa) {
		return poas.add(poa);
	}

	public Set<PointOfAlias<V>> getAndClearPOA() {
		HashSet<PointOfAlias<V>> res = new HashSet<>(poas);
		poas.clear();
		return res;
	}

	public boolean isInIDEPhase() {
		return idePhase;
	}

	public void enableIDEPhase() {
		idePhase = true;
	}


	/**
	 * Retrieves for a given call site POA the flow that occured.
	 * @param cs The call site POA object.
	 * @return
	 */
	public Collection<AccessGraph> getFlowAtPointOfAlias(PointOfAlias<V> cs) {
		if (!isInIDEPhase())
			throw new RuntimeException("This can only be applied in the kill phase");
		return callSiteToFlows.get(cs);
	}

	/**
	 * At a field write statement all indirect flows are stored by calling that function.
	 * @param instanceFieldWrite
	 * @param outFlows
	 */
	public void storeFlowAtPointOfAlias(PointOfAlias<V> instanceFieldWrite,
			Collection<AccessGraph> outFlows) {
		callSiteToFlows.putAll(instanceFieldWrite, outFlows);
	}

	/**
	 * For a given callSite check is a strong update can be performed for the returnSideNode.
	 * @param callSite
	 * @param returnSideNode
	 * @return
	 */
	public boolean isStrongUpdate(Unit callSite, AccessGraph returnSideNode) {
		return Analysis.ENABLE_STRONG_UPDATES && callSiteToStrongUpdates.get(callSite).contains(returnSideNode);
	}

	public void storeStrongUpdateAtCallSite(Unit callSite, Collection<AccessGraph> mayAliasSet) {
		callSiteToStrongUpdates.putAll(callSite, mayAliasSet);
	}
	
	public boolean isNullnessBranch(Unit curr, Unit succ, AccessGraph returnSideNode) {
		Pair<Pair<Unit, Unit>, AccessGraph> key = new Pair<>(new Pair<Unit, Unit>(curr, succ),
				returnSideNode);
		return nullnessBranches.contains(key);
	}

	public void storeComputedNullnessFlow(NullnessCheck<V> nullnessCheck, AliasResults results) {
		for (AccessGraph receivesUpdate : results.mayAliasSet()) {
			nullnessBranches.add(new Pair<Pair<Unit, Unit>, AccessGraph>(
					new Pair<Unit, Unit>(nullnessCheck.getCurr(), nullnessCheck.getSucc()), receivesUpdate));
		}
	}

	public IInfoflowCFG icfg() {
		return icfg;
	}

	public IContextRequester getContextRequestorFor(final AccessGraph d1, final Unit stmt) {
		return solver.getContextRequestorFor(d1, stmt);
	}

	public AliasResults aliasesFor(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1) {
		if(!Analysis.ALIASING)
			return new AliasResults();
		Analysis.checkTimeout();
		BoomerangOptions opts = new BoomerangOptions();
		opts.setQueryBudget(Analysis.ALIAS_BUDGET);
		opts.setTrackStaticFields(Analysis.ALIASING_FOR_STATIC_FIELDS);
			AliasFinder boomerang = new AliasFinder(icfg(),opts);
		if(!boomerangAccessGraph.isStatic() && Scene.v().getPointsToAnalysis().reachingObjects(boomerangAccessGraph.getBase()).isEmpty())
			return new AliasResults();
		debugger.beforeAlias(boomerangAccessGraph, curr, d1);
		try {
			boomerang.startQuery();
			AliasResults res = boomerang.findAliasAtStmt(boomerangAccessGraph, curr,
					getContextRequestorFor(d1, curr)).withoutNullAllocationSites();
			debugger.onAliasesComputed(boomerangAccessGraph, curr, d1, res);
			if(res.queryTimedout()){
				System.out.println("TIMOEUT");
				debugger.onAliasTimeout(boomerangAccessGraph, curr, d1);
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			debugger.onAliasTimeout(boomerangAccessGraph, curr, d1);
			Analysis.checkTimeout();
			return new AliasResults();
		}
	}

	public void destroy() {
		poas = null;
		callSiteToFlows.clear();
		callSiteToFlows = null;
		callSiteToStrongUpdates = null;
		nullnessBranches = null;
		icfg = null;
		solver = null;
		eventAtCallSite.clear();
	}



}
