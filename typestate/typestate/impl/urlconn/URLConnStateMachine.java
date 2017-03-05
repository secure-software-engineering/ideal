package typestate.impl.urlconn;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.solver.Pair;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import typestate.TransitionFunction;
import typestate.TypestateChangeFunction;
import typestate.TypestateDomainValue;
import typestate.finiteautomata.MatcherStateMachine;
import typestate.finiteautomata.MatcherTransition;
import typestate.finiteautomata.MatcherTransition.Parameter;
import typestate.finiteautomata.MatcherTransition.Type;
import typestate.finiteautomata.State;
import typestate.finiteautomata.Transition;

public class URLConnStateMachine extends MatcherStateMachine implements TypestateChangeFunction {

	private MatcherTransition initialTrans;
	private InfoflowCFG icfg;

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

	URLConnStateMachine(InfoflowCFG icfg) {
		this.icfg = icfg;
		initialTrans = new MatcherTransition(States.NONE, connect(), Parameter.This, States.CONNECTED, Type.OnReturn);
		// addTransition(initialTrans);
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
	public Collection<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> generate(SootMethod m, Unit unit,
			Collection<SootMethod> calledMethod) {
		if(!m.getDeclaringClass().isApplicationClass())
			return Collections.emptySet();
		return this.generateThisAtAnyCallSitesOf(unit, calledMethod, connect(), initialTrans);
//		for (Unit isRet : icfg.getSuccsOf(unit)) {
//			if (connect().contains(methodOf)) {
//				if (icfg.isExitStmt(isRet)) {
//					Local thisLocal = methodOf.getActiveBody().getThisLocal();
//					Set<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> out = new HashSet<>();
//					out.add(new Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>(
//							new AccessGraph(thisLocal, thisLocal.getType()), new TransitionFunction(initialTrans)));
//					return out;
//				}
//			}
//		}
//		return Collections.emptySet();
	}
}
