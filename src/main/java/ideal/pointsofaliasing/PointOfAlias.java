package ideal.pointsofaliasing;

import java.util.Collection;

import boomerang.accessgraph.AccessGraph;
import heros.solver.PathEdge;
import ideal.AnalysisContext;
import soot.Unit;

public interface PointOfAlias<V> {


	  /**
	   * Generates the path edges the given POA should generate.
	   */
	  public Collection<PathEdge<Unit, AccessGraph>> getPathEdges(
	      AnalysisContext<V> tsanalysis);

	  /**
	   * Generates the path edges the given POA should generate.
	   */
	  public Collection<AccessGraph> getIndirectFlowTargets(
	      AnalysisContext<V> tsanalysis);

}
