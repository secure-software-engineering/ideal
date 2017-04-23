package ideal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table.Cell;

import boomerang.accessgraph.AccessGraph;
import boomerang.context.IContextRequester;
import heros.EdgeFunction;
import heros.InterproceduralCFG;
import heros.solver.IDESolver;
import heros.solver.Pair;
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
	public void runExecutorAndAwaitCompletion() {
		while (!worklist.isEmpty()) {
			Runnable pop = worklist.pop();
			if (propagationCount % 1000 == 0) {
				context.checkTimeout();
			}
			pop.run();
		}
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
}
