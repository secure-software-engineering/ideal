package test;

import boomerang.accessgraph.AccessGraph;
import soot.Unit;
import typestate.TypestateDomainValue;

public abstract class ExpectedResults {
	final Unit unit;
	final AccessGraph accessGraph;
	final State state;
	protected boolean satisfied;

	enum State{
		ERROR, ACCEPTING
	}
	ExpectedResults(Unit unit, AccessGraph accessGraph, State state){
		this.unit = unit;
		this.accessGraph = accessGraph;
		this.state = state;
	}
	public boolean isSatisfied(){
		return satisfied;
	}
	public abstract void computedResults(TypestateDomainValue results);
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessGraph == null) ? 0 : accessGraph.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
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
		ExpectedResults other = (ExpectedResults) obj;
		if (accessGraph == null) {
			if (other.accessGraph != null)
				return false;
		} else if (!accessGraph.equals(other.accessGraph))
			return false;
		if (state != other.state)
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "[" + accessGraph +" @ " + unit + " in state " + state + "]";
	}
}
