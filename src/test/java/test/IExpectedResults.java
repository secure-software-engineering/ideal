package test;

import boomerang.accessgraph.AccessGraph;
import soot.Unit;
import typestate.TypestateDomainValue;

public interface IExpectedResults<State> {
	public void computedResults(TypestateDomainValue<State> val);

	public Unit getStmt();
	public boolean isSatisfied();
	public boolean isImprecise();
	public AccessGraph getAccessGraph();
}
