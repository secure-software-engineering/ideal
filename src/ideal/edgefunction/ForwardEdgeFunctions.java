package ideal.edgefunction;

import heros.EdgeFunction;
import heros.EdgeFunctions;
import heros.edgefunc.AllBottom;
import heros.edgefunc.AllTop;
import ideal.AnalysisContext;
import ideal.flowfunctions.WrappedAccessGraph;
import soot.SootMethod;
import soot.Unit;

public class ForwardEdgeFunctions<V> implements EdgeFunctions<Unit, WrappedAccessGraph, SootMethod, V> {

  private AnalysisContext<V> context;
  private final EdgeFunction<V> ALL_TOP;
  private final AnalysisEdgeFunctions<V> edgeFunctions;
  private final AllBottom<V> ALL_BOTTOM;

  public ForwardEdgeFunctions(AnalysisContext<V> context,
      AnalysisEdgeFunctions<V> edgeFunctions) {
    this.context = context;
    this.edgeFunctions = edgeFunctions;
    this.ALL_TOP = new AllTop<V>(edgeFunctions.top());
    this.ALL_BOTTOM = new AllBottom<V>(edgeFunctions.top());
  }

  @Override
  public EdgeFunction<V> getNormalEdgeFunction(WrappedAccessGraph d1, Unit curr,
      WrappedAccessGraph currNode, Unit succ, WrappedAccessGraph succNode) {
    if (!context.isInIDEPhase())
      return ALL_BOTTOM;

    if (context.isNullnessBranch(curr, succ, currNode)) {
      return ALL_TOP;
    }
    return edgeFunctions.getNormalEdgeFunction(d1, curr, currNode, succ, succNode);
  }

  @Override
  public EdgeFunction<V> getCallEdgeFunction(WrappedAccessGraph callerD1, Unit callSite,
      WrappedAccessGraph srcNode, SootMethod calleeMethod, WrappedAccessGraph destNode) {
    if (!context.isInIDEPhase())
      return ALL_BOTTOM;
    return edgeFunctions.getCallEdgeFunction(callerD1, callSite, srcNode, calleeMethod, destNode);
  }

  @Override
  public EdgeFunction<V> getReturnEdgeFunction(WrappedAccessGraph callerD1,
      Unit callSite, SootMethod calleeMethod, Unit exitStmt, WrappedAccessGraph exitNode, Unit returnSite,
      WrappedAccessGraph retNode) {
    if (!context.isInIDEPhase())
      return ALL_BOTTOM;
    return edgeFunctions.getReturnEdgeFunction(callerD1, callSite, calleeMethod, exitStmt, exitNode,
        returnSite,
        retNode);
  }

  @Override
  public EdgeFunction<V> getCallToReturnEdgeFunction(WrappedAccessGraph d1,
      Unit callSite, WrappedAccessGraph callNode, Unit returnSite, WrappedAccessGraph returnSideNode) {
    if (!context.isInIDEPhase())
      return ALL_BOTTOM;
    
    // Assign the top function to call-to-return flows where we know about a strong update.
    if (context.isStrongUpdate(callSite, returnSideNode)) {
      context.debugger.killAsOfStrongUpdate(d1, callSite, callNode, returnSideNode, returnSideNode);
      return ALL_TOP;
    }
    return edgeFunctions.getCallToReturnEdgeFunction(d1, callSite, callNode, returnSite,
        returnSideNode);
  }

}
