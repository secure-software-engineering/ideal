package ideal;

import java.util.Map;
import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.EdgeFunctions;
import heros.Flow;
import heros.FlowFunctions;
import heros.IDETabulationProblem;
import heros.InterproceduralCFG;
import heros.JoinLattice;
import heros.edgefunc.AllTop;
import heros.solver.IDEDebugger;
import heros.solver.IPropagationController;
import heros.solver.Scheduler;
import ideal.edgefunction.AnalysisEdgeFunctions;
import ideal.edgefunction.ForwardEdgeFunctions;
import ideal.flowfunctions.ForwardFlowFunctions;
import ideal.pointsofaliasing.ReturnEvent;
import soot.SootMethod;
import soot.Unit;

public class InternalAnalysisProblem<V> implements
    IDETabulationProblem<Unit, AccessGraph, SootMethod, V, InterproceduralCFG<Unit, SootMethod>> {

  private InterproceduralCFG<Unit, SootMethod> icfg;
  private PerSeedAnalysisContext<V> context;
  private AnalysisEdgeFunctions<V> edgeFunctions;
  private IPropagationController<Unit, AccessGraph> propagationController;
  private NonIdentityEdgeFlowHandler<V> nonIdentityEdgeFlowHandler;
  public final static AccessGraph ZERO = new AccessGraph(null, null){
	  public String toString(){
		  return "{ZERO}";
	  }
  };

  InternalAnalysisProblem(IDEALAnalysisDefinition<V> analysisDefinition, PerSeedAnalysisContext<V> context) {
    this.icfg = analysisDefinition.icfg();
    this.edgeFunctions = analysisDefinition.edgeFunctions();
    this.propagationController = analysisDefinition.propagationController();
    this.nonIdentityEdgeFlowHandler = analysisDefinition.nonIdentityEdgeFlowHandler();
    this.context = context;
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
  public FlowFunctions<Unit, AccessGraph, SootMethod> flowFunctions() {
    return new ForwardFlowFunctions<V>(context);
  }

  @Override
  public InterproceduralCFG<Unit, SootMethod> interproceduralCFG() {
    return icfg;
  }

  @Override
  public Map<Unit, Set<AccessGraph>> initialSeeds() {
    return null;
  }

  @Override
  public AccessGraph zeroValue() {
    return ZERO;
  }

  @Override
  public EdgeFunctions<Unit, AccessGraph, SootMethod, V> edgeFunctions() {
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

	@Override
	public boolean recordEdges() {
		return false;
	}

	@Override
	public IDEDebugger<Unit, AccessGraph, SootMethod, V, InterproceduralCFG<Unit, SootMethod>> getDebugger() {
		return context.debugger();
	}

	@Override
	public Flow<Unit,AccessGraph,V> flowWrapper() {
		return new Flow<Unit,AccessGraph,V>(){


			@Override
			public void nonIdentityCallToReturnFlow( AccessGraph d2,Unit callSite, AccessGraph d3, Unit returnSite,
					AccessGraph d1, EdgeFunction<V> func) {
				//TODO search for aliases and update results.
				InternalAnalysisProblem.this.nonIdentityEdgeFlowHandler.onCallToReturnFlow(d2,callSite,d3,returnSite,d1,func);
			}

			@Override
			public void nonIdentityReturnFlow(Unit exitStmt,AccessGraph d2, Unit callSite, AccessGraph d3, Unit returnSite,
					AccessGraph d1, EdgeFunction<V> func) {
				InternalAnalysisProblem.this.nonIdentityEdgeFlowHandler.onReturnFlow(d2,callSite,d3,returnSite,d1,func);
				if(!context.isInIDEPhase())
					context.addPOA(new ReturnEvent<V>(exitStmt,d2, callSite, d3, returnSite, d1, func));
			}};
	}

	@Override
	public Scheduler getScheduler() {
		return context.scheduler;
	}

	@Override
	public IPropagationController<Unit, AccessGraph> propagationController() {
		return propagationController;
	}

}
