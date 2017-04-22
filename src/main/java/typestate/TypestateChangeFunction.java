package typestate;

import java.util.Collection;
import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import soot.SootMethod;
import soot.Unit;
import typestate.finiteautomata.Transition;

public interface TypestateChangeFunction {
	Set<? extends Transition> getReturnTransitionsFor(AccessGraph callerD1, Unit callSite, SootMethod calleeMethod,
			Unit exitStmt, AccessGraph exitNode, Unit returnSite, AccessGraph retNode);

	Collection<AccessGraph> generate(SootMethod method, Unit stmt, Collection<SootMethod> optional);

	Set<? extends Transition> getCallTransitionsFor(AccessGraph callerD1, Unit callSite, SootMethod calleeMethod,
			AccessGraph srcNode, AccessGraph destNode);

	Set<? extends Transition> getCallToReturnTransitionsFor(AccessGraph d1, Unit callSite, AccessGraph d2,
			Unit returnSite, AccessGraph d3);

	TypestateDomainValue getBottomElement();

}
