package typestate;

import java.util.Set;

import heros.EdgeFunction;
import heros.edgefunc.AllBottom;
import heros.edgefunc.EdgeIdentity;
import ideal.edgefunction.AnalysisEdgeFunctions;
import ideal.flowfunctions.WrappedAccessGraph;
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
  public EdgeFunction<TypestateDomainValue> getNormalEdgeFunction(WrappedAccessGraph d1, Unit curr,
      WrappedAccessGraph currNode, Unit succ, WrappedAccessGraph succNode) {
    return EdgeIdentity.v();
  }

  @Override
  public EdgeFunction<TypestateDomainValue> getCallEdgeFunction(WrappedAccessGraph callerD1, Unit callSite,
      WrappedAccessGraph srcNode, SootMethod calleeMethod, WrappedAccessGraph destNode) {
    Set<? extends Transition> trans =
        func.getCallTransitionsFor(callerD1.getDelegate(), callSite, calleeMethod, srcNode.getDelegate(), destNode.getDelegate());
    if (trans.isEmpty())
      return EdgeIdentity.v();
    return new TransitionFunction(trans);
  }

  @Override
  public EdgeFunction<TypestateDomainValue> getReturnEdgeFunction(WrappedAccessGraph callerD1,
      Unit callSite, SootMethod calleeMethod, Unit exitStmt, WrappedAccessGraph exitNode, Unit returnSite,
      WrappedAccessGraph retNode) {

    Set<? extends Transition> trans = func.getReturnTransitionsFor(callerD1.getDelegate(), callSite, calleeMethod,
        exitStmt, exitNode.getDelegate(), returnSite, retNode.getDelegate());
    if (trans.isEmpty())
      return EdgeIdentity.v();
    return new TransitionFunction(trans);
  }

  @Override
  public EdgeFunction<TypestateDomainValue> getCallToReturnEdgeFunction(WrappedAccessGraph d1,
      Unit callSite, WrappedAccessGraph callNode, Unit returnSite, WrappedAccessGraph returnSideNode) {
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
