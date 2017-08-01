package ideal.pointsofaliasing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.beust.jcommander.internal.Sets;

import boomerang.AliasResults;
import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.solver.PathEdge;
import ideal.PerSeedAnalysisContext;
import soot.Unit;

public class Call2ReturnEvent<V> extends Event<V> {

	private AccessGraph d2;
	private Unit callSite;
	private Unit returnSite;
	private AccessGraph d1;
	private EdgeFunction<V> func;

	public Call2ReturnEvent(AccessGraph d1, Unit callSite, AccessGraph d2, Unit returnSite, EdgeFunction<V> func) {
		this.d2 = d2;
		this.callSite = callSite;
		this.returnSite = returnSite;
		this.d1 = d1;
		this.func = func;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callSite == null) ? 0 : callSite.hashCode());
		result = prime * result + ((d1 == null) ? 0 : d1.hashCode());
		result = prime * result + ((d2 == null) ? 0 : d2.hashCode());
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
		Call2ReturnEvent other = (Call2ReturnEvent) obj;
		if (callSite == null) {
			if (other.callSite != null)
				return false;
		} else if (!callSite.equals(other.callSite))
			return false;
		if (d1 == null) {
			if (other.d1 != null)
				return false;
		} else if (!d1.equals(other.d1))
			return false;
		if (d2 == null) {
			if (other.d2 != null)
				return false;
		} else if (!d2.equals(other.d2))
			return false;
		if (returnSite == null) {
			if (other.returnSite != null)
				return false;
		} else if (!returnSite.equals(other.returnSite))
			return false;
		return true;
	}


	@Override
	public Collection<PathEdge<Unit, AccessGraph>> getPathEdges(PerSeedAnalysisContext<V> tsanalysis) {
		Set<PathEdge<Unit, AccessGraph>> res = new HashSet<>();
		for (AccessGraph mayAliasingAccessGraph : getIndirectFlowTargets(tsanalysis)) {
			res.add(new PathEdge<Unit, AccessGraph>(d1, returnSite,mayAliasingAccessGraph));
		}
		return res;
	}

	@Override
	public Collection<AccessGraph> getIndirectFlowTargets(PerSeedAnalysisContext<V> tsanalysis) {
		AliasResults results = tsanalysis.aliasesFor(d2, callSite, d1);
		checkMustAlias(results,tsanalysis);
		Collection<AccessGraph> mayAliasSet = results.mayAliasSet();
		tsanalysis.storeFlowAtPointOfAlias(this, mayAliasSet);
		return mayAliasSet;
	}

	private void checkMustAlias(AliasResults results,
			PerSeedAnalysisContext<V> context) {
		boolean isStrongUpdate = !results.queryTimedout() && results.keySet().size() == 1;
		if(isStrongUpdate){
			Set<AccessGraph> vars = Sets.newHashSet();
			 vars.addAll(results.mayAliasSet());
			 vars.remove(d2);
			context.storeStrongUpdateAtCallSite(callSite,vars);
		}
	}

	@Override
	public String toString() {
		return "[Call2Return (" + d1 +","+ callSite +","+ d2 +") succ: " +  returnSite + "]";
	}

	@Override
	Unit getCallsite() {
		return callSite;
	}

	public EdgeFunction<V> getEdgeFunction() {
		return func;
	}

}
