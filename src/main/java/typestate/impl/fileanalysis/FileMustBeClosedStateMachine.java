package typestate.impl.fileanalysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.solver.Pair;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import typestate.TransitionFunction;
import typestate.TypestateDomainValue;
import typestate.finiteautomata.MatcherStateMachine;
import typestate.finiteautomata.MatcherTransition;
import typestate.finiteautomata.MatcherTransition.Parameter;
import typestate.finiteautomata.MatcherTransition.Type;
import typestate.finiteautomata.State;

public class FileMustBeClosedStateMachine extends MatcherStateMachine{
  private MatcherTransition initialTrans;

  public static enum States implements State {
    NONE, INIT, OPENED, CLOSED;

    @Override
    public boolean isErrorState() {
      return this == OPENED;
    }

    @Override
    public boolean isInitialState() {
      return this == INIT;
    }
  }

  FileMustBeClosedStateMachine() {
    initialTrans =
 new MatcherTransition(States.NONE, "<targets.file.File: void <init>\\(\\)>",Parameter.This, States.INIT,
        Type.OnCall);
    addTransition(initialTrans);
    addTransition(new MatcherTransition(States.INIT, ".*open.*",Parameter.This, States.OPENED, Type.OnReturn));
    addTransition(new MatcherTransition(States.OPENED, ".*close.*",Parameter.This, States.CLOSED, Type.OnReturn));
  }



  @Override
  public Collection<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> generateSeed(SootMethod method,Unit unit,
      Collection<SootMethod> calledMethod) {
    boolean matches = false;
    for (SootMethod m : calledMethod) {
      if (initialTrans.matches(m)) {
        matches = true;
      }
    }
    if (!matches)
      return Collections.emptySet();
    if (unit instanceof Stmt && ((Stmt) unit).getInvokeExpr() instanceof InstanceInvokeExpr) {
      InstanceInvokeExpr iie = (InstanceInvokeExpr) ((Stmt) unit).getInvokeExpr();
      Set<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> out = new HashSet<>();
      out.add(new Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>(
          new AccessGraph((Local) iie.getBase(), iie.getBase().getType()),
          new TransitionFunction(initialTrans)));
      return out;
    }
    return Collections.emptySet();
  }



}
