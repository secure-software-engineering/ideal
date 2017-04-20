package test;

import boomerang.accessgraph.AccessGraph;
import soot.Unit;
import typestate.TypestateDomainValue;

public class MayBe extends ExpectedResults {

	MayBe(Unit unit, AccessGraph accessGraph, State state) {
		super(unit, accessGraph, state);
	}
	public String toString(){
		return "Maybe " + super.toString();
	}
	@Override
	public void computedResults(TypestateDomainValue results) {
		for(typestate.finiteautomata.State s : results.getStates()){
			if(state == State.ACCEPTING){
				satisfied |= !s.isErrorState();
			} else if(state == State.ERROR){
				satisfied |= s.isErrorState();
			}
		}
	}
}	
