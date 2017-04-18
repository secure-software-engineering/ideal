package typestate;

import java.util.HashSet;
import java.util.Set;

import typestate.finiteautomata.Transition;

public class TypestateDomainValue {

  private final Set<? extends Transition> transitions;

  public TypestateDomainValue(Set<? extends Transition> trans) {
    this.transitions = trans;
  }

  public TypestateDomainValue() {
    this.transitions = new HashSet<>();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((transitions == null) ? 0 : transitions.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TypestateDomainValue other = (TypestateDomainValue) obj;
    if (transitions == null) {
      if (other.transitions != null)
        return false;
    } else if (!transitions.equals(other.transitions))
      return false;
    return true;
  }

  public Set<Transition> getTransitions() {
    return new HashSet<>(transitions);
  }

  public boolean endsInErrorState() {
    for (Transition t : transitions) {
      if (t.to().isErrorState())
        return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return transitions.toString();
  }

  static final TypestateDomainValue BOTTOM = new TypestateDomainValue() {
    public int hashCode() {
      return 101001011;
    };

    public boolean equals(Object obj) {
      return obj == BOTTOM;
    };

    public String toString() {
      return "BOTTOM";
    };
  };
  static final TypestateDomainValue TOP = new TypestateDomainValue() {
    public int hashCode() {
      return 101001010;
    };

    public boolean equals(Object obj) {
      return obj == TOP;
    };

    public String toString() {
      return "TOP";
    };
  };
}
