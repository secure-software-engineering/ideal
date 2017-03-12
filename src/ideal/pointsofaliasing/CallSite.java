package ideal.pointsofaliasing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import boomerang.AliasResults;
import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import heros.solver.PathEdge;
import ideal.AnalysisContext;
import soot.Unit;

public class CallSite<V> extends PointOfAlias<V> {

	private AccessGraph callerCallSiteFact;

	public CallSite(AccessGraph callerD1, Unit stmt, AccessGraph callerCallSiteFact,
			AccessGraph callerD2, Unit returnSite) {
		super(callerD1, stmt, callerD2, returnSite);
		this.callerCallSiteFact = callerCallSiteFact;
	}

	@Override
	public Collection<PathEdge<Unit, AccessGraph>> getPathEdges(AnalysisContext<V> tsanalysis) {
		Collection<PathEdge<Unit, AccessGraph>> res = new HashSet<>();

		if (tsanalysis.hasEvent(d2)){
			res = balancedReturn(tsanalysis);
		}
		if (d2.getFieldCount() > 0 && !callerCallSiteFact.equals(d2)){
			res.addAll(unbalancedReturn(tsanalysis));
		}
		return res;
	}


	private Collection<PathEdge<Unit, AccessGraph>> balancedReturn(AnalysisContext<V> tsanalysis) {
		Set<PathEdge<Unit, AccessGraph>> res = new HashSet<>();
		AliasResults results = tsanalysis.aliasesFor(d2, curr, d1);
		for (AccessGraph mayAliasingAccessGraph : results.mayAliasSet()) {
			res.add(new PathEdge<Unit, AccessGraph>(d1, succ,mayAliasingAccessGraph));
			tsanalysis.flowFromTo(d2, mayAliasingAccessGraph);
		}
		checkMustAlias(results, res, tsanalysis);
		return res;
	}

	private Collection<PathEdge<Unit, AccessGraph>> unbalancedReturn(AnalysisContext<V> tsanalysis) {
		Collection<WrappedSootField> lastFields = d2.getLastField();
		Set<AccessGraph> popLastField = d2.popLastField();
		Set<PathEdge<Unit, AccessGraph>> res = new HashSet<>();
		for (AccessGraph withoutLast : popLastField) {
			AliasResults results = tsanalysis.aliasesFor(withoutLast, curr, d1);
			for (AccessGraph mayAliasingAccessGraph : results.mayAliasSet()) {
				for(WrappedSootField lastField : lastFields){
					AccessGraph g = mayAliasingAccessGraph.appendFields(new WrappedSootField[] { lastField });
					res.add(new PathEdge<Unit, AccessGraph>(d1, succ,g));
					tsanalysis.debugger.indirectFlowAtCall(withoutLast, curr, g);
				}
			}
		}
		tsanalysis.storeComputedCallSiteFlow(this, res, false);
		return res;
	}

	private void checkMustAlias(AliasResults results, Set<PathEdge<Unit, AccessGraph>> res,
			AnalysisContext<V> context) {
		boolean isStrongUpdate = results.keySet().size() == 1;
		context.storeComputedCallSiteFlow(this, res, isStrongUpdate);
	}

	public Unit getCallSite() {
		return curr;
	}

	@Override
	public String toString() {
		return "[CallSite " + super.toString() + " returns to: " + succ + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((callerCallSiteFact == null) ? 0 : callerCallSiteFact.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CallSite other = (CallSite) obj;
		if (callerCallSiteFact == null) {
			if (other.callerCallSiteFact != null)
				return false;
		} else if (!callerCallSiteFact.equals(other.callerCallSiteFact))
			return false;
		return true;
	}


}
