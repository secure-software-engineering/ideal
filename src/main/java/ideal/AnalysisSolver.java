package ideal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table.Cell;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import boomerang.context.IContextRequester;
import heros.EdgeFunction;
import heros.InterproceduralCFG;
import heros.solver.IDESolver;
import heros.solver.Pair;
import heros.solver.PathEdge;
import soot.SootMethod;
import soot.Unit;

public class AnalysisSolver<V>
		extends IDESolver<Unit, AccessGraph, SootMethod, V, InterproceduralCFG<Unit, SootMethod>> {

	private PerSeedAnalysisContext<V> context;

	public AnalysisSolver(IDEALAnalysisDefinition<V> analysisDefinition, PerSeedAnalysisContext<V> context) {
		super(new InternalAnalysisProblem<V>(analysisDefinition, context));
		this.context = context;
	}

	/**
	 * Starts the IFDS phase with the given path edge <d1>-><curr,d2>
	 * 
	 * @param d1
	 * @param curr
	 * @param d2
	 */
	public void injectPhase1Seed(AccessGraph d1, Unit curr, AccessGraph d2, EdgeFunction<V> func) {
		super.propagate(d1, curr, d2, func, null, true);
		runExecutorAndAwaitCompletion();
	}

	@Override
	protected void scheduleEdgeProcessing(PathEdge<Unit, AccessGraph> edge) {
		worklist.add(new PathEdgeProcessingTask(edge));
		propagationCount++;
	}

	@Override
	protected void scheduleValueProcessing(ValuePropagationTask vpt) {
		context.checkTimeout();
		super.scheduleValueProcessing(vpt);
	}

	@Override
	protected void scheduleValueComputationTask(ValueComputationTask task) {
		context.checkTimeout();
		super.scheduleValueComputationTask(task);
	}

	public IContextRequester getContextRequestorFor(final AccessGraph d1, final Unit stmt) {
		return new ContextRequester(d1, stmt);
	}

	private class ContextRequester implements IContextRequester {
		Multimap<SootMethod, AccessGraph> methodToStartFact = HashMultimap.create();
		private AccessGraph d1;

		public ContextRequester(AccessGraph d1, Unit stmt) {
			this.d1 = d1;
			methodToStartFact.put(icfg.getMethodOf(stmt), d1);
		}

		@Override
		public boolean continueAtCallSite(Unit callSite, SootMethod callee) {
			if (d1.equals(zeroValue)) {
				return true;
			}
			Collection<Unit> startPoints = icfg.getStartPointsOf(callee);

			for (Unit sp : startPoints) {
				for (AccessGraph g : new HashSet<>(methodToStartFact.get(callee))) {
					Map<Unit, Set<Pair<AccessGraph, AccessGraph>>> inc = incoming(g, sp);
					for (Set<Pair<AccessGraph, AccessGraph>> in : inc.values()) {
						for (Pair<AccessGraph, AccessGraph> e : in) {
							methodToStartFact.put(icfg.getMethodOf(callSite), e.getO2());
						}
					}
					if (inc.containsKey(callSite))
						return true;
				}
			}
			return false;
		}

		@Override
		public boolean isEntryPointMethod(SootMethod method) {
			return false;
		}
	}

	public void destroy() {
		jumpFn.clear();
		incoming.clear();
		endSummary.clear();
		incoming.clear();
	}

	public Set<Cell<AccessGraph, AccessGraph, EdgeFunction<V>>> getPathEdgesAt(Unit statement) {
		return jumpFn.lookupByTarget(statement);
	}

	public Multimap<Unit, AccessGraph> getEndPathOfPropagation() {
		Multimap<Unit,AccessGraph> endPathOfPropagation = HashMultimap.create();
		for (SootMethod method : getVisitedMethods()) {
			if (!method.hasActiveBody())
				continue;

			Collection<Unit> endPointsOf = getEndPointsOf(method);

			for (Unit eP : endPointsOf) {
				Set<AccessGraph> parameterLocalsAtEndPoint = new HashSet<>();
				Set<AccessGraph> nonParameterLocalsAtEndPoint = new HashSet<>();
				for (Cell<AccessGraph, AccessGraph, EdgeFunction<V>> cell : getPathEdgesAt(eP)) {
					if (!cell.getRowKey().equals(InternalAnalysisProblem.ZERO)) {
						continue;
					}
					if (cell.getColumnKey().isStatic() || BoomerangContext.isParameterOrThisValue(method, cell.getColumnKey().getBase()) || BoomerangContext.isReturnValue(method,cell.getColumnKey().getBase())) {
						parameterLocalsAtEndPoint.add(cell.getColumnKey());
					} else {
						nonParameterLocalsAtEndPoint.add(cell.getColumnKey());
					}
				}
				if (parameterLocalsAtEndPoint.isEmpty()) {
					endPathOfPropagation.putAll(eP, nonParameterLocalsAtEndPoint);
				}
			}
		}
		return endPathOfPropagation;
	}

	private Collection<Unit> getEndPointsOf(SootMethod method) {
		Set<Unit> endPoints = Sets.newHashSet();
		for (Unit u : method.getActiveBody().getUnits())
			if (icfg.isExitStmt(u))
				endPoints.add(u);
		return endPoints;
	}
	
	
	public Multimap<Unit, AccessGraph> getResultsAtStatement() {
		Multimap<Unit,AccessGraph> results = HashMultimap.create();
		for (SootMethod method : getVisitedMethods()) {
			if (!method.hasActiveBody())
				continue;


			for (Unit u : method.getActiveBody().getUnits()) {
				for (Cell<AccessGraph, AccessGraph, EdgeFunction<V>> cell : getPathEdgesAt(u)) {
					results.put(u, cell.getColumnKey());
				}
			}
		}
		return results;
	}
}
