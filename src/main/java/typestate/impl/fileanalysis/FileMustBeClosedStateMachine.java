package typestate.impl.fileanalysis;

import java.util.Collection;

import boomerang.accessgraph.AccessGraph;
import heros.solver.Pair;
import soot.SootMethod;
import soot.Unit;
import typestate.TypestateDomainValue;
import typestate.finiteautomata.MatcherStateMachine;
import typestate.finiteautomata.MatcherTransition;
import typestate.finiteautomata.MatcherTransition.Parameter;
import typestate.finiteautomata.MatcherTransition.Type;
import typestate.finiteautomata.State;

public class FileMustBeClosedStateMachine extends MatcherStateMachine{

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
    addTransition(new MatcherTransition(States.INIT, ".*open.*",Parameter.This, States.OPENED, Type.OnReturn));
    addTransition(new MatcherTransition(States.INIT, ".*close.*",Parameter.This, States.CLOSED, Type.OnReturn));
    addTransition(new MatcherTransition(States.OPENED, ".*close.*",Parameter.This, States.CLOSED, Type.OnReturn));
  }



  @Override
  public Collection<AccessGraph> generateSeed(SootMethod method,Unit unit,
      Collection<SootMethod> calledMethod) {
    return generateAtAllocationSiteOf(unit, "targets.file.File");
  }



public TypestateDomainValue getBottomElement() {
	return new TypestateDomainValue(States.INIT);
}



}
