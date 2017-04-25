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
import typestate.finiteautomata.Transition;

public class TransitionFunction<State> implements EdgeFunction<TypestateDomainValue<State>> {

	private final Set<ITransition<State>> value;

	private static Logger logger = LoggerFactory.getLogger(TransitionFunction.class);

	public TransitionFunction(Set<? extends ITransition<State>> trans) {
		this.value = new HashSet<>(trans);
	}

	public TransitionFunction(ITransition<State> trans) {
		this(new HashSet<>(Collections.singleton(trans)));
	}

	@Override
	public TypestateDomainValue<State> computeTarget(TypestateDomainValue<State> source) {
		Set<State> states = new HashSet<>();
		for (ITransition<State> t : value) {
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
		return new TypestateDomainValue<State>(states);
	}

	@Override
	public EdgeFunction<TypestateDomainValue<State>> composeWith(EdgeFunction<TypestateDomainValue<State>> secondFunction) {
		if (secondFunction instanceof AllTop)
			return secondFunction;

		if (secondFunction instanceof AllBottom)
			return this;

		if (secondFunction instanceof EdgeIdentity) {
			return this;
		}
		if (!(secondFunction instanceof TransitionFunction))
			throw new RuntimeException("Wrong type, is of type: " + secondFunction);
		TransitionFunction<State> func = (TransitionFunction) secondFunction;
		Set<ITransition<State>> otherTransitions = func.value;
		Set<ITransition<State>> res = new HashSet<>();
		for (ITransition<State> first : value) {
			for (ITransition<State> second : otherTransitions) {
				if (second instanceof IdentityTransition) {
					res.add(first);
				} else if (first instanceof IdentityTransition) {
					res.add(second);
				} else if (first.to().equals(second.from()))
					res.add(new Transition<State>(first.from(), second.to()));
			}
		}
		logger.debug("ComposeWith: {} with {} -> {}", this, secondFunction, new TransitionFunction(res));
		return new TransitionFunction<State>(res);

	}

	@Override
	public EdgeFunction<TypestateDomainValue<State>> joinWith(EdgeFunction<TypestateDomainValue<State>> otherFunction) {
		if (otherFunction instanceof AllTop)
			return this;
		if (otherFunction instanceof AllBottom)
			return otherFunction;
		if (otherFunction instanceof EdgeIdentity) {
			Set<ITransition<State>> transitions = new HashSet<>(value);
			transitions.add(new IdentityTransition<State>());
			return new TransitionFunction<State>(transitions);
		}
		TransitionFunction<State> func = (TransitionFunction) otherFunction;
		Set<ITransition<State>> transitions =  new HashSet<>(func.value);
		transitions.addAll(value);
		return new TransitionFunction<State>(transitions);
	}

	@Override
	public boolean equalTo(EdgeFunction<TypestateDomainValue<State>> other) {
		if (!(other instanceof TransitionFunction))
			return false;
		TransitionFunction<State> func = (TransitionFunction) other;
		return func.value.equals(value);
	}

	public String toString() {
		return "{Func:" + value.toString() + "}";
	};
}
