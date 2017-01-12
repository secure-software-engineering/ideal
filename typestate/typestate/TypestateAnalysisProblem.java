package typestate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Table.Cell;

import boomerang.BoomerangContext;
import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.AnalysisProblem;
import ideal.AnalysisSolver;
import ideal.InternalAnalysisProblem;
import ideal.edgefunction.AnalysisEdgeFunctions;
import ideal.flowfunctions.WrappedAccessGraph;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;
import typestate.finiteautomata.Transition;

public class TypestateAnalysisProblem implements AnalysisProblem<TypestateDomainValue> {

  private TypestateChangeFunction func;
  private Set<Cell<SootMethod,AccessGraph,TypestateDomainValue>> errorPathEdges = new HashSet<>();
  private ResultCollection<TypestateDomainValue> endingPathsOfPropagation = new ResultCollection<>(new Join<TypestateDomainValue>(){

	@Override
	public TypestateDomainValue join(TypestateDomainValue t1, TypestateDomainValue t2) {
		Set<Transition> transitions = t1.getTransitions();
		Set<Transition> transitions2 = t2.getTransitions();
		transitions.addAll(transitions2);
		return new TypestateDomainValue(transitions);
	}});

  public TypestateAnalysisProblem(TypestateChangeFunction func) {
    this.func = func;
  }

  @Override
  public AnalysisEdgeFunctions<TypestateDomainValue> edgeFunctions() {
    return new TypestateEdgeFunctions(func);
  }
 
  @Override
	public void onAnalysisFinished(PathEdge<Unit, WrappedAccessGraph> seed,
			AnalysisSolver<TypestateDomainValue> solver) {
    ReachableMethods rm = Scene.v().getReachableMethods();
    QueueReader<MethodOrMethodContext> listener = rm.listener();
    while (listener.hasNext()) {
      MethodOrMethodContext next = listener.next();
      SootMethod method = next.method();
      if (!method.hasActiveBody())
        continue;

      
      Collection<Unit> endPointsOf = solver.icfg().getEndPointsOf(method);

      for (Unit eP : endPointsOf) {
        Set<WrappedAccessGraph> localsAtEndPoint = new HashSet<>();
        for (Cell<WrappedAccessGraph, WrappedAccessGraph, EdgeFunction<TypestateDomainValue>> cell : solver
            .getPathEdgesAt(eP)) {
          if (!cell.getRowKey().equals(InternalAnalysisProblem.ZERO)) {
            continue;
          }
          localsAtEndPoint.add(cell.getColumnKey());
        }
        boolean escapes = false;
        for (WrappedAccessGraph ag : localsAtEndPoint) {
          if (BoomerangContext.isParameterOrThisValue(method, ag.getBase())) {
            escapes = true;
          }
        }
        if (!escapes) {
          Map<WrappedAccessGraph, TypestateDomainValue> resultAt = solver.resultsAt(eP);
          for (Entry<WrappedAccessGraph, TypestateDomainValue> fact : resultAt.entrySet()) {
            if (localsAtEndPoint.contains(fact.getKey())) {
              if (!fact.getValue().equals(solver.bottom()))
                endingPathsOfPropagation
                    .put(method, fact.getKey().getDelegate(), fact.getValue());
            }
          }
        }

      }
    }
    for (Cell<SootMethod, AccessGraph, TypestateDomainValue> res : endingPathsOfPropagation) {
      if (res.getValue().endsInErrorState()) {
        errorPathEdges.add(res);
      }
    }
  }

  @Override
  public Collection<Pair<AccessGraph, EdgeFunction<TypestateDomainValue>>> generate(Unit stmt,
      Collection<SootMethod> optional) {
    return func.generate(stmt, optional);
  }

  public Set<Cell<SootMethod, AccessGraph, TypestateDomainValue>> getErrors() {
    return errorPathEdges;
  }

  public ResultCollection<TypestateDomainValue> getPathEdgesAtEndOfMethods() {
    return endingPathsOfPropagation;
  }

  @Override
  public boolean isInErrorState() {
    return !errorPathEdges.isEmpty();
  }

}
