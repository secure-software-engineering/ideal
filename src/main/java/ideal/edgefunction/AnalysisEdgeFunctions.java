package ideal.edgefunction;

import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunctions;
import soot.SootMethod;
import soot.Unit;

/**
 * This class just lifts the regular JoinLattice from the Heros solver to the EdgeFunction.
 *
 */
public interface AnalysisEdgeFunctions<V> extends EdgeFunctions<Unit, AccessGraph, SootMethod, V> {
  V bottom();

  V top();

  V join(V left, V right);
}
