package typestate.finiteautomata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.solver.Pair;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import typestate.TransitionFunction;
import typestate.TypestateChangeFunction;
import typestate.TypestateDomainValue;
import typestate.finiteautomata.MatcherTransition.Parameter;
import typestate.finiteautomata.MatcherTransition.Type;

public abstract class MatcherStateMachine implements TypestateChangeFunction {
	public Set<MatcherTransition> transition = new HashSet<>();

	public void addTransition(MatcherTransition trans) {
		transition.add(trans);
	}

	public Set<Transition> getReturnTransitionsFor(AccessGraph callerD1, Unit callSite, SootMethod calleeMethod,
			Unit exitStmt, AccessGraph exitNode, Unit returnSite, AccessGraph retNode) {
		return getMatchingTransitions(calleeMethod, exitNode, Type.OnReturn);
	}

	public Set<Transition> getCallTransitionsFor(AccessGraph callerD1, Unit callSite, SootMethod callee,
			AccessGraph srcNode, AccessGraph destNode) {
		return getMatchingTransitions(callee, destNode, Type.OnCall);
	}

	private Set<Transition> getMatchingTransitions(SootMethod method, AccessGraph node, Type type) {
		Set<Transition> res = new HashSet<>();
		if (node.getFieldCount() == 0) {
			for (MatcherTransition trans : transition) {

				if (trans.matches(method) && trans.getType().equals(type)) {
					Parameter param = trans.getParam();
					if (param.equals(Parameter.This) && BoomerangContext.isThisValue(method, node.getBase()))
						res.add(new Transition(trans.from(), trans.to()));
					if (param.equals(Parameter.Param1)
							&& method.getActiveBody().getParameterLocal(0).equals(node.getBase()))
						res.add(new Transition(trans.from(), trans.to()));
					if (param.equals(Parameter.Param2)
							&& method.getActiveBody().getParameterLocal(1).equals(node.getBase()))
						res.add(new Transition(trans.from(), trans.to()));
				}
			}
		}

		return res;
	}

	protected Set<SootMethod> selectMethodByName(Collection<SootClass> classes, String pattern) {
		Set<SootMethod> res = new HashSet<>();
		for (SootClass c : classes) {
			for (SootMethod m : c.getMethods()) {
				if (Pattern.matches(pattern, m.getName()))
					res.add(m);
			}
		}
		return res;
	}

	protected List<SootClass> getSubclassesOf(String className) {
		SootClass sootClass = Scene.v().getSootClass(className);
		List<SootClass> list = Scene.v().getActiveHierarchy().getSubclassesOfIncluding(sootClass);
		List<SootClass> res = new LinkedList<>();
		for (SootClass c : list) {
			res.add(c);
		}
		return res;
	}

	protected Collection<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> generateAtConstructor(Unit unit,
			Collection<SootMethod> calledMethod, MatcherTransition initialTrans) {
		boolean matches = false;
		for (SootMethod method : calledMethod) {
			if (initialTrans.matches(method)) {
				matches = true;
			}
		}
		if (!matches)
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

	protected Collection<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> generateReturnValueOf(Unit unit,
			Collection<SootMethod> calledMethod, MatcherTransition initialTrans) {
		boolean matches = false;
		for (SootMethod method : calledMethod) {
			if (initialTrans.matches(method)) {
				matches = true;
			}
		}
		if (!matches)
			return Collections.emptySet();
		if (!matches)
			return Collections.emptySet();
		if (unit instanceof AssignStmt) {
			Set<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> out = new HashSet<>();
			AssignStmt stmt = (AssignStmt) unit;
			out.add(new Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>(
					new AccessGraph((Local) stmt.getLeftOp(), stmt.getLeftOp().getType()),
					new TransitionFunction(initialTrans)));
			return out;
		}
		return Collections.emptySet();
	}
	
	protected Collection<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> generateThisAtAnyCallSitesOf(Unit unit,
			Collection<SootMethod> calledMethod, Set<SootMethod> hasToCall, MatcherTransition initialTrans) {
		for (SootMethod callee : calledMethod) {
			if (hasToCall.contains(callee)) {
				if (unit instanceof Stmt) {
					if (((Stmt) unit).getInvokeExpr() instanceof InstanceInvokeExpr) {
						InstanceInvokeExpr iie = (InstanceInvokeExpr) ((Stmt) unit).getInvokeExpr();
						Local thisLocal = (Local) iie.getBase();
						Set<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> out = new HashSet<>();
						out.add(new Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>(
								new AccessGraph(thisLocal, thisLocal.getType()), new TransitionFunction(initialTrans)));
						return out;
					}
				}

			}
		}
		return Collections.emptySet();
	}}
