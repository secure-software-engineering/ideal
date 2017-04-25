package test;

import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.accessgraph.AccessGraph;
import heros.solver.PathEdge;
import ideal.AnalysisSolver;
import ideal.FactAtStatement;
import ideal.ResultReporter;
import soot.Unit;
import typestate.TypestateDomainValue;

public class TestingResultReporter<State> implements ResultReporter<TypestateDomainValue<State>>{
	private Multimap<Unit, IExpectedResults<State>> stmtToResults = HashMultimap.create();
	public TestingResultReporter(Set<IExpectedResults<State>> expectedResults) {
		for(IExpectedResults<State> e : expectedResults){
			stmtToResults.put(e.getStmt(), e);
		}
	}

	@Override
	public void onSeedFinished(FactAtStatement seed, AnalysisSolver<TypestateDomainValue<State>> solver) {
		for(Entry<Unit, IExpectedResults<State>> e : stmtToResults.entries()){
			TypestateDomainValue<State> resultAt = solver.resultAt(e.getKey(), e.getValue().getAccessGraph());
			if(resultAt != null)
				e.getValue().computedResults(resultAt);
		}
	}

}
