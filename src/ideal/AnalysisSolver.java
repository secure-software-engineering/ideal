package ideal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Table.Cell;

import boomerang.context.Context;
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
    return new IContextRequester() {
      @Override
      public Collection<Context> getCallSiteOf(Context child) {
        if (!(child instanceof AnalysisSolver.AliasContext)) {
          throw new RuntimeException("Test ");
        }
        @SuppressWarnings("unchecked")
        AliasContext aliasContext = (AliasContext) child;
        Set<Context> res = new HashSet<>();
        if (aliasContext.fact.equals(zeroValue)) {
          for (Unit callsites : icfg.getCallersOf(icfg.getMethodOf(aliasContext.stmt))) {
            res.add(new AliasContext(zeroValue, callsites));
          }
          return res;
        }
        Collection<Unit> startPoints = icfg.getStartPointsOf(icfg.getMethodOf(aliasContext.stmt));

        for (Unit sp : startPoints) {

          Map<Unit, Set<Pair<WrappedAccessGraph, WrappedAccessGraph>>> inc = incoming(aliasContext.fact, sp);

          for (Entry<Unit, Set<Pair<WrappedAccessGraph, WrappedAccessGraph>>> e : inc.entrySet()) {
            for (Pair<WrappedAccessGraph, WrappedAccessGraph> p : e.getValue()) {
              res.add(new AliasContext(p.getO2(), e.getKey()));
            }
          }
        }
        return res;
      }

      @Override
      public Context initialContext(Unit stmt) {
        return new AliasContext(d1, stmt);
      }
    };
  }

  private class AliasContext implements Context {
    final Unit stmt;
    final WrappedAccessGraph fact;

    AliasContext(WrappedAccessGraph fact, Unit stmt) {
      this.fact = fact;
      this.stmt = stmt;
    }

    @Override
    public Unit getStmt() {
      return stmt;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((fact == null) ? 0 : fact.hashCode());
      result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      @SuppressWarnings("unchecked")
      AliasContext other = (AliasContext) obj;
      if (fact == null) {
        if (other.fact != null)
          return false;
      } else if (!fact.equals(other.fact))
        return false;
      if (stmt == null) {
        if (other.stmt != null)
          return false;
      } else if (!stmt.equals(other.stmt))
        return false;
      return true;
    }

  }

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
