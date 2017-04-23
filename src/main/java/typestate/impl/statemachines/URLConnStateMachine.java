package typestate.impl.statemachines;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TypestateChangeFunction;
import typestate.TypestateDomainValue;
import typestate.finiteautomata.MatcherStateMachine;
import typestate.finiteautomata.MatcherTransition;
import typestate.finiteautomata.MatcherTransition.Parameter;
import typestate.finiteautomata.MatcherTransition.Type;
import typestate.finiteautomata.State;

public class URLConnStateMachine extends MatcherStateMachine implements TypestateChangeFunction {

	public static enum States implements State {
		NONE, INIT, CONNECTED, ERROR;

		@Override
		public boolean isErrorState() {
			return this == ERROR;
		}

		@Override
		public boolean isInitialState() {
			return this == INIT;
		}
	}

	public URLConnStateMachine() {
		addTransition(new MatcherTransition(States.CONNECTED, illegalOpertaion(), Parameter.This, States.ERROR,
				Type.OnReturn));
		addTransition(
				new MatcherTransition(States.ERROR, illegalOpertaion(), Parameter.This, States.ERROR, Type.OnReturn));
	}

	private Set<SootMethod> connect() {
		return selectMethodByName(getSubclassesOf("java.net.URLConnection"), "connect");
	}

	private Set<SootMethod> illegalOpertaion() {
		List<SootClass> subclasses = getSubclassesOf("java.net.URLConnection");
		return selectMethodByName(subclasses,
				"setDoInput|setDoOutput|setAllowUserInteraction|setUseCaches|setIfModifiedSince|setRequestProperty|addRequestProperty|getRequestProperty|getRequestProperties");
	}

	@Override
	public Collection<AccessGraph> generateSeed(SootMethod m, Unit unit, Collection<SootMethod> calledMethod) {
		return this.generateThisAtAnyCallSitesOf(unit, calledMethod, connect());
	}

	@Override
	public TypestateDomainValue getBottomElement() {
		return new TypestateDomainValue(States.CONNECTED);
	}
}
