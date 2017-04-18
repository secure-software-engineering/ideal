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
import typestate.finiteautomata.Transition;

public class TransitionFunction
 implements EdgeFunction<TypestateDomainValue> {

  public final TypestateDomainValue value;

  private static Logger logger = LoggerFactory.getLogger(TransitionFunction.class);

  public TransitionFunction(Set<? extends Transition> trans) {
    this.value = new TypestateDomainValue(trans);
  }

  public TransitionFunction(Transition trans) {
    this.value = new TypestateDomainValue(new HashSet<>(Collections.singleton(trans)));
  }
  @Override
  public TypestateDomainValue computeTarget(TypestateDomainValue source) {
    return value;
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
    TypestateDomainValue otherTransitions = func.value;
    Set<Transition> res = new HashSet<>();
    for (Transition first : value.getTransitions()) {
      for (Transition second : otherTransitions.getTransitions()) {
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
      transitions.addAll(value.getTransitions());
      return new TransitionFunction(transitions);
    }
    TransitionFunction func = (TransitionFunction) otherFunction;
    Set<Transition> transitions = func.value.getTransitions();
    transitions.addAll(value.getTransitions());
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
