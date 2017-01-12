package ideal.pointsofaliasing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import boomerang.cache.AliasResults;
import heros.solver.PathEdge;
import ideal.AnalysisContext;
import ideal.flowfunctions.WrappedAccessGraph;
import soot.Unit;

public class CallSite<V> extends PointOfAlias<V> {

	private WrappedAccessGraph callerCallSiteFact;

	public CallSite(WrappedAccessGraph callerD1, Unit stmt, WrappedAccessGraph callerCallSiteFact,
			WrappedAccessGraph callerD2, Unit returnSite) {
		super(callerD1, stmt, callerD2, returnSite);
		this.callerCallSiteFact = callerCallSiteFact;
	}

	@Override
	public Collection<PathEdge<Unit, WrappedAccessGraph>> getPathEdges(AnalysisContext<V> tsanalysis) {
		Collection<PathEdge<Unit, WrappedAccessGraph>> res = new HashSet<>();
		if (d2.hasEvent())
			res = balancedReturn(tsanalysis);
		if (d2.getFieldCount() > 0 && !callerCallSiteFact.equals(d2))
			res.addAll(unbalancedReturn(tsanalysis));
		return res;
	}

	private Collection<PathEdge<Unit, WrappedAccessGraph>> balancedReturn(AnalysisContext<V> tsanalysis) {
		Set<PathEdge<Unit, WrappedAccessGraph>> res = new HashSet<>();
		AliasResults results = tsanalysis.aliasesFor(d2, curr, d1);
		for (AccessGraph mayAliasingWrappedAccessGraph : results.mayAliasSet()) {
			res.add(new PathEdge<Unit, WrappedAccessGraph>(d1, succ,
					new WrappedAccessGraph(mayAliasingWrappedAccessGraph, d2.hasEvent())));
		}
		checkMustAlias(results, res, tsanalysis);
		return res;
	}

	private Collection<PathEdge<Unit, WrappedAccessGraph>> unbalancedReturn(AnalysisContext<V> tsanalysis) {
		WrappedSootField lastField = d2.getLastField();
		Set<WrappedAccessGraph> popLastField = d2.popLastField();
		Set<PathEdge<Unit, WrappedAccessGraph>> res = new HashSet<>();
		for (WrappedAccessGraph withoutLast : popLastField) {
			AliasResults results = tsanalysis.aliasesFor(withoutLast, curr, d1);

			for (AccessGraph mayAliasingWrappedAccessGraph : results.mayAliasSet()) {
				AccessGraph g = mayAliasingWrappedAccessGraph.appendFields(new WrappedSootField[] { lastField });
				res.add(new PathEdge<Unit, WrappedAccessGraph>(d1, succ, new WrappedAccessGraph(g, d2.hasEvent())));
			}
		}
		tsanalysis.storeComputedCallSiteFlow(this, res, false);
		return res;
	}

	private void checkMustAlias(AliasResults results, Set<PathEdge<Unit, WrappedAccessGraph>> res,
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

	public CallSite<V> ignoreEvent() {
		return new CallSite<V>(d1.deriveWithoutEvent(), curr, callerCallSiteFact.deriveWithoutEvent(),
				d2.deriveWithoutEvent(), succ);
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

	public boolean triggersQuery() {
		return d2.hasEvent() || (d2.getFieldCount() > 0 && !callerCallSiteFact.equals(d2));
	}

}
