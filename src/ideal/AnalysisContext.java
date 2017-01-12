package ideal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.AliasFinder;
import boomerang.BoomerangTimeoutException;
import boomerang.accessgraph.AccessGraph;
import boomerang.cache.AliasResults;
import boomerang.cache.ResultCache;
import boomerang.context.IContextRequester;
import boomerang.context.NoContextRequester;
import boomerang.debug.BoomerangEfficiencyDebugger;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.debug.IDebugger;
import ideal.edgefunction.AnalysisEdgeFunctions;
import ideal.flowfunctions.WrappedAccessGraph;
import ideal.pointsofaliasing.CallSite;
import ideal.pointsofaliasing.InstanceFieldWrite;
import ideal.pointsofaliasing.NullnessCheck;
import ideal.pointsofaliasing.PointOfAlias;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.BackwardsInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;

public class AnalysisContext<V> {

  /**
   * Global debugger object.
   */
	public final IDebugger<V> debugger;
	private Set<PointOfAlias<V>> poas = new HashSet<>();
	private boolean idePhase;
	private Multimap<CallSite<V>, WrappedAccessGraph> callSiteToFlows = HashMultimap.create();
	private Multimap<InstanceFieldWrite<V>, WrappedAccessGraph> fieldWritesToFlows = HashMultimap.create();
	private Set<Pair<Unit, AccessGraph>> callSiteToStrongUpdates = new HashSet<>();
	private Set<Pair<Pair<Unit, Unit>, AccessGraph>> nullnessBranches = new HashSet<>();
	private IInfoflowCFG icfg;
	private AnalysisSolver<V> solver;
	private BackwardsInfoflowCFG bwicfg;
	private AnalysisEdgeFunctions<V> edgeFunc;

	public AnalysisContext(IInfoflowCFG icfg, BackwardsInfoflowCFG bwicfg, AnalysisEdgeFunctions<V> edgeFunc,
			IDebugger<V> debugger) {
		this.icfg = icfg;
		this.bwicfg = bwicfg;
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
	public void storeComputedCallSiteFlow(CallSite<V> callSitePOA, Set<PathEdge<Unit, WrappedAccessGraph>> res,
			boolean isStrongUpdate) {
		for (PathEdge<Unit, WrappedAccessGraph> edge : res) {
			WrappedAccessGraph receivesUpdate = edge.factAtTarget();
			callSiteToFlows.put(callSitePOA.ignoreEvent(), receivesUpdate);
			if (isStrongUpdate) {
				debugger.detectedStrongUpdate(callSitePOA.getCallSite(), receivesUpdate);
				callSiteToStrongUpdates
						.add(new Pair<Unit, AccessGraph>(callSitePOA.getCallSite(), receivesUpdate.getDelegate()));
			}
		}
	}

	/**
	 * Retrieves for a given call site POA the flow that occured.
	 * @param cs The call site POA object.
	 * @return
	 */
	public Collection<WrappedAccessGraph> callSiteFlows(CallSite<V> cs) {
		if (!isInIDEPhase())
			throw new RuntimeException("This can only be applied in the kill phase");
		return callSiteToFlows.get(cs.ignoreEvent());
	}

	/**
	 * At a field write statement all indirect flows are stored by calling that function.
	 * @param instanceFieldWrite
	 * @param outFlows
	 */
	public void storeComputeInstanceFieldWrite(InstanceFieldWrite<V> instanceFieldWrite,
			Set<WrappedAccessGraph> outFlows) {
		fieldWritesToFlows.putAll(instanceFieldWrite, outFlows);
	}

	/**
	 * Retrieve the flows at field write statements.
	 * @param ifr
	 * @return
	 */
	public Collection<WrappedAccessGraph> instanceFieldWriteFlows(InstanceFieldWrite<V> ifr) {
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
	public boolean isStrongUpdate(Unit callSite, WrappedAccessGraph returnSideNode) {
		Pair<Unit, AccessGraph> key = new Pair<>(callSite, returnSideNode.getDelegate());
		return callSiteToStrongUpdates.contains(key);
	}

	
	public boolean isNullnessBranch(Unit curr, Unit succ, WrappedAccessGraph returnSideNode) {
		Pair<Pair<Unit, Unit>, AccessGraph> key = new Pair<>(new Pair<Unit, Unit>(curr, succ),
				returnSideNode.getDelegate());
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

	public IContextRequester getContextRequestorFor(final WrappedAccessGraph d1, final Unit stmt) {
		return solver.getContextRequestorFor(d1, stmt);
	}

	public AliasResults aliasesFor(WrappedAccessGraph boomerangAccessGraph, Unit curr, WrappedAccessGraph d1) {
		Analysis.checkTimeout();
		AliasFinder boomerang = new AliasFinder(icfg(), bwicfg);
		boomerang.context.startTime = Stopwatch.createStarted();
		boomerang.context.budgetInMilliSeconds = Analysis.ALIAS_BUDGET;
		debugger.beforeAlias(boomerangAccessGraph, curr, d1);

		try {
			AliasResults res = boomerang.findAliasAtStmt(boomerangAccessGraph.getDelegate(), curr,
					getContextRequestorFor(d1, curr));
			debugger.onAliasesComputed(boomerangAccessGraph, curr, d1, res);
			return res;
		} catch (BoomerangTimeoutException e) {
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
		bwicfg = null;
	}

}
