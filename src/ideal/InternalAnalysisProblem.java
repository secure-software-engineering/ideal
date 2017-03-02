package ideal;

import java.util.Map;
import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.EdgeFunctions;
import heros.FlowFunctions;
import heros.IDETabulationProblem;
import heros.InterproceduralCFG;
import heros.JoinLattice;
import heros.edgefunc.AllBottom;
import heros.edgefunc.AllTop;
import heros.solver.IDEDebugger;
import ideal.edgefunction.AnalysisEdgeFunctions;
import ideal.edgefunction.ForwardEdgeFunctions;
import ideal.flowfunctions.ForwardFlowFunctions;
import ideal.flowfunctions.WrappedAccessGraph;
import soot.SootMethod;
import soot.Unit;

public class InternalAnalysisProblem<V> implements
    IDETabulationProblem<Unit, WrappedAccessGraph, SootMethod, V, InterproceduralCFG<Unit, SootMethod>> {

  private InterproceduralCFG<Unit, SootMethod> icfg;
  private AnalysisContext<V> context;
  private AnalysisEdgeFunctions<V> edgeFunctions;
  public final static WrappedAccessGraph ZERO = new WrappedAccessGraph(new AccessGraph(null, null));

  InternalAnalysisProblem(InterproceduralCFG<Unit, SootMethod> icfg, AnalysisContext<V> context,
      AnalysisEdgeFunctions<V> edgeFunctions) {
    this.icfg = icfg;
    this.context = context;
    this.edgeFunctions = edgeFunctions;
  }

  @Override
  public boolean followReturnsPastSeeds() {
    return true;
  }

  @Override
  public boolean autoAddZero() {
    return false;
  }

  @Override
  public int numThreads() {
    return 1;
  }

  @Override
  public boolean computeValues() {
    return false;
  }

  @Override
  public FlowFunctions<Unit, WrappedAccessGraph, SootMethod> flowFunctions() {
    return new ForwardFlowFunctions<V>(context);
  }

  @Override
  public InterproceduralCFG<Unit, SootMethod> interproceduralCFG() {
    return icfg;
  }

  @Override
  public Map<Unit, Set<WrappedAccessGraph>> initialSeeds() {
    return null;
  }

  @Override
  public WrappedAccessGraph zeroValue() {
    return ZERO;
  }

  @Override
  public EdgeFunctions<Unit, WrappedAccessGraph, SootMethod, V> edgeFunctions() {
    return new ForwardEdgeFunctions<>(context, edgeFunctions);
  }

  @Override
  public JoinLattice<V> joinLattice() {
    return new JoinLattice<V>() {

      @Override
      public V topElement() {
        return edgeFunctions.top();
      }

      @Override
      public V bottomElement() {
        return edgeFunctions.bottom();
      }

      @Override
      public V join(V left, V right) {
        if (left == topElement() && right == topElement()) {
          return topElement();
        }
        if (left == bottomElement() && right == bottomElement()) {
          return bottomElement();
        }
        return edgeFunctions.join(left, right);
      }
    };
  }

  @Override
  public EdgeFunction<V> allTopFunction() {
    return new AllTop<V>(edgeFunctions.top());
  }

  public EdgeFunction<V> allBottomFunction() {
    return new AllBottom<V>(edgeFunctions.top());
  }

	@Override
	public boolean recordEdges() {
		return false;
	}

	@Override
	public IDEDebugger<Unit, WrappedAccessGraph, SootMethod, V, InterproceduralCFG<Unit, SootMethod>> getDebugger() {
		return context.debugger;
	}
}
