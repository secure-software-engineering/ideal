package typestate;

import typestate.finiteautomata.State;
import typestate.finiteautomata.Transition;

public class IdentityTransition extends Transition {
  public static final State ID = new State() {
    @Override
    public boolean isErrorState() {
      return false;
    }

    @Override
    public boolean isInitialState() {
      return false;
    }
    public String toString(){
      return "*";
    }
  };

  public IdentityTransition() {
    super(ID, ID);
  }

}
