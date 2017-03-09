package typestate.impl.inputstream.allocsite;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.solver.Pair;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.options.Options;
import typestate.TransitionFunction;
import typestate.TypestateChangeFunction;
import typestate.TypestateDomainValue;
import typestate.finiteautomata.MatcherStateMachine;
import typestate.finiteautomata.MatcherTransition;
import typestate.finiteautomata.MatcherTransition.Parameter;
import typestate.finiteautomata.MatcherTransition.Type;
import typestate.finiteautomata.State;
import typestate.finiteautomata.Transition;

public class InputStreamStateMachine extends MatcherStateMachine implements TypestateChangeFunction {

	private MatcherTransition initialTrans;
	private InfoflowCFG icfg;

	public static enum States implements State {
		NONE, OPEN, CLOSED, ERROR;

		@Override
		public boolean isErrorState() {
			return this == ERROR;
		}

		@Override
		public boolean isInitialState() {
			return this == OPEN;
		}
	}

	InputStreamStateMachine(InfoflowCFG icfg) {
		this.icfg = icfg;
		initialTrans = new MatcherTransition(States.NONE, constructors(), Parameter.This, States.OPEN, Type.OnReturn);
		addTransition(initialTrans);
		addTransition(
				new MatcherTransition(States.OPEN, closeMethods(), Parameter.This, States.CLOSED, Type.OnReturn));
		addTransition(
				new MatcherTransition(States.CLOSED, closeMethods(), Parameter.This, States.CLOSED, Type.OnReturn));
		addTransition(new MatcherTransition(States.OPEN, readMethods(), Parameter.This, States.OPEN, Type.OnReturn));
		addTransition(new MatcherTransition(States.ERROR, readMethods(), Parameter.This, States.ERROR, Type.OnReturn));
		addTransition(new MatcherTransition(States.CLOSED, readMethods(), Parameter.This, States.ERROR, Type.OnReturn));
		addTransition(new MatcherTransition(States.ERROR, closeMethods(), Parameter.This, States.ERROR, Type.OnReturn));

		addTransition(new MatcherTransition(States.CLOSED, Collections.singleton(Scene.v().getMethod("<java.io.InputStream: int read()>")), Parameter.This, States.ERROR, Type.OnCallToReturn));
		addTransition(new MatcherTransition(States.ERROR, Collections.singleton(Scene.v().getMethod("<java.io.InputStream: int read()>")), Parameter.This, States.ERROR, Type.OnCallToReturn));
	}


	private Set<SootMethod> constructors() {
		List<SootClass> subclasses = getSubclassesOf("java.util.InputStream");
		Set<SootMethod> out = new HashSet<>();
		for (SootClass c : subclasses) {
			for (SootMethod m : c.getMethods())
				if (m.isConstructor())
					out.add(m);
		}
		return out;
	}

	private Set<SootMethod> closeMethods() {
		return selectMethodByName(getImplementersOf("java.io.InputStream"), "close");
	}

	private Set<SootMethod> readMethods() {
		return selectMethodByName(getImplementersOf("java.io.InputStream"), "read");
	}


	private List<SootClass> getImplementersOf(String className) {
		SootClass sootClass = Scene.v().getSootClass(className);
		List<SootClass> list = Scene.v().getActiveHierarchy().getSubclassesOfIncluding(sootClass);
		List<SootClass> res = new LinkedList<>();
		for (SootClass c : list) {
			res.add(c);
		}
		return res;
	}

	@Override
	public Collection<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> generate(SootMethod method, Unit unit,
			Collection<SootMethod> calledMethod) {
		if(!method.getDeclaringClass().isApplicationClass())
			return Collections.emptySet();
		return this.generateThisAtAnyCallSitesOf(unit, calledMethod, constructors(), initialTrans);
	}
}
