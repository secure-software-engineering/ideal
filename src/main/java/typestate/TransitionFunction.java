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
import typestate.finiteautomata.ITransition;
import typestate.finiteautomata.IdentityTransition;
import typestate.finiteautomata.State;
import typestate.finiteautomata.Transition;

public class TransitionFunction implements EdgeFunction<TypestateDomainValue> {

	private final Set<ITransition> value;

	private static Logger logger = LoggerFactory.getLogger(TransitionFunction.class);

	public TransitionFunction(Set<? extends ITransition> trans) {
		this.value = new HashSet<>(trans);
	}

	public TransitionFunction(ITransition trans) {
		this(new HashSet<>(Collections.singleton(trans)));
	}

	@Override
	public TypestateDomainValue computeTarget(TypestateDomainValue source) {
		Set<State> states = new HashSet<>();
		for (ITransition t : value) {
			if (t instanceof IdentityTransition) {
				states.addAll(source.getStates());
				continue;
			}
			for (State sourceState : source.getStates()) {
				if (t.from().equals(sourceState)) {
					states.add(t.to());
				}
			}
		}
		return new TypestateDomainValue(states);
	}

	@Override
	public EdgeFunction<TypestateDomainValue> composeWith(EdgeFunction<TypestateDomainValue> secondFunction) {
		if (secondFunction instanceof AllTop)
			return secondFunction;

		if (secondFunction instanceof AllBottom)
			return this;

		if (secondFunction instanceof EdgeIdentity) {
			return this;
		}
		if (!(secondFunction instanceof TransitionFunction))
			throw new RuntimeException("Wrong type, is of type: " + secondFunction);
		TransitionFunction func = (TransitionFunction) secondFunction;
		Set<ITransition> otherTransitions = func.value;
		Set<ITransition> res = new HashSet<>();
		for (ITransition first : value) {
			for (ITransition second : otherTransitions) {
				if (second instanceof IdentityTransition) {
					res.add(first);
				} else if (first instanceof IdentityTransition) {
					res.add(second);
				} else if (first.to().equals(second.from()))
					res.add(new Transition(first.from(), second.to()));
			}
		}
		logger.debug("ComposeWith: {} with {} -> {}", this, secondFunction, new TransitionFunction(res));
		return new TransitionFunction(res);

	}

	@Override
	public EdgeFunction<TypestateDomainValue> joinWith(EdgeFunction<TypestateDomainValue> otherFunction) {
		if (otherFunction instanceof AllTop)
			return this;
		if (otherFunction instanceof AllBottom)
			return otherFunction;
		if (otherFunction instanceof EdgeIdentity) {
			Set<ITransition> transitions = new HashSet<>(value);
			transitions.add(new IdentityTransition());
			return new TransitionFunction(transitions);
		}
		TransitionFunction func = (TransitionFunction) otherFunction;
		Set<ITransition> transitions =  new HashSet<>(func.value);
		transitions.addAll(value);
		return new TransitionFunction(transitions);
	}

	@Override
	public boolean equalTo(EdgeFunction<TypestateDomainValue> other) {
		if (!(other instanceof TransitionFunction))
			return false;
		TransitionFunction func = (TransitionFunction) other;
		return func.value.equals(value);
	}

	public String toString() {
		return "{Func:" + value.toString() + "}";
	};
}
