package typestate;

import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.edgefunc.AllBottom;
import heros.edgefunc.EdgeIdentity;
import ideal.edgefunction.AnalysisEdgeFunctions;
import soot.SootMethod;
import soot.Unit;
import typestate.finiteautomata.Transition;

public class TypestateEdgeFunctions implements AnalysisEdgeFunctions<TypestateDomainValue> {

  public final static EdgeFunction<TypestateDomainValue> ALL_BOTTOM =
      new AllBottom<TypestateDomainValue>(TypestateDomainValue.BOTTOM);
  private TypestateChangeFunction func;

  public TypestateEdgeFunctions(TypestateChangeFunction func) {
    this.func = func;
  }

  @Override
  public EdgeFunction<TypestateDomainValue> getNormalEdgeFunction(AccessGraph d1, Unit curr,
      AccessGraph currNode, Unit succ, AccessGraph succNode) {
    return EdgeIdentity.v();
  }

  @Override
  public EdgeFunction<TypestateDomainValue> getCallEdgeFunction(AccessGraph callerD1, Unit callSite,
      AccessGraph srcNode, SootMethod calleeMethod, AccessGraph destNode) {
    Set<? extends Transition> trans =
        func.getCallTransitionsFor(callerD1, callSite, calleeMethod, srcNode, destNode);
    if (trans.isEmpty())
      return EdgeIdentity.v();
    return new TransitionFunction(trans);
  }

  @Override
  public EdgeFunction<TypestateDomainValue> getReturnEdgeFunction(AccessGraph callerD1,
      Unit callSite, SootMethod calleeMethod, Unit exitStmt, AccessGraph exitNode, Unit returnSite,
      AccessGraph retNode) {

    Set<? extends Transition> trans = func.getReturnTransitionsFor(callerD1, callSite, calleeMethod,
        exitStmt, exitNode, returnSite, retNode);
    if (trans.isEmpty())
      return EdgeIdentity.v();
    return new TransitionFunction(trans);
  }

  @Override
  public EdgeFunction<TypestateDomainValue> getCallToReturnEdgeFunction(AccessGraph d1,
      Unit callSite, AccessGraph callNode, Unit returnSite, AccessGraph returnSideNode) {
    return EdgeIdentity.v();
  }

  @Override
  public TypestateDomainValue bottom() {
    return TypestateDomainValue.BOTTOM;
  }

  @Override
  public TypestateDomainValue top() {
    return TypestateDomainValue.TOP;
  }

  @Override
  public TypestateDomainValue join(TypestateDomainValue left, TypestateDomainValue right) {
    Set<Transition> transitions = left.getTransitions();
    transitions.addAll(right.getTransitions());
    return new TypestateDomainValue(transitions);
  }
}
