package ideal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.AliasFinder;
import boomerang.AliasResults;
import boomerang.Query;
import boomerang.accessgraph.AccessGraph;
import boomerang.cfg.IExtendedICFG;
import boomerang.context.IContextRequester;
import heros.EdgeFunction;
import heros.edgefunc.EdgeIdentity;
import heros.solver.Pair;
import heros.solver.PathEdge;
import heros.solver.Scheduler;
import heros.utilities.DefaultValueMap;
import ideal.debug.IDebugger;
import ideal.edgefunction.AnalysisEdgeFunctions;
import ideal.pointsofaliasing.NullnessCheck;
import ideal.pointsofaliasing.PointOfAlias;
import ideal.pointsofaliasing.ReturnEvent;
import soot.Scene;
import soot.Unit;

public class PerSeedAnalysisContext<V> {

	/**
	 * Global debugger object.
	 */
	private Set<PointOfAlias<V>> poas = new HashSet<>();
	private boolean idePhase;
	private Multimap<PointOfAlias<V>, AccessGraph> callSiteToFlows = HashMultimap.create();
	private Multimap<Unit, AccessGraph> callSiteToStrongUpdates = HashMultimap.create();
	private Set<Pair<Pair<Unit, Unit>, AccessGraph>> nullnessBranches = new HashSet<>();
	private AnalysisSolver<V> solver;
	private Multimap<Unit, AccessGraph> eventAtCallSite = HashMultimap.create();
	private AliasFinder boomerang;
	private IDEALAnalysisDefinition<V> analysisDefinition;
	private Stopwatch startTime;
	private FactAtStatement seed;
	private Set<PointOfAlias<V>> seenPOA = new HashSet<>();
	private Map<PathEdge<Unit, AccessGraph>, EdgeFunction<V>> pathEdgeToEdgeFunc = new HashMap<>();

	public PerSeedAnalysisContext(IDEALAnalysisDefinition<V> analysisDefinition, FactAtStatement seed) {
		this.seed = seed;
		this.analysisDefinition = analysisDefinition;
		this.scheduler = this.analysisDefinition.getScheduler();
		this.scheduler.setContext(this);
	}

	public void setSolver(AnalysisSolver<V> solver) {
		this.solver = solver;
	}

