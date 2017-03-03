package ideal.pointsofaliasing;

import java.util.Collection;
import java.util.Collections;

import boomerang.AliasResults;
import heros.solver.PathEdge;
import ideal.AnalysisContext;
import ideal.flowfunctions.WrappedAccessGraph;
import soot.Unit;

public class NullnessCheck<V> extends PointOfAlias<V> {
  public NullnessCheck(WrappedAccessGraph callerD1, Unit stmt, WrappedAccessGraph callerD2, Unit returnSite) {
    super(callerD1, stmt, callerD2, returnSite);
  }

  @Override
  public Collection<PathEdge<Unit, WrappedAccessGraph>> getPathEdges(
AnalysisContext<V> tsanalysis) {
    AliasResults results = tsanalysis.aliasesFor(d2, curr, d1);
    if (results.withoutNullAllocationSites().keySet().size() <= 1) {

      tsanalysis.storeComputedNullnessFlow(this, results.withoutNullAllocationSites());
    }
    return Collections.emptySet();
  }

  public Unit getCurr() {
    return curr;
  }

  public Unit getSucc() {
    return succ;
  }

  @Override
  public String toString() {
    return "[Nullness " + super.toString() + " ifs to: " + succ + "]";
  }

}
