package typestate.finiteautomata;

public class IdentityTransition implements ITransition {

	public IdentityTransition() {
	}

	public boolean equals(Object o) {
		return o instanceof IdentityTransition;
	}

	@Override
	public State from() {
		throw new RuntimeException("Unreachable");
	}

	@Override
	public State to() {
		throw new RuntimeException("Unreachable");
	}

}
