package ideal.pointsofaliasing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Stopwatch;

import boomerang.AliasFinder;
import boomerang.accessgraph.AccessGraph;
import boomerang.cache.AliasResults;
import heros.solver.PathEdge;
import ideal.AnalysisContext;
import ideal.flowfunctions.WrappedAccessGraph;
import soot.Local;
import soot.Unit;

public class InstanceFieldWrite<V> extends PointOfAlias<V> {

  private Local base;

  public InstanceFieldWrite(WrappedAccessGraph d1, Unit stmt, Local base, WrappedAccessGraph d2, Unit succ) {
    super(d1, stmt, d2, succ);
    this.base = base;
  }

  @Override
  public Collection<PathEdge<Unit, WrappedAccessGraph>> getPathEdges(AnalysisContext<V> tsanalysis) {
    Set<PathEdge<Unit, WrappedAccessGraph>> res = new HashSet<>();

    AliasFinder aliasFinder = new AliasFinder(tsanalysis.icfg());
    aliasFinder.startQuery();
    WrappedAccessGraph accessGraph = new WrappedAccessGraph(new AccessGraph(base, base.getType()));
    AliasResults results = tsanalysis.aliasesFor(accessGraph, curr, d1);

    Set<WrappedAccessGraph> outFlows = new HashSet<>();
    for (AccessGraph mayAliasingWrappedAccessGraph : results.mayAliasSet()) {
    	WrappedAccessGraph withFields = new WrappedAccessGraph(mayAliasingWrappedAccessGraph.appendGraph(d2.getFieldGraph()),d2.hasEvent());
      outFlows.add(withFields);
      res.add(new PathEdge<Unit, WrappedAccessGraph>(d1, succ, withFields));
    }
    tsanalysis.storeComputeInstanceFieldWrite(this, outFlows);
    return res;
  }

  public Unit getCallSite() {
    return curr;
  }
}
