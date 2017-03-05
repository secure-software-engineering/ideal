package typestate.impl.pipedoutputstream;

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
import typestate.TransitionFunction;
import typestate.TypestateChangeFunction;
import typestate.TypestateDomainValue;
import typestate.finiteautomata.MatcherStateMachine;
import typestate.finiteautomata.MatcherTransition;
import typestate.finiteautomata.MatcherTransition.Parameter;
import typestate.finiteautomata.MatcherTransition.Type;
import typestate.finiteautomata.State;
import typestate.finiteautomata.Transition;

public class PipedOutputStreamStateMachine extends MatcherStateMachine implements TypestateChangeFunction {

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
			return this == NONE;
		}
	}

	PipedOutputStreamStateMachine(InfoflowCFG icfg) {
		this.icfg = icfg;
		initialTrans = new MatcherTransition(States.NONE, constructors(), Parameter.This, States.INIT, Type.OnReturn);
		addTransition(initialTrans);
		addTransition(
				new MatcherTransition(States.INIT, connect(), Parameter.This, States.CONNECTED, Type.OnReturn));
		addTransition(new MatcherTransition(States.INIT, readMethods(), Parameter.This, States.ERROR, Type.OnReturn));
		addTransition(new MatcherTransition(States.CONNECTED, readMethods(), Parameter.This, States.CONNECTED, Type.OnReturn));
		addTransition(new MatcherTransition(States.ERROR, readMethods(), Parameter.This, States.ERROR, Type.OnReturn));
	}

	private Set<SootMethod> constructors() {
		List<SootClass> subclasses = getSubclassesOf("java.io.PipedOutputStream");
		Set<SootMethod> out = new HashSet<>();
		for (SootClass c : subclasses) {
			for (SootMethod m : c.getMethods())
				if (m.isConstructor() && !m.toString().contains("PipedInputStream"))
					out.add(m);
		}
		return out;
	}
	private Set<SootMethod> connect() {
		return selectMethodByName(getSubclassesOf("java.io.PipedOutputStream"), "connect");
	}


	private Set<SootMethod> readMethods() {
		return selectMethodByName(getSubclassesOf("java.io.PipedOutputStream"), "write");
	}


	@Override
	public Collection<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> generate(SootMethod m, Unit unit,
			Collection<SootMethod> calledMethod) {
		if(!m.getDeclaringClass().isApplicationClass())
			return Collections.emptySet();
		return generateAtConstructor(unit, calledMethod, initialTrans);
	}
}
