package test;

import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import boomerang.accessgraph.AccessGraph;
import heros.solver.PathEdge;
import ideal.AnalysisSolver;
import ideal.ResultReporter;
import soot.Unit;
import typestate.TypestateDomainValue;

public class TestingResultReporter implements ResultReporter<TypestateDomainValue>{
	private Multimap<Unit, ExpectedResults> stmtToResults = HashMultimap.create();
	public TestingResultReporter(Set<ExpectedResults> expectedResults) {
		for(ExpectedResults e : expectedResults){
			stmtToResults.put(e.unit, e);
		}
	}

	@Override
	public void onSeedFinished(PathEdge<Unit, AccessGraph> seed, AnalysisSolver<TypestateDomainValue> solver) {
		for(Entry<Unit, ExpectedResults> e : stmtToResults.entries()){
			TypestateDomainValue resultAt = solver.resultAt(e.getKey(), e.getValue().accessGraph);
			if(resultAt != null)
				e.getValue().computedResults(resultAt);
		}
	}

}
