package typestate;

import java.util.HashSet;
import java.util.Set;

import typestate.finiteautomata.State;

public class TypestateDomainValue {

  private final Set<State> states;

  public TypestateDomainValue(Set<State> trans) {
    this.states = trans;
  }

  public TypestateDomainValue() {
    this.states = new HashSet<>();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((states == null) ? 0 : states.hashCode());
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
    if (states == null) {
      if (other.states != null)
        return false;
    } else if (!states.equals(other.states))
      return false;
    return true;
  }

  public Set<State> getStates() {
    return new HashSet<>(states);
  }

  public boolean endsInErrorState() {
    for (State t : states) {
      if (t.isErrorState())
        return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return states.toString();
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
