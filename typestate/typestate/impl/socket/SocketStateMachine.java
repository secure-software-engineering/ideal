package typestate.impl.socket;

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

public class SocketStateMachine extends MatcherStateMachine implements TypestateChangeFunction {

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

  SocketStateMachine(InfoflowCFG icfg) {
    this.icfg = icfg;
    initialTrans =
        new MatcherTransition(States.NONE, socketConstructor(),Parameter.This, States.INIT, Type.OnReturn);
    addTransition(initialTrans);
    addTransition(new MatcherTransition(States.INIT, connect(),Parameter.This, States.CONNECTED, Type.OnReturn));
    addTransition(new MatcherTransition(States.INIT, useMethods(),Parameter.This, States.ERROR, Type.OnReturn));
    addTransition(new MatcherTransition(States.ERROR, useMethods(),Parameter.This, States.ERROR, Type.OnReturn));
  }
  private Set<SootMethod> socketConstructor() {
    List<SootClass> subclasses = getSubclassesOf("java.net.Socket");
    Set<SootMethod> out = new HashSet<>();
    for (SootClass c : subclasses) {
      for (SootMethod m : c.getMethods())
        if (m.isConstructor())
          out.add(m);
    }
    return out;
  }

  private Set<SootMethod> connect() {
    return selectMethodByName(getSubclassesOf("java.net.Socket"), "connect");
  }


  private Set<SootMethod> useMethods() {
    List<SootClass> subclasses = getSubclassesOf("java.net.Socket");
    Set<SootMethod> connectMethod = connect();
    Set<SootMethod> out = new HashSet<>();
    for (SootClass c : subclasses) {
      for (SootMethod m : c.getMethods())
        if (m.isPublic() && !connectMethod.contains(m) && !m.isStatic())
          out.add(m);
    }
    return out;
  }


  @Override
  public Collection<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> generate(SootMethod m, Unit unit,
      Collection<SootMethod> calledMethod) {
	  if(!m.getDeclaringClass().isApplicationClass())
			return Collections.emptySet();
	  return generateAtConstructor(unit, calledMethod, initialTrans);

  }


}

