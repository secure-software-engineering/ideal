package ideal;

import boomerang.accessgraph.AccessGraph;
import heros.solver.PathEdge;
import soot.Unit;

public interface ResultReporter<V> {
	public void onSeedFinished(PathEdge<Unit, AccessGraph> seed, AnalysisSolver<V> solver);
}