	public AnalysisEdgeFunctions<V> getEdgeFunctions() {
		return analysisDefinition.edgeFunctions();
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
	 * 
	 * @param cs
	 *            The call site POA object.
	 * @return
	 */
	public Collection<AccessGraph> getFlowAtPointOfAlias(PointOfAlias<V> cs) {
		if (!isInIDEPhase())
			throw new RuntimeException("This can only be applied in the kill phase");
		return callSiteToFlows.get(cs);
	}

	/**
	 * At a field write statement all indirect flows are stored by calling that
	 * function.
	 * 
	 * @param instanceFieldWrite
	 * @param outFlows
	 */
	public void storeFlowAtPointOfAlias(PointOfAlias<V> instanceFieldWrite, Collection<AccessGraph> outFlows) {
		callSiteToFlows.putAll(instanceFieldWrite, outFlows);
	}

	/**
	 * For a given callSite check is a strong update can be performed for the
	 * returnSideNode.
	 * 
	 * @param callSite
	 * @param returnSideNode
	 * @return
	 */
	public boolean isStrongUpdate(Unit callSite, AccessGraph returnSideNode) {
		return analysisDefinition.enableStrongUpdates()
				&& callSiteToStrongUpdates.get(callSite).contains(returnSideNode);
	}

	public void storeStrongUpdateAtCallSite(Unit callSite, Collection<AccessGraph> mayAliasSet) {
		callSiteToStrongUpdates.putAll(callSite, mayAliasSet);
	}

	public boolean isNullnessBranch(Unit curr, Unit succ, AccessGraph returnSideNode) {
		Pair<Pair<Unit, Unit>, AccessGraph> key = new Pair<>(new Pair<Unit, Unit>(curr, succ), returnSideNode);
		return nullnessBranches.contains(key);
	}

	public void storeComputedNullnessFlow(NullnessCheck<V> nullnessCheck, AliasResults results) {
		for (AccessGraph receivesUpdate : results.mayAliasSet()) {
			nullnessBranches.add(new Pair<Pair<Unit, Unit>, AccessGraph>(
					new Pair<Unit, Unit>(nullnessCheck.getCurr(), nullnessCheck.getSucc()), receivesUpdate));
		}
	}

	public IExtendedICFG icfg() {
		return analysisDefinition.icfg();
	}

	public IContextRequester getContextRequestorFor(final AccessGraph d1, final Unit stmt) {
		return solver.getContextRequestorFor(d1, stmt);
	}

	private DefaultValueMap<BoomerangQuery, AliasResults> queryToResult = new DefaultValueMap<BoomerangQuery, AliasResults>() {

		@Override
		protected AliasResults createItem(PerSeedAnalysisContext<V>.BoomerangQuery key) {
			try {
				boomerang.startQuery();
				AliasResults res = boomerang
						.findAliasAtStmt(key.getAp(), key.getStmt(), getContextRequestorFor(key.d1, key.getStmt()))
						.withoutNullAllocationSites();
				analysisDefinition.debugger().onAliasesComputed(key.getAp(), key.getStmt(), key.d1, res);
				if (res.queryTimedout()) {
					analysisDefinition.debugger().onAliasTimeout(key.getAp(), key.getStmt(), key.d1);
				}
				return res;
			} catch (Exception e) {
				e.printStackTrace();
				analysisDefinition.debugger().onAliasTimeout(key.getAp(), key.getStmt(), key.d1);
				checkTimeout();
				return new AliasResults();
			}
		}
	};
	public final IDEALScheduler<V> scheduler;

	private class BoomerangQuery extends Query {

		private AccessGraph d1;

		public BoomerangQuery(AccessGraph accessPath, Unit stmt, AccessGraph d1) {
			super(accessPath, stmt);
			this.d1 = d1;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((d1 == null) ? 0 : d1.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			BoomerangQuery other = (BoomerangQuery) obj;
			if (d1 == null) {
				if (other.d1 != null)
					return false;
			} else if (!d1.equals(other.d1))
				return false;
			return true;
		}

	}

	public void run() {
		debugger().startWithSeed(seed);
		startTime = Stopwatch.createStarted();
		AnalysisSolver<V> solver = new AnalysisSolver<>(analysisDefinition, this);
		setSolver(solver);
		try {
//			System.out.println("================== STARTING PHASE 1 ==================");
			phase1(solver);
			solver.destroy();
			solver = new AnalysisSolver<>(analysisDefinition, this);
//			System.out.println("================== STARTING PHASE 2 ==================");
			phase2(solver);
			setSolver(solver);
		} catch (IDEALTimeoutException e) {
			System.out.println("Timeout of IDEAL, Budget:" + analysisDefinition.analysisBudgetInSeconds());
			debugger().onAnalysisTimeout(seed);
		}
		reporter().onSeedFinished(seed, solver);
		destroy();
		solver.destroy();
	}

	private ResultReporter<V> reporter() {
		return analysisDefinition.resultReporter();
	}

	private void phase1(AnalysisSolver<V> solver) {
		debugger().startPhase1WithSeed(seed, solver);
		Set<PathEdge<Unit, AccessGraph>> worklist = new HashSet<>();
		if (icfg().isExitStmt(seed.getStmt())) {
			worklist.add(new PathEdge<Unit, AccessGraph>(InternalAnalysisProblem.ZERO, seed.getStmt(), seed.getFact()));
		} else {
			for (Unit u : icfg().getSuccsOf(seed.getStmt())) {
				worklist.add(new PathEdge<Unit, AccessGraph>(InternalAnalysisProblem.ZERO, u, seed.getFact()));
			}
		}
		while (!worklist.isEmpty()) {
			debugger().startForwardPhase(worklist);
			for (PathEdge<Unit, AccessGraph> s : worklist) {
				EdgeFunction<V> func = pathEdgeToEdgeFunc.get(s);
				if (func == null)
					func = EdgeIdentity.v();
				solver.injectPhase1Seed(s.factAtSource(), s.getTarget(), s.factAtTarget(), func);
			}
			worklist.clear();
			Set<PointOfAlias<V>> pointsOfAlias = getAndClearPOA();
			debugger().startAliasPhase(pointsOfAlias);
			for (PointOfAlias<V> p : pointsOfAlias) {
				if (seenPOA.contains(p))
					continue;
				seenPOA.add(p);
				debugger().solvePOA(p);
				Collection<PathEdge<Unit, AccessGraph>> edges = p.getPathEdges(this);
				worklist.addAll(edges);
				if (p instanceof ReturnEvent) {
					ReturnEvent<V> returnEvent = (ReturnEvent) p;
					for (PathEdge<Unit, AccessGraph> edge : edges) {
						pathEdgeToEdgeFunc.put(edge, returnEvent.getEdgeFunction());
					}
				}
			}
		}
		debugger().finishPhase1WithSeed(seed, solver);
	}

	private void phase2(AnalysisSolver<V> solver) {
		debugger().startPhase2WithSeed(seed, solver);
		enableIDEPhase();
		if (icfg().isExitStmt(seed.getStmt())) {
			solver.injectPhase1Seed(InternalAnalysisProblem.ZERO, seed.getStmt(), seed.getFact(), EdgeIdentity.<V>v());
		} else {
			for (Unit u : icfg().getSuccsOf(seed.getStmt())) {
				solver.injectPhase1Seed(InternalAnalysisProblem.ZERO, u, seed.getFact(), EdgeIdentity.<V>v());
			}
		}
		solver.runExecutorAndAwaitCompletion();
		HashMap<Unit, Set<AccessGraph>> map = new HashMap<Unit, Set<AccessGraph>>();
		for (Unit sp : icfg().getStartPointsOf(icfg().getMethodOf(seed.getStmt()))) {
			map.put(sp, Collections.singleton(InternalAnalysisProblem.ZERO));
		}
		solver.computeValues(map);
		analysisDefinition.onFinishWithSeed(seed,solver);
//		debugger().finishPhase2WithSeed(seed, solver);
	}

	public AliasResults aliasesFor(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1) {
		if (!analysisDefinition.enableAliasing())
			return new AliasResults();
		checkTimeout();
		if (boomerang == null)
			boomerang = new AliasFinder(analysisDefinition.boomerangOptions());
		if (!boomerangAccessGraph.isStatic()
				&& Scene.v().getPointsToAnalysis().reachingObjects(boomerangAccessGraph.getBase()).isEmpty())
			return new AliasResults();

		analysisDefinition.debugger().beforeAlias(boomerangAccessGraph, curr, d1);
		return queryToResult.getOrCreate(new BoomerangQuery(boomerangAccessGraph, curr, d1));
	}

	public void destroy() {
		poas = null;
		callSiteToFlows.clear();
		callSiteToFlows = null;
		callSiteToStrongUpdates = null;
		nullnessBranches = null;
		eventAtCallSite.clear();
		analysisDefinition = null;
	}

	public void checkTimeout() {
		if (startTime.elapsed(TimeUnit.SECONDS) > analysisDefinition.analysisBudgetInSeconds())
			throw new IDEALTimeoutException();
	}

	public IDebugger<V> debugger() {
		return analysisDefinition.debugger();
	}

	public boolean enableNullPointAlias() {
		return analysisDefinition.enableNullPointOfAlias();
	}

}
