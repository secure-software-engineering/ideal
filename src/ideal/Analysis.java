package ideal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.debug.IDebugger;
import ideal.debug.NullDebugger;
import ideal.edgefunction.AnalysisEdgeFunctions;
import ideal.flowfunctions.WrappedAccessGraph;
import ideal.pointsofaliasing.PointOfAlias;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.BackwardsInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

public class Analysis<V> {
	
/**
 * Specifies the budget per seed in milliseconds.
 */
  public static long BUDGET = 30000;
  
  /**
   * Specifies the budget per alias query in milliseconds.
   */
  public static long ALIAS_BUDGET = 1000;
  
  private final IDebugger<V> debugger;
  private static Stopwatch START_TIME;
  private AnalysisContext<V> context;
  private Set<PathEdge<Unit, WrappedAccessGraph>> initialSeeds = new HashSet<>();
  private Set<PointOfAlias<V>> seenPOA = new HashSet<>();
  private Map<PathEdge<Unit, WrappedAccessGraph>, EdgeFunction<V>> seedToInitivalValue = new HashMap<>();
  private final IInfoflowCFG icfg;
  protected final AnalysisProblem<V> problem;
  private final AnalysisEdgeFunctions<V> edgeFunc;
  private BackwardsInfoflowCFG bwicfg;

  public Analysis(AnalysisProblem<V> problem, IInfoflowCFG icfg) {
    this.edgeFunc = problem.edgeFunctions();
    this.problem = problem;
    this.icfg = icfg;
    this.bwicfg = new BackwardsInfoflowCFG(icfg);
    this.debugger = new NullDebugger<V>();
  }

  public Analysis(AnalysisProblem<V> problem, IInfoflowCFG icfg, IDebugger<V> debugger) {
    this.edgeFunc = problem.edgeFunctions();
    this.problem = problem;
    this.icfg = icfg;
    this.bwicfg = new BackwardsInfoflowCFG(icfg);
    this.debugger = debugger;
  }
  public void run() {
    initialSeeds = computeSeeds();
    debugger.computedSeeds(seedToInitivalValue);
    debugger.beforeAnalysis();
    for (PathEdge<Unit, WrappedAccessGraph> seed : initialSeeds) {
      analysisForSeed(seed);
    }
    debugger.afterAnalysis();
  }


  private void analysisForSeed(final PathEdge<Unit, WrappedAccessGraph> seed) {
    boolean timeout = false;
    debugger.startWithSeed(seed);
    timeout = false;
    context = new AnalysisContext<>(icfg,bwicfg, edgeFunc, debugger);
    START_TIME = Stopwatch.createStarted();
    AnalysisSolver<V> solver = new AnalysisSolver<>(context.icfg(), context, edgeFunc);
    context.setSolver(solver);
    boolean isInErrorState = false;
    try {
      phase1(seed, solver);
      solver.destroy();
      solver = new AnalysisSolver<>(context.icfg(), context, edgeFunc);
      context.setSolver(solver);
      phase2(seed, solver);
      problem.onAnalysisFinished(seed,solver);
      isInErrorState = problem.isInErrorState();
    } catch (AnalysisTimeoutException e) {
      isInErrorState = true;
      debugger.onAnalysisTimeout(seed);
    } catch (RuntimeException e) {
      e.printStackTrace();
      isInErrorState = true;
    }
    debugger.finishWithSeed(seed, timeout, isInErrorState, solver);
    context.destroy();
    solver.destroy();
  }

  private void phase1(PathEdge<Unit, WrappedAccessGraph> seed, AnalysisSolver<V> solver) {
    debugger.startPhase1WithSeed(seed, solver);
    Set<PathEdge<Unit, WrappedAccessGraph>> worklist = new HashSet<>();
    if(icfg.isExitStmt(seed.getTarget()) || icfg.isCallStmt(seed.getTarget())){
    	worklist.add(seed);
    } else{
	    for(Unit u : icfg.getSuccsOf(seed.getTarget())){
	    	worklist.add(new PathEdge<Unit, WrappedAccessGraph>(seed.factAtSource(),u,seed.factAtTarget()));
	    }
    }
    while (!worklist.isEmpty()) {
      debugger.startForwardPhase(worklist);
      for (PathEdge<Unit, WrappedAccessGraph> s : worklist) {
        solver.injectPhase1Seed(s.factAtSource(), s.getTarget(), s.factAtTarget());
      }
      worklist.clear();
      Set<PointOfAlias<V>> pointsOfAlias = context.getAndClearPOA();
      debugger.startAliasPhase(pointsOfAlias);
      for (PointOfAlias<V> p : pointsOfAlias) {
        if (seenPOA.contains(p))
          continue;
        seenPOA.add(p);
        debugger.solvePOA(p);
        worklist.addAll(p.getPathEdges(context));
        Analysis.checkTimeout();
      }
    }
    debugger.finishPhase1WithSeed(seed, solver);
  }

  private void phase2(PathEdge<Unit, WrappedAccessGraph> s, AnalysisSolver<V> solver) {
    debugger.startPhase2WithSeed(s, solver);
    context.enableIDEPhase();
    if(icfg.isExitStmt(s.getTarget())){
    	solver.injectPhase2Seed(s.factAtSource(),s.getTarget(), s.factAtTarget(),
    	        seedToInitivalValue.get(s), context);
    } else{
	    for(Unit u : icfg.getSuccsOf(s.getTarget())){
	    	solver.injectPhase2Seed(s.factAtSource(),u, s.factAtTarget(),
	    	        seedToInitivalValue.get(s), context);
	    }
    }
    for(Unit u : icfg.getSuccsOf(s.getTarget())){
    	solver.injectPhase2Seed(s.factAtSource(),u, s.factAtTarget(),
    	        seedToInitivalValue.get(s), context);
    }
    solver.runExecutorAndAwaitCompletion();
    solver.computeValues(s);
    debugger.finishPhase2WithSeed(s, solver);
  }

  private Set<PathEdge<Unit, WrappedAccessGraph>> computeSeeds() {
    Set<PathEdge<Unit, WrappedAccessGraph>> seeds = new HashSet<>();
    ReachableMethods rm = Scene.v().getReachableMethods();
    QueueReader<MethodOrMethodContext> listener = rm.listener();
    while (listener.hasNext()) {
      MethodOrMethodContext next = listener.next();
      seeds.addAll(computeSeeds(next.method()));
    }
    return seeds;
  }

  private Collection<? extends PathEdge<Unit, WrappedAccessGraph>> computeSeeds(SootMethod method) {
    Set<PathEdge<Unit, WrappedAccessGraph>> seeds = new HashSet<>();

    if (!method.hasActiveBody())
      return seeds;
    for (Unit u : method.getActiveBody().getUnits()) {
      Collection<SootMethod> calledMethods =
          (icfg.isCallStmt(u) ? icfg.getCalleesOfCallAt(u) : new HashSet<SootMethod>());
        for (Pair<AccessGraph, EdgeFunction<V>> fact : problem.generate(u, calledMethods)) {
          PathEdge<Unit, WrappedAccessGraph> pathEdge =
              new PathEdge<Unit, WrappedAccessGraph>(InternalAnalysisProblem.ZERO, u, new WrappedAccessGraph(fact.getO1()));
          seeds.add(pathEdge);
          seedToInitivalValue.put(pathEdge, fact.getO2());
        }
    }
    return seeds;

  }



  public static void checkTimeout() {
    if ((Analysis.START_TIME.elapsed(TimeUnit.MILLISECONDS)) > Analysis.BUDGET)
      throw new AnalysisTimeoutException();
  }
}
