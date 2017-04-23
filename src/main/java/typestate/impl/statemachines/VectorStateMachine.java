package typestate.impl.statemachines;

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
import ideal.Analysis;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.NewExpr;
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

	public static enum States implements State {
		INIT, NOT_EMPTY, ACCESSED_EMPTY;

		@Override
		public boolean isErrorState() {
			return this == ACCESSED_EMPTY;
		}

		@Override
		public boolean isInitialState() {
			return this == INIT;
		}
	}

	public VectorStateMachine() {
		addTransition(
				new MatcherTransition(States.INIT, addElement(), Parameter.This, States.NOT_EMPTY, Type.OnReturn));
		addTransition(new MatcherTransition(States.INIT, accessElement(), Parameter.This, States.ACCESSED_EMPTY,
				Type.OnReturn));
		addTransition(new MatcherTransition(States.NOT_EMPTY, accessElement(), Parameter.This, States.NOT_EMPTY,
				Type.OnReturn));

		addTransition(new MatcherTransition(States.NOT_EMPTY, removeAllElements(), Parameter.This, States.INIT,
				Type.OnReturn));
		addTransition(
				new MatcherTransition(States.INIT, removeAllElements(), Parameter.This, States.INIT, Type.OnReturn));
		addTransition(new MatcherTransition(States.ACCESSED_EMPTY, accessElement(), Parameter.This,
				States.ACCESSED_EMPTY, Type.OnReturn));
	}

	private Set<SootMethod> removeAllElements() {
		List<SootClass> vectorClasses = getSubclassesOf("java.util.Vector");
		Set<SootMethod> selectMethodByName = selectMethodByName(vectorClasses, "removeAllElements");
		return selectMethodByName;
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
	public Collection<AccessGraph> generateSeed(SootMethod m, Unit unit,
			Collection<SootMethod> calledMethod) {
		if(m.toString().contains("<clinit>"))
			return Collections.emptySet();
		return generateAtAllocationSiteOf(unit,"java.util.Vector");
	}
	
	@Override
	public TypestateDomainValue getBottomElement() {
		return new TypestateDomainValue(States.INIT);
	}

}
