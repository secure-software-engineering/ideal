package test;

import boomerang.accessgraph.AccessGraph;
import soot.Unit;
import typestate.TypestateDomainValue;

public interface ComparableResult<State> {

	public AccessGraph getAccessGraph();
	public Unit getStmt();
	public void computedResults(TypestateDomainValue<State> val);
}
