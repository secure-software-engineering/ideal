package typestate.finiteautomata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
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
import typestate.TypestateChangeFunction;
import typestate.finiteautomata.MatcherTransition.Parameter;
import typestate.finiteautomata.MatcherTransition.Type;

public abstract class MatcherStateMachine<State> implements TypestateChangeFunction<State> {
	public Set<MatcherTransition<State>> transition = new HashSet<>();

	public void addTransition(MatcherTransition<State> trans) {
		transition.add(trans);
	}

	public Set<Transition<State>> getReturnTransitionsFor(AccessGraph callerD1, Unit callSite, SootMethod calleeMethod,
			Unit exitStmt, AccessGraph exitNode, Unit returnSite, AccessGraph retNode) {
		return getMatchingTransitions(calleeMethod, exitNode, Type.OnReturn);
	}

	public Set<Transition<State>> getCallTransitionsFor(AccessGraph callerD1, Unit callSite, SootMethod callee,
			AccessGraph srcNode, AccessGraph destNode) {
		return getMatchingTransitions(callee, destNode, Type.OnCall);
	}

	public Set<Transition<State>> getCallToReturnTransitionsFor(AccessGraph d1, Unit callSite, AccessGraph d2,
			Unit returnSite, AccessGraph d3) {
		Set<Transition<State>> res = new HashSet<>();
		if(callSite instanceof Stmt){
			Stmt stmt = (Stmt) callSite;
			if(stmt.containsInvokeExpr() && stmt.getInvokeExpr() instanceof InstanceInvokeExpr){
				SootMethod method = stmt.getInvokeExpr().getMethod();
				InstanceInvokeExpr e = (InstanceInvokeExpr)stmt.getInvokeExpr();
				if(e.getBase().equals(d2.getBase())){
					for (MatcherTransition<State> trans : transition) {
						if(trans.matches(method) && trans.getType().equals(Type.OnCallToReturn)){
							res.add(trans);
						}
					}	
				}
			}
		}
		return res;
	}

	private Set<Transition<State>> getMatchingTransitions(SootMethod method, AccessGraph node, Type type) {
		Set<Transition<State>> res = new HashSet<>();
		if (node.getFieldCount() == 0) {
			for (MatcherTransition<State> trans : transition) {
				if (trans.matches(method) && trans.getType().equals(type)) {
					Parameter param = trans.getParam();
					if (param.equals(Parameter.This) && BoomerangContext.isThisValue(method, node.getBase()))
						res.add(new Transition<State>(trans.from(), trans.to()));
					if (param.equals(Parameter.Param1)
							&& method.getActiveBody().getParameterLocal(0).equals(node.getBase()))
						res.add(new Transition<State>(trans.from(), trans.to()));
					if (param.equals(Parameter.Param2)
							&& method.getActiveBody().getParameterLocal(1).equals(node.getBase()))
						res.add(new Transition<State>(trans.from(), trans.to()));
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

	protected Collection<AccessGraph> generateAtConstructor(Unit unit,
			Collection<SootMethod> calledMethod, MatcherTransition<State> initialTrans) {
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
						Set<AccessGraph> out = new HashSet<>();
						out.add(new AccessGraph(l));
						return out;
					}
				}
		}
		return Collections.emptySet();
	}

	protected Collection<AccessGraph> getLeftSideOf(Unit unit) {
		if (unit instanceof AssignStmt) {
			Set<AccessGraph> out = new HashSet<>();
			AssignStmt stmt = (AssignStmt) unit;
			out.add(
					new AccessGraph((Local) stmt.getLeftOp()));
			return out;
		}
		return Collections.emptySet();
	}
	
	protected Collection<AccessGraph> generateThisAtAnyCallSitesOf(Unit unit,
			Collection<SootMethod> calledMethod, Set<SootMethod> hasToCall) {
		for (SootMethod callee : calledMethod) {
			if (hasToCall.contains(callee)) {
				if (unit instanceof Stmt) {
					if (((Stmt) unit).getInvokeExpr() instanceof InstanceInvokeExpr) {
						InstanceInvokeExpr iie = (InstanceInvokeExpr) ((Stmt) unit).getInvokeExpr();
						Local thisLocal = (Local) iie.getBase();
						Set<AccessGraph> out = new HashSet<>();
						out.add(new AccessGraph(thisLocal));
						return out;
						
					}
				}

			}
		}
		return Collections.emptySet();
	}
	

	protected Collection<AccessGraph> generateAtAllocationSiteOf(Unit unit, Class allocationSuperType) {
		if(unit instanceof AssignStmt){
			AssignStmt assignStmt = (AssignStmt) unit;
			if(assignStmt.getRightOp() instanceof NewExpr){
				NewExpr newExpr = (NewExpr) assignStmt.getRightOp();
				Value leftOp = assignStmt.getLeftOp();
				soot.Type type = newExpr.getType();
				if(Scene.v().getOrMakeFastHierarchy().canStoreType(type, Scene.v().getType(allocationSuperType.getName()))){
					return Collections.singleton(new AccessGraph((Local)leftOp));
				}
			}
		}
		return Collections.emptySet();
	}
	@Override
	public Collection<AccessGraph> generate(SootMethod method, Unit stmt,
			Collection<SootMethod> calledMethods) {
		return generateSeed(method, stmt, calledMethods);
	}
	
	public abstract Collection<AccessGraph> generateSeed(SootMethod method, Unit stmt,
			Collection<SootMethod> calledMethods);
	
	@Override
	public String toString() {
		return Joiner.on("\n").join(transition);
	}
}
	
