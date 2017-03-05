package typestate.impl.vector;

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
import soot.jimple.InstanceInvokeExpr;
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

public class VectorStateMachine extends MatcherStateMachine implements TypestateChangeFunction {

	private MatcherTransition initialTrans;
	private InfoflowCFG icfg;

	public static enum States implements State {
		NONE, INIT, NOT_EMPTY, ACCESSED_EMPTY;

		@Override
		public boolean isErrorState() {
			return this == ACCESSED_EMPTY;
		}

		@Override
		public boolean isInitialState() {
			return this == INIT;
		}
	}

	VectorStateMachine(InfoflowCFG icfg) {
		this.icfg = icfg;
		initialTrans = new MatcherTransition(States.NONE, vectorConstructor(), Parameter.This,States.INIT, Type.OnReturn);
		addTransition(initialTrans);
		addTransition(new MatcherTransition(States.INIT, addElement(), Parameter.This, States.NOT_EMPTY, Type.OnReturn));
		addTransition(new MatcherTransition(States.INIT, accessElement(), Parameter.This,States.ACCESSED_EMPTY, Type.OnReturn));
		addTransition(new MatcherTransition(States.NOT_EMPTY, removeAllElements(), Parameter.This, States.INIT, Type.OnReturn));
		addTransition(new MatcherTransition(States.INIT, removeAllElements(),Parameter.This, States.INIT, Type.OnReturn));
		addTransition(
				new MatcherTransition(States.ACCESSED_EMPTY, accessElement(), Parameter.This,States.ACCESSED_EMPTY, Type.OnReturn));
	}
	private Set<SootMethod> removeAllElements() {
		List<SootClass> vectorClasses = getSubclassesOf("java.util.Vector");
		Set<SootMethod> selectMethodByName = selectMethodByName(vectorClasses, "removeAllElements");
		return selectMethodByName;
	}

	private Set<SootMethod> vectorConstructor() {
		List<SootClass> subclasses = getSubclassesOf("java.util.Vector");
		Set<SootMethod> out = new HashSet<>();
		for (SootClass c : subclasses) {
			for (SootMethod m : c.getMethods())
				if (m.isConstructor()
						&& !m.getSignature().equals("<java.util.Vector: void <init>(java.util.Collection)>"))
					out.add(m);
		}
		return out;
	}

	private Set<SootMethod> addElement() {
		List<SootClass> vectorClasses = getSubclassesOf("java.util.Vector");
		Set<SootMethod> selectMethodByName = selectMethodByName(vectorClasses,
				"add|addAll|addElement|insertElementAt|set|setElementAt");
		return selectMethodByName;
	}

	private Set<SootMethod> accessElement() {
		List<SootClass> vectorClasses = getSubclassesOf("java.util.Vector");
		Set<SootMethod> selectMethodByName = selectMethodByName(vectorClasses,
				"elementAt|firstElement|lastElement|get");
		return selectMethodByName;
	}


	@Override
	public Collection<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> generate(SootMethod m, Unit unit,
			Collection<SootMethod> calledMethod) {
		if(!m.getDeclaringClass().isApplicationClass())
			return Collections.emptySet();
		boolean matches = false;
		for (SootMethod method : calledMethod) {
			if (initialTrans.matches(method) && !initialTrans.matches(icfg.getMethodOf(unit))) {
				matches = true;
			}
		}
		if (!matches || icfg.getMethodOf(unit).getSignature().equals("<java.lang.ClassLoader: void <clinit>()>"))
			return Collections.emptySet();
		if (unit instanceof Stmt) {
			Stmt stmt = (Stmt) unit;
			if (stmt.containsInvokeExpr())
				if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
					InstanceInvokeExpr iie = (InstanceInvokeExpr) stmt.getInvokeExpr();
					if (iie.getBase() instanceof Local) {
						Local l = (Local) iie.getBase();
						Set<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> out = new HashSet<>();
						out.add(new Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>(
								new AccessGraph(l, l.getType()), new TransitionFunction(initialTrans)));
						return out;
					}
				}
		}
		return Collections.emptySet();
	}
}
