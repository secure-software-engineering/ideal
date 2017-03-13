package ideal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.AliasFinder;
import boomerang.AliasResults;
import boomerang.BoomerangOptions;
import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import boomerang.context.IContextRequester;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.debug.IDebugger;
import ideal.edgefunction.AnalysisEdgeFunctions;
import ideal.pointsofaliasing.CallSite;
import ideal.pointsofaliasing.InstanceFieldWrite;
import ideal.pointsofaliasing.NullnessCheck;
import ideal.pointsofaliasing.PointOfAlias;
import soot.SootMethod;
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
	private Multimap<CallSite<V>, AccessGraph> callSiteToFlows = HashMultimap.create();
	private Multimap<InstanceFieldWrite<V>, AccessGraph> fieldWritesToFlows = HashMultimap.create();
	private Set<Pair<Unit, AccessGraph>> callSiteToStrongUpdates = new HashSet<>();
	private Set<Pair<Pair<Unit, Unit>, AccessGraph>> nullnessBranches = new HashSet<>();
	private IInfoflowCFG icfg;
	private AnalysisSolver<V> solver;
	private AnalysisEdgeFunctions<V> edgeFunc;
	private Set<AccessGraph> hasEvents = new HashSet<>();
	AliasFinder boomerang;
	private Multimap<AccessGraph,AccessGraph> sourceToTargetFact = HashMultimap.create();


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
	 * This methods stores the results, the indirect flow, from alias queries for the appropriate call sites. Then, in the IDE phase, 
	 * the flow edges can be re-created from those earlier stored flows.
	 * @param callSitePOA
	 * @param res
	 * @param isStrongUpdate
	 */
	public void storeComputedCallSiteFlow(CallSite<V> callSitePOA, Set<PathEdge<Unit, AccessGraph>> res,
			boolean isStrongUpdate) {
		for (PathEdge<Unit, AccessGraph> edge : res) {
			AccessGraph receivesUpdate = edge.factAtTarget();
			callSiteToFlows.put(callSitePOA, receivesUpdate);
			if (isStrongUpdate) {
				debugger.detectedStrongUpdate(callSitePOA.getCallSite(), receivesUpdate);
				callSiteToStrongUpdates
						.add(new Pair<Unit, AccessGraph>(callSitePOA.getCallSite(), receivesUpdate));
			}
		}
	}

	/**
	 * Retrieves for a given call site POA the flow that occured.
	 * @param cs The call site POA object.
	 * @return
	 */
	public Collection<AccessGraph> callSiteFlows(CallSite<V> cs) {
		if (!isInIDEPhase())
			throw new RuntimeException("This can only be applied in the kill phase");
		return callSiteToFlows.get(cs);
	}

	/**
	 * At a field write statement all indirect flows are stored by calling that function.
	 * @param instanceFieldWrite
	 * @param outFlows
	 */
	public void storeComputeInstanceFieldWrite(InstanceFieldWrite<V> instanceFieldWrite,
			Set<AccessGraph> outFlows) {
		fieldWritesToFlows.putAll(instanceFieldWrite, outFlows);
	}

	/**
	 * Retrieve the flows at field write statements.
	 * @param ifr
	 * @return
	 */
	public Collection<AccessGraph> instanceFieldWriteFlows(InstanceFieldWrite<V> ifr) {
		if (!isInIDEPhase())
			throw new RuntimeException("This can only be applied in the kill phase");
		return fieldWritesToFlows.get(ifr);
	}

	/**
	 * For a given callSite check is a strong update can be performed for the returnSideNode.
	 * @param callSite
	 * @param returnSideNode
	 * @return
	 */
	public boolean isStrongUpdate(Unit callSite, AccessGraph returnSideNode) {
		Pair<Unit, AccessGraph> key = new Pair<>(callSite, returnSideNode);
		return Analysis.ENABLE_STRONG_UPDATES && callSiteToStrongUpdates.contains(key);
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
		Analysis.checkTimeout();
		BoomerangOptions opts = new BoomerangOptions();
		opts.setQueryBudget(Analysis.ALIAS_BUDGET);
		opts.setTrackStaticFields(true);
		if(boomerang == null)
			boomerang = new AliasFinder(icfg(),opts);
		debugger.beforeAlias(boomerangAccessGraph, curr, d1);
		try {
			boomerang.startQuery();
			AliasResults res = boomerang.findAliasAtStmt(boomerangAccessGraph, curr,
					getContextRequestorFor(d1, curr)).withoutNullAllocationSites();
			debugger.onAliasesComputed(boomerangAccessGraph, curr, d1, res);
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
		fieldWritesToFlows.clear();
		fieldWritesToFlows = null;
		callSiteToStrongUpdates = null;
		nullnessBranches = null;
		icfg = null;
		solver = null;
		boomerang = null;
		sourceToTargetFact.clear();
	}

	public void addEventFor(AccessGraph fact,Set<AccessGraph> visited) {
		if(visited.contains(fact))
			return;
		visited.add(fact);
		hasEvents.add(fact);
		for(AccessGraph targets : sourceToTargetFact.get(fact)){
			addEventFor(targets, visited);
		}
	}

	public boolean hasEvent(AccessGraph fact) {
		return hasEvents.contains(fact);
	}

	public void flowFromTo(AccessGraph source, AccessGraph target) {
		if(source.equals(target))
			return;
		sourceToTargetFact.put(source,target);
		if(hasEvent(source))
			addEventFor(target, new HashSet<AccessGraph>());
	}

	public void addEventFor(AccessGraph fact) {
		addEventFor(fact, new HashSet<AccessGraph>());
	}

}
