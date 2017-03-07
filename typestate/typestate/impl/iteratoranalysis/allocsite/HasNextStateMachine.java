package typestate.impl.iteratoranalysis.allocsite;

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
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.NewExpr;
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

public class HasNextStateMachine extends MatcherStateMachine implements TypestateChangeFunction {

	private MatcherTransition initialTrans;
	private InfoflowCFG icfg;
	private Set<SootMethod> hasNextMethods;

	public static enum States implements State {
		NONE, INIT, HASNEXT, ERROR;

		@Override
		public boolean isErrorState() {
			return this == ERROR;
		}

		@Override
		public boolean isInitialState() {
			return this == INIT;
		}
	}

	HasNextStateMachine(InfoflowCFG cfg) {
		this.icfg = cfg;
		initialTrans = new MatcherTransition(States.NONE, retrieveIteratorConstructors(), Parameter.This, States.INIT,
				Type.None);
		addTransition(initialTrans);
		addTransition(
				new MatcherTransition(States.INIT, retrieveNextMethods(), Parameter.This, States.ERROR, Type.OnReturn));
		addTransition(new MatcherTransition(States.ERROR, retrieveNextMethods(), Parameter.This, States.ERROR,
				Type.OnReturn));
		addTransition(new MatcherTransition(States.HASNEXT, retrieveNextMethods(), Parameter.This, States.INIT,
				Type.OnReturn));
		addTransition(new MatcherTransition(States.INIT, retrieveHasNextMethods(), Parameter.This, States.HASNEXT,
				Type.OnReturn));
		addTransition(new MatcherTransition(States.HASNEXT, retrieveHasNextMethods(), Parameter.This, States.HASNEXT,
				Type.OnReturn));
		addTransition(new MatcherTransition(States.ERROR, retrieveHasNextMethods(), Parameter.This, States.ERROR,
				Type.OnReturn));
	}

	private Set<SootMethod> retrieveHasNextMethods() {
		if (hasNextMethods == null)
			hasNextMethods = selectMethodByName(getImplementersOfIterator("java.util.Iterator"), "hasNext");
		return hasNextMethods;
	}

	private Set<SootMethod> retrieveNextMethods() {
		return selectMethodByName(getImplementersOfIterator("java.util.Iterator"), "next");
	}

	private Set<SootMethod> retrieveIteratorConstructors() {
		Set<SootMethod> selectMethodByName = selectMethodByName(Scene.v().getClasses(), "iterator");
		Set<SootMethod> res = new HashSet<>();
		for (SootMethod m : selectMethodByName) {
			if (m.getReturnType() instanceof RefType) {
				RefType refType = (RefType) m.getReturnType();
				SootClass classs = refType.getSootClass();
				if (classs.equals(Scene.v().getSootClass("java.util.Iterator")) || Scene.v().getActiveHierarchy()
						.getImplementersOf(Scene.v().getSootClass("java.util.Iterator")).contains(classs)) {
					res.add(m);
				}
			}
		}
		return res;
	}

	private List<SootClass> getImplementersOfIterator(String className) {
		SootClass sootClass = Scene.v().getSootClass(className);
		List<SootClass> list = Scene.v().getActiveHierarchy().getImplementersOf(sootClass);
		List<SootClass> res = new LinkedList<>();
		for (SootClass c : list) {
			res.add(c);
		}
		return res;
	}

	@Override
	public Collection<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> generate(SootMethod method, Unit unit,
			Collection<SootMethod> calledMethod) {
		if (!method.getDeclaringClass().isApplicationClass())
			return Collections.emptySet();
		if (unit instanceof AssignStmt) {
			if (((AssignStmt) unit).getRightOp() instanceof NewExpr) {
				NewExpr newExpr = (NewExpr) ((AssignStmt) unit).getRightOp();
				if (newExpr.getType() instanceof RefType) {
					RefType refType = (RefType) newExpr.getType();
					if (getImplementersOfIterator("java.util.Iterator").contains(refType.getSootClass())) {
						Set<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> out = new HashSet<>();
						AssignStmt stmt = (AssignStmt) unit;
						out.add(new Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>(
								new AccessGraph((Local) stmt.getLeftOp(), stmt.getLeftOp().getType()),
								new TransitionFunction(initialTrans)));
						return out;
					}
				}
			}

		}
		return Collections.emptySet();
	}

	@Override
	public Set<Transition> getReturnTransitionsFor(AccessGraph callerD1, Unit callSite, SootMethod calleeMethod,
			Unit exitStmt, AccessGraph exitNode, Unit returnSite, AccessGraph retNode) {
		if (retrieveHasNextMethods().contains(calleeMethod)) {
			if (icfg.getMethodOf(callSite).getSignature().contains("java.lang.Object next()"))
				return Collections.emptySet();
		}

		return super.getReturnTransitionsFor(callerD1, callSite, calleeMethod, exitStmt, exitNode, returnSite, retNode);
	}
}
