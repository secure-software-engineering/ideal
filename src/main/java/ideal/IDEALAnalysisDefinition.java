package ideal;

import java.util.Collection;

import boomerang.accessgraph.AccessGraph;
import ideal.debug.IDebugger;
import ideal.edgefunction.AnalysisEdgeFunctions;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public interface IDEALAnalysisDefinition<V> {

	/**
	 * This function generates the seed. Each (reachable) statement of the
	 * analyzed code is visited. To place a seed, a pair of access graph and an
	 * edge function must be specified. From this node the analysis starts its
	 * analysis.
	 * 
	 * @param method
	 * @param stmt
	 *            The statement over which is itearted over
	 * @param calledMethod
	 *            If stmt is a call site, this set contains the set of called
	 *            method for the call site.
	 * @return
	 */
	Collection<AccessGraph> generate(SootMethod method, Unit stmt, Collection<SootMethod> calledMethod);

	/**
	 * This function must generate and return the AnalysisEdgeFunctions that are
	 * used for the analysis. As for standard IDE in Heros, the edge functions
	 * for normal-, call-, return- and call-to-return flows have to be
	 * specified.
	 */
	AnalysisEdgeFunctions<V> edgeFunctions();

	ResultReporter<V> resultReporter();

	IInfoflowCFG icfg();

	IDebugger<V> debugger();

}
