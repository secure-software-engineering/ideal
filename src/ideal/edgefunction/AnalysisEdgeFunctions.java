package ideal.edgefunction;

import heros.EdgeFunctions;
import ideal.flowfunctions.WrappedAccessGraph;
import soot.SootMethod;
import soot.Unit;

/**
 * This class just lifts the regular JoinLattice from the Heros solver to the EdgeFunction.
 *
 */
public interface AnalysisEdgeFunctions<V> extends EdgeFunctions<Unit, WrappedAccessGraph, SootMethod, V> {
  V bottom();

  V top();

  V join(V left, V right);
}
