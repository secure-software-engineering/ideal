package typestate.impl.statemachines;

import java.util.Collection;

import boomerang.accessgraph.AccessGraph;
import soot.SootMethod;
import soot.Unit;
import test.ConcreteState;
import typestate.TypestateDomainValue;
import typestate.finiteautomata.MatcherStateMachine;
import typestate.finiteautomata.MatcherTransition;
import typestate.finiteautomata.MatcherTransition.Parameter;
import typestate.finiteautomata.MatcherTransition.Type;
import typestate.test.helper.File;

public class FileMustBeClosedStateMachine extends MatcherStateMachine<ConcreteState>{

  public static enum States implements ConcreteState {
    NONE, INIT, OPENED, CLOSED;

    @Override
    public boolean isErrorState() {
      return this == OPENED;
    }

  }

  public FileMustBeClosedStateMachine() {
    addTransition(new MatcherTransition<ConcreteState>(States.INIT, ".*open.*",Parameter.This, States.OPENED, Type.OnReturn));
    addTransition(new MatcherTransition<ConcreteState>(States.INIT, ".*close.*",Parameter.This, States.CLOSED, Type.OnReturn));
    addTransition(new MatcherTransition<ConcreteState>(States.OPENED, ".*close.*",Parameter.This, States.CLOSED, Type.OnReturn));
  }



  @Override
  public Collection<AccessGraph> generateSeed(SootMethod method,Unit unit,
      Collection<SootMethod> calledMethod) {
    return generateAtAllocationSiteOf(unit, File.class);
  }



public TypestateDomainValue<ConcreteState> getBottomElement() {
	return new TypestateDomainValue<ConcreteState>(States.INIT);
}



}
