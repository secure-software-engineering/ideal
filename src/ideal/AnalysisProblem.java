package ideal;

import java.util.Collection;

import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.edgefunction.AnalysisEdgeFunctions;
import ideal.flowfunctions.WrappedAccessGraph;
import soot.SootMethod;
import soot.Unit;

public interface AnalysisProblem<V> {

	/**
	 * This function generates the seed. Each (reachable) statement of the analyzed code is visited. 
	 * To place a seed, a pair of access graph and an edge function must be specified. From this node
	 * the analysis starts its analysis.
	 * @param stmt The statement over which is itearted over
	 * @param calledMethod If stmt is a call site, this set contains the set of called method for the call site.
	 * @return
	 */
  Collection<Pair<AccessGraph, EdgeFunction<V>>> generate(Unit stmt,
      Collection<SootMethod> calledMethod);

  /**
   * This function must generate and return the AnalysisEdgeFunctions that are used for the analysis.
   * As for standard IDE in Heros, the edge functions for normal-, call-, return- and call-to-return flows
   * have to be specified.
   */
  AnalysisEdgeFunctions<V> edgeFunctions();

  /**
   * This is invoked as a callback when the analysis is finised. It retrieves the seed and the solver as input.
   * The client may wish to use that information.
   */
  void onAnalysisFinished(PathEdge<Unit, WrappedAccessGraph> seed, AnalysisSolver<V> solver);

  /**
   * Just used to report errors in the typestate analysis.
   * @return
   */
  boolean isInErrorState();

}
