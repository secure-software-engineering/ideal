package ideal;

import java.util.Collection;
import java.util.HashMap;
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
import heros.edgefunc.EdgeIdentity;
import heros.solver.IDESolver;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.edgefunction.AnalysisEdgeFunctions;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class AnalysisSolver<V>
    extends IDESolver<Unit, AccessGraph, SootMethod, V, InterproceduralCFG<Unit, SootMethod>> {

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
  public void injectPhase1Seed(AccessGraph d1, Unit curr, AccessGraph d2) {
    super.propagate(d1, curr, d2, EdgeIdentity.<V>v(),null, true);
    runExecutorAndAwaitCompletion();
  }

  public IInfoflowCFG icfg() {
    return (IInfoflowCFG) icfg;
  }

  
  /**
   * Starts the IDE phase with the given path edge <d1>-><curr,d2> and the initial edge function
   */
  public void injectPhase2Seed(AccessGraph d1, Unit curr, AccessGraph d2, AnalysisContext<V> context) {
	    
    super.propagate(d1, curr, d2, EdgeIdentity.<V>v(), null, true);
    runExecutorAndAwaitCompletion();
    for(Unit sp: icfg.getStartPointsOf(icfg.getMethodOf(curr)))
    	this.setVal(sp, d1, bottom());
    this.setVal(curr, d2, bottom());
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


  public IContextRequester getContextRequestorFor(final AccessGraph d1, final Unit stmt) {
    return new ContextRequester(d1,stmt);
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
			if (d1.equals(zeroValue)){
				return true;
			}
			Collection<Unit> startPoints = icfg.getStartPointsOf(callee);

			for (Unit sp : startPoints) {
				for (AccessGraph g : new HashSet<>(methodToStartFact.get(callee))) {
					Map<Unit, Set<Pair<AccessGraph, AccessGraph>>> inc = incoming(g, sp);
					for(Set<Pair<AccessGraph, AccessGraph>> in : inc.values()){
						for(Pair<AccessGraph, AccessGraph> e : in){
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
  public void computeValues(PathEdge<Unit, AccessGraph> seed) {
    HashMap<Unit, Set<AccessGraph>> map = new HashMap<Unit, Set<AccessGraph>>();
    HashSet<AccessGraph> hashSet = new HashSet<>();
    hashSet.add(seed.factAtSource());
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
  
  public Set<Cell<AccessGraph, AccessGraph, EdgeFunction<V>>> getPathEdgesAt(Unit statement) {
    return jumpFn.lookupByTarget(statement);
  }
}
