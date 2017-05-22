package ideal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.WrappedSootField;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.debug.IDebugger;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.util.queue.QueueReader;

public class Analysis<V> {

	public static boolean ENABLE_STATIC_FIELDS = true;
	public static boolean ALIASING_FOR_STATIC_FIELDS = false;
	public static boolean SEED_IN_APPLICATION_CLASS_METHOD = false;

	private final IDebugger<V> debugger;
	private final IInfoflowCFG icfg;
	protected final IDEALAnalysisDefinition<V> analysisDefinition;

	public Analysis(IDEALAnalysisDefinition<V> analysisDefinition) {
		this.analysisDefinition = analysisDefinition;
		this.icfg = analysisDefinition.icfg();
		this.debugger = analysisDefinition.debugger();
	}

	public void run() {
		printOptions();
		WrappedSootField.TRACK_TYPE = false;
		WrappedSootField.TRACK_STMT = false;
		Set<FactAtStatement> initialSeeds = computeSeeds();
		if (initialSeeds.isEmpty())
			System.err.println("No seeds found!");
		else
			System.err.println("Analysing " + initialSeeds.size() + " seeds!");
		debugger.beforeAnalysis();
		for (FactAtStatement seed : initialSeeds) {
			analysisForSeed(seed);
		}
		debugger.afterAnalysis();
	}

	public void analysisForSeed(FactAtStatement seed){
		new PerSeedAnalysisContext<>(analysisDefinition, seed).run();
	}
	
	private void printOptions() {
		System.out.println(analysisDefinition);
	}

	public Set<FactAtStatement> computeSeeds() {
		Set<FactAtStatement> seeds = new HashSet<>();
		ReachableMethods rm = Scene.v().getReachableMethods();
		QueueReader<MethodOrMethodContext> listener = rm.listener();
		while (listener.hasNext()) {
			MethodOrMethodContext next = listener.next();
			seeds.addAll(computeSeeds(next.method()));
		}
		return seeds;
	}

	private Collection<FactAtStatement> computeSeeds(SootMethod method) {
		Set<FactAtStatement> seeds = new HashSet<>();
		if (!method.hasActiveBody())
			return seeds;
		if (SEED_IN_APPLICATION_CLASS_METHOD && !method.getDeclaringClass().isApplicationClass())
			return seeds;
		for (Unit u : method.getActiveBody().getUnits()) {
			Collection<SootMethod> calledMethods = (icfg.isCallStmt(u) ? icfg.getCalleesOfCallAt(u)
					: new HashSet<SootMethod>());
			for (AccessGraph fact : analysisDefinition.generate(method, u, calledMethods)) {
				seeds.add(new FactAtStatement(u,fact));
			}
		}
		return seeds;
	}

}
