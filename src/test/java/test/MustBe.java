package test;

import boomerang.accessgraph.AccessGraph;
import soot.Unit;
import test.ExpectedResults.State;
import typestate.TypestateDomainValue;

public class MustBe extends ExpectedResults {

	MustBe(Unit unit, AccessGraph accessGraph, State state) {
		super(unit, accessGraph, state);
	}

	public String toString(){
		return "MustBe " + super.toString();
	}

	@Override
	public void computedResults(TypestateDomainValue results) {
		System.out.println(this + " " + results);
		if(results.getStates().size() > 1)
			return;
		for(typestate.finiteautomata.State s : results.getStates()){
			if(state == State.ACCEPTING){
				satisfied |= !s.isErrorState();
			} else if(state == State.ERROR){
				satisfied |= s.isErrorState();
			}
		}
	}
}	
