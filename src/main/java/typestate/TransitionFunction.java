package typestate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import heros.EdgeFunction;
import heros.edgefunc.AllBottom;
import heros.edgefunc.AllTop;
import heros.edgefunc.EdgeIdentity;
import typestate.finiteautomata.State;
import typestate.finiteautomata.Transition;

public class TransitionFunction
 implements EdgeFunction<TypestateDomainValue> {

  public final Set<Transition> value;

  private static Logger logger = LoggerFactory.getLogger(TransitionFunction.class);

  public TransitionFunction(Set<? extends Transition> trans) {
    this.value = new HashSet<>(trans);
  }

  public TransitionFunction(Transition trans) {
    this.value = new HashSet<>(new HashSet<>(Collections.singleton(trans)));
  }
  @Override
  public TypestateDomainValue computeTarget(TypestateDomainValue source) {
	  System.err.println(source + " "+  this );
	  Set<State> states = new HashSet<>();
	  for(Transition t : value){
		  for(State sourceState : source.getStates()){
			  if(t.from().equals(sourceState)){
				  states.add(t.to());
			  }
		  }
	  }
    return new TypestateDomainValue(states);
  }

  @Override
  public EdgeFunction<TypestateDomainValue> composeWith(
      EdgeFunction<TypestateDomainValue> secondFunction) {
    if (secondFunction instanceof AllTop)
      return secondFunction;

    if (secondFunction instanceof AllBottom)
      return this;

    if (secondFunction instanceof EdgeIdentity){
      return this;
    }
    if (!(secondFunction instanceof TransitionFunction))
        throw new RuntimeException("Wrong type, is of type: " + secondFunction);
    TransitionFunction func = (TransitionFunction) secondFunction;
    Set<Transition> otherTransitions = func.value;
    Set<Transition> res = new HashSet<>();
    for (Transition first : value) {
      for (Transition second : otherTransitions) {
        if (second.from().equals(IdentityTransition.ID)
            && second.to().equals(IdentityTransition.ID)) {
          res.add(first);
        } else if (first.to().equals(second.from()) || first.to().equals(IdentityTransition.ID)
            || second.from().equals(IdentityTransition.ID))
          res.add(new Transition(first.from(), second.to()));
        // else
        // res.add(first);
      }
    }
    logger.debug("ComposeWith: {} with {} -> {}", this, secondFunction,
        new TransitionFunction(res));
    return new TransitionFunction(res);
  }

  @Override
  public EdgeFunction<TypestateDomainValue> joinWith(
      EdgeFunction<TypestateDomainValue> otherFunction) {
    if (otherFunction instanceof AllTop)
      return this;
    if (otherFunction instanceof AllBottom)
      return otherFunction;
    if (otherFunction instanceof EdgeIdentity) {
      Set<Transition> transitions = new HashSet<>();
      transitions.add(new IdentityTransition());
      transitions.addAll(value);
      return new TransitionFunction(transitions);
    }
    TransitionFunction func = (TransitionFunction) otherFunction;
    Set<Transition> transitions = func.value;
    transitions.addAll(value);
    return new TransitionFunction(transitions);
  }

  @Override
  public boolean equalTo(EdgeFunction<TypestateDomainValue> other) {
    if(!(other instanceof TransitionFunction))
        return false;
    TransitionFunction func = (TransitionFunction) other;
    return func.value.equals(value);
  }

  public String toString() {
    return "{Func:" + value.toString() + "}";
  };
}
