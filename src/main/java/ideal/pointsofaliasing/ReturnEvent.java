package ideal.pointsofaliasing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import boomerang.AliasResults;
import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.solver.PathEdge;
import ideal.AnalysisContext;
import soot.Unit;

public class ReturnEvent<V> extends Event<V> {

	private boolean isStrongUpdate;
	private Unit exitStmt;
	private AccessGraph d2;
	private Unit callSite;
	private AccessGraph d3;
	private Unit returnSite;
	private AccessGraph d1;
	private EdgeFunction<V> func;

	public ReturnEvent(Unit exitStmt, AccessGraph d2, Unit callSite, AccessGraph d3, Unit returnSite, AccessGraph d1, EdgeFunction<V> func) {
		this.exitStmt = exitStmt;
		this.d2 = d2;
		this.callSite = callSite;
		this.d3 = d3;
		this.returnSite = returnSite;
		this.d1 = d1;
		this.func = func;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callSite == null) ? 0 : callSite.hashCode());
		result = prime * result + ((d2 == null) ? 0 : d2.hashCode());
		result = prime * result + ((d3 == null) ? 0 : d3.hashCode());
		result = prime * result + ((exitStmt == null) ? 0 : exitStmt.hashCode());
		result = prime * result + ((returnSite == null) ? 0 : returnSite.hashCode());
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
		ReturnEvent other = (ReturnEvent) obj;
		if (callSite == null) {
			if (other.callSite != null)
				return false;
		} else if (!callSite.equals(other.callSite))
			return false;
		if (d2 == null) {
			if (other.d2 != null)
				return false;
		} else if (!d2.equals(other.d2))
			return false;
		if (d3 == null) {
			if (other.d3 != null)
				return false;
		} else if (!d3.equals(other.d3))
			return false;
		if (exitStmt == null) {
			if (other.exitStmt != null)
				return false;
		} else if (!exitStmt.equals(other.exitStmt))
			return false;
		if (returnSite == null) {
			if (other.returnSite != null)
				return false;
		} else if (!returnSite.equals(other.returnSite))
			return false;
		return true;
	}

	@Override
	public Collection<PathEdge<Unit, AccessGraph>> getPathEdges(AnalysisContext<V> tsanalysis) {
		Set<PathEdge<Unit, AccessGraph>> res = new HashSet<>();
		for (AccessGraph mayAliasingAccessGraph : getIndirectFlowTargets(tsanalysis)) {
			res.add(new PathEdge<Unit, AccessGraph>(d1, returnSite,mayAliasingAccessGraph));
		}
		return res;
	}

	@Override
	public Collection<AccessGraph> getIndirectFlowTargets(AnalysisContext<V> tsanalysis) {
		AliasResults results = tsanalysis.aliasesFor(d3, callSite, d1);
		checkMustAlias(results,tsanalysis);
		Collection<AccessGraph> mayAliasSet = results.mayAliasSet();
		System.out.println("SOLVED EVETN " + d3 + " "+ callSite + " ma" + mayAliasSet);
		tsanalysis.storeFlowAtPointOfAlias(this, mayAliasSet);
		return mayAliasSet;
	}

	private void checkMustAlias(AliasResults results,
			AnalysisContext<V> context) {
		boolean isStrongUpdate = !results.queryTimedout() && results.keySet().size() == 1;
		if(isStrongUpdate)
			context.storeStrongUpdateAtCallSite(callSite, results.mayAliasSet());
	}

	@Override
	public String toString() {
		return "[Event " + super.toString() + " returns to: " + returnSite + "]";
	}

	@Override
	Unit getCallsite() {
		return callSite;
	}

	public EdgeFunction<V> getEdgeFunction() {
		return func;
	}

}
