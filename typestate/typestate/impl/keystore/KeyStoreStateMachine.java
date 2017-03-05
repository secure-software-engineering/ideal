package typestate.impl.keystore;

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
import soot.jimple.AssignStmt;
import typestate.TransitionFunction;
import typestate.TypestateChangeFunction;
import typestate.TypestateDomainValue;
import typestate.finiteautomata.MatcherStateMachine;
import typestate.finiteautomata.MatcherTransition;
import typestate.finiteautomata.MatcherTransition.Parameter;
import typestate.finiteautomata.MatcherTransition.Type;
import typestate.finiteautomata.State;
import typestate.finiteautomata.Transition;

public class KeyStoreStateMachine extends MatcherStateMachine implements TypestateChangeFunction {

  private MatcherTransition initialTrans;

  public static enum States implements State {
    NONE, INIT, LOADED, ERROR;

    @Override
    public boolean isErrorState() {
      return this == ERROR;
    }

    @Override
    public boolean isInitialState() {
      return this == INIT;
    }
  }

  KeyStoreStateMachine() {
    initialTrans =
        new MatcherTransition(States.NONE, keyStoreConstructor(),Parameter.This, States.INIT, Type.OnReturn);
    addTransition(initialTrans);
    addTransition(new MatcherTransition(States.INIT, loadMethods(),Parameter.This, States.LOADED, Type.OnReturn));

    addTransition(
        new MatcherTransition(States.INIT, anyMethodOtherThanLoad(),Parameter.This, States.ERROR, Type.OnReturn));
    addTransition(
        new MatcherTransition(States.ERROR, anyMethodOtherThanLoad(),Parameter.This, States.ERROR, Type.OnReturn));

  }
  private Set<SootMethod> anyMethodOtherThanLoad() {
    List<SootClass> subclasses = getSubclassesOf("java.security.KeyStore");
    Set<SootMethod> loadMethods = loadMethods();
    Set<SootMethod> out = new HashSet<>();
    for (SootClass c : subclasses) {
      for (SootMethod m : c.getMethods())
        if (m.isPublic() && !loadMethods.contains(m) && !m.isStatic())
          out.add(m);
    }
    return out;
  }

  private Set<SootMethod> loadMethods() {
    return selectMethodByName(getSubclassesOf("java.security.KeyStore"), "load");
  }


  private Set<SootMethod> keyStoreConstructor() {
    List<SootClass> subclasses = getSubclassesOf("java.security.KeyStore");
    Set<SootMethod> out = new HashSet<>();
    for (SootClass c : subclasses) {
      for (SootMethod m : c.getMethods())
        if (m.getName().equals("getInstance") && m.isStatic())
          out.add(m);
    }
    return out;
  }


  @Override
  public Collection<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> generate(SootMethod m, Unit unit,
      Collection<SootMethod> calledMethod) {
    boolean matches = false;
    for (SootMethod method : calledMethod) {
      if (initialTrans.matches(method)) {
        matches = true;
      }
    }
    if (!matches || !m.getDeclaringClass().isApplicationClass())
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

}

