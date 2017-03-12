package ideal.pointsofaliasing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import boomerang.AliasFinder;
import boomerang.AliasResults;
import boomerang.accessgraph.AccessGraph;
import heros.solver.PathEdge;
import ideal.AnalysisContext;
import soot.Local;
import soot.Unit;

public class InstanceFieldWrite<V> extends PointOfAlias<V> {

  private Local base;

  public InstanceFieldWrite(AccessGraph d1, Unit stmt, Local base, AccessGraph d2, Unit succ) {
    super(d1, stmt, d2, succ);
    this.base = base;
  }

  @Override
  public Collection<PathEdge<Unit, AccessGraph>> getPathEdges(AnalysisContext<V> tsanalysis) {
    Set<PathEdge<Unit, AccessGraph>> res = new HashSet<>();

    AccessGraph accessGraph = new AccessGraph(base, base.getType());
    AliasResults results = tsanalysis.aliasesFor(accessGraph, curr, d1);

    Set<AccessGraph> outFlows = new HashSet<>();
    for (AccessGraph mayAliasingAccessGraph : results.mayAliasSet()) {
    	AccessGraph withFields = mayAliasingAccessGraph.appendGraph(d2.getFieldGraph());
      outFlows.add(withFields);
      tsanalysis.debugger.indirectFlowAtWrite(d2,curr,withFields);
      res.add(new PathEdge<Unit, AccessGraph>(d1, succ, withFields));
    }
    tsanalysis.storeComputeInstanceFieldWrite(this, outFlows);
    return res;
  }

  public Unit getCallSite() {
    return curr;
  }
}
