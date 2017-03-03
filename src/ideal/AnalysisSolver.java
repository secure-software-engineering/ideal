package ideal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table.Cell;

import boomerang.context.IContextRequester;
import heros.EdgeFunction;
import heros.InterproceduralCFG;
import heros.solver.IDESolver;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.edgefunction.AnalysisEdgeFunctions;
import ideal.flowfunctions.WrappedAccessGraph;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class AnalysisSolver<V>
    extends IDESolver<Unit, WrappedAccessGraph, SootMethod, V, InterproceduralCFG<Unit, SootMethod>> {

  private AnalysisEdgeFunctions<V> edgeFn2;

  public AnalysisSolver(InterproceduralCFG<Unit, SootMethod> icfg,
      AnalysisContext<V> context, AnalysisEdgeFunctions<V> edgeFn) {
    super(new InternalAnalysisProblem<V>(icfg, context, edgeFn));
    edgeFn2 = edgeFn;
  }

  /**
   * Starts the IFDS phase with the given path edge <d1>-><curr,d2>
   * @param d1
   * @param curr
   * @param d2
   */
  public void injectPhase1Seed(WrappedAccessGraph d1, Unit curr, WrappedAccessGraph d2) {
    super.propagate(d1, curr, d2, allBottom, null, true);
    runExecutorAndAwaitCompletion();
  }

  public IInfoflowCFG icfg() {
    return (IInfoflowCFG) icfg;
  }

  
  /**
   * Starts the IDE phase with the given path edge <d1>-><curr,d2> and the initial edge function
   */
  public void injectPhase2Seed(WrappedAccessGraph d1, Unit curr, WrappedAccessGraph d2,
      EdgeFunction<V> initialFunction, AnalysisContext<V> context) {
    super.propagate(d1, curr, d2, initialFunction, null, true);
    runExecutorAndAwaitCompletion();
  }

  @Override
  public void runExecutorAndAwaitCompletion() {
    while (!worklist.isEmpty()) {
      Runnable pop = worklist.pop();
      if (propagationCount % 1000 == 0) {
        Analysis.checkTimeout();
      }
      pop.run();
    }
  }
  

  @Override
  protected void scheduleValueProcessing(ValuePropagationTask vpt) {
    Analysis.checkTimeout();
    super.scheduleValueProcessing(vpt);
  }

  @Override
  protected void scheduleValueComputationTask(ValueComputationTask task) {
      Analysis.checkTimeout();
    super.scheduleValueComputationTask(task);
  }


  public IContextRequester getContextRequestorFor(final WrappedAccessGraph d1, final Unit stmt) {
    return new ContextRequester(d1,stmt);
  }

  
	private class ContextRequester implements IContextRequester {
		Multimap<SootMethod, WrappedAccessGraph> methodToStartFact = HashMultimap.create();
		private WrappedAccessGraph d1;

		public ContextRequester(WrappedAccessGraph d1, Unit stmt) {
			this.d1 = d1;
			methodToStartFact.put(icfg.getMethodOf(stmt), d1);
		}

		@Override
		public boolean continueAtCallSite(Unit callSite, SootMethod callee) {
			if (d1.equals(zeroValue))
				return true;
			Collection<Unit> startPoints = icfg.getStartPointsOf(callee);

			for (Unit sp : startPoints) {
				for (WrappedAccessGraph g : methodToStartFact.get(callee)) {
					Map<Unit, Set<Pair<WrappedAccessGraph, WrappedAccessGraph>>> inc = incoming(g, sp);
					for(Set<Pair<WrappedAccessGraph, WrappedAccessGraph>> in : inc.values()){
						for(Pair<WrappedAccessGraph, WrappedAccessGraph> e : in){
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
	}}
  public void computeValues(PathEdge<Unit, WrappedAccessGraph> seed) {
    HashMap<Unit, Set<WrappedAccessGraph>> map = new HashMap<Unit, Set<WrappedAccessGraph>>();
    HashSet<WrappedAccessGraph> hashSet = new HashSet<>();
    hashSet.add(seed.factAtTarget());
    map.put(seed.getTarget(), hashSet);
    super.computeValues(map);
  }

  public void destroy() {
    jumpFn.clear();
    incoming.clear();
    endSummary.clear();
    incoming.clear();
  }

  public V bottom(){
    return edgeFn2.bottom();
  }
  
  public Set<Cell<WrappedAccessGraph, WrappedAccessGraph, EdgeFunction<V>>> getPathEdgesAt(Unit statement) {
    return jumpFn.lookupByTarget(statement);
  }
}
