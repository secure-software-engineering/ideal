package ideal.debug;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;

import boomerang.AliasResults;
import boomerang.accessgraph.AccessGraph;
import heros.EdgeFunction;
import heros.debug.visualization.ExplodedSuperGraph;
import heros.debug.visualization.IDEToJSON;
import heros.debug.visualization.ExplodedSuperGraph.ESGNode;
import heros.debug.visualization.IDEToJSON.Direction;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.AnalysisSolver;
import ideal.pointsofaliasing.PointOfAlias;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class JSONDebugger<V> implements IDebugger<V> {

	private IDEToJSON<SootMethod, Unit, AccessGraph, V, IInfoflowCFG> ideToJSON;
	private IInfoflowCFG icfg;

	public JSONDebugger(File file, IInfoflowCFG icfg) {
		this.ideToJSON = new IDEToJSON<SootMethod, Unit, AccessGraph, V, IInfoflowCFG>(file, icfg){
			@Override
			public String getShortLabel(Unit u) {
				return JSONDebugger.this.getShortLabel(u);
			}
			@Override
			public List<Unit> getListOfStmts(SootMethod method) {
				return Lists.newLinkedList(method.getActiveBody().getUnits());
			}
		};
		this.icfg = icfg;
	}


	@Override
	public void beforeAnalysis() {

	}

	@Override
	public void addSummary(SootMethod methodToSummary, PathEdge<Unit, AccessGraph> summary) {
		for (Unit callSite : icfg.getCallersOf(methodToSummary)) {
			ExplodedSuperGraph cfg = getESG(callSite);
			for (Unit start : icfg.getStartPointsOf(methodToSummary)) {
				cfg.addSummary(cfg.new ESGNode(start, summary.factAtSource()), cfg.new ESGNode(summary.getTarget(),summary.factAtTarget()));
			}
		}
	}

	@Override
	public void normalFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		getESG(start).normalFlow(start, startFact, target, targetFact);
	}

	private ExplodedSuperGraph<SootMethod, Unit, AccessGraph, V> getESG(Unit unit) {
		return ideToJSON.getOrCreateESG(icfg.getMethodOf(unit), Direction.Forward);
	}

	@Override
	public void callFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		getESG(start).callFlow(start, startFact, target, targetFact);;
	}

	@Override
	public void callToReturn(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		getESG(start).callToReturn(start, startFact, target, targetFact);
	}

	@Override
	public void returnFlow(Unit start, AccessGraph startFact, Unit target, AccessGraph targetFact) {
		getESG(target).returnFlow(start, startFact, target, targetFact);
	}

	@Override
	public void indirectFlowAtCall(AccessGraph source, Unit curr, AccessGraph target) {
		// ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf( curr));
		// cfg.addEdge(new ESGEdge(new ESGNode(curr, source), new ESGNode(curr,
		// target), "indirectFlow"));
	}

	@Override
	public void indirectFlowAtWrite(AccessGraph source, Unit curr, AccessGraph target) {
		// ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf( curr));
		// cfg.addEdge(new ESGEdge(new ESGNode(curr, source), new ESGNode(curr,
		// target), "indirectFlow"));
	}

	@Override
	public void killAsOfStrongUpdate(AccessGraph d1, Unit callSite, AccessGraph callNode, Unit returnSite,
			AccessGraph returnSiteNode) {
//		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(callSite));
//		cfg.addTopEdge(
//				new ESGEdge(new ESGNode(callSite, callNode), new ESGNode(returnSite, returnSiteNode), "topEdge"));
	}

	@Override
	public void setValue(Unit start, AccessGraph startFact, V value) {
		getESG(start).setValue(getESG(start).new ESGNode(start, startFact), value);
	}

	@Override
	public void startWithSeed(PathEdge<Unit, AccessGraph> seed) {

	}

	@Override
	public void startPhase1WithSeed(PathEdge<Unit, AccessGraph> seed, AnalysisSolver<V> solver) {

	}

	@Override
	public void startPhase2WithSeed(PathEdge<Unit, AccessGraph> s, AnalysisSolver<V> solver) {
	}

	@Override
	public void finishPhase1WithSeed(PathEdge<Unit, AccessGraph> seed, AnalysisSolver<V> solver) {

	}

	@Override
	public void finishPhase2WithSeed(PathEdge<Unit, AccessGraph> s, AnalysisSolver<V> solver) {

	}

	@Override
	public void finishWithSeed(PathEdge<Unit, AccessGraph> seed, boolean timeout, boolean isInErrorState,
			AnalysisSolver<V> solver) {

	}

	@Override
	public void afterAnalysis() {
		ideToJSON.writeToFile();
	}

	@Override
	public void startAliasPhase(Set<PointOfAlias<V>> pointsOfAlias) {

	}

	@Override
	public void startForwardPhase(Set<PathEdge<Unit, AccessGraph>> worklist) {

	}

	@Override
	public void onAliasesComputed(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1, AliasResults res) {

	}

	@Override
	public void onAliasTimeout(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1) {

	}

	@Override
	public void beforeAlias(AccessGraph boomerangAccessGraph, Unit curr, AccessGraph d1) {

	}

	@Override
	public void detectedStrongUpdate(Unit callSite, AccessGraph receivesUpdate) {

	}

	@Override
	public void onAnalysisTimeout(PathEdge<Unit, AccessGraph> seed) {

	}

	@Override
	public void solvePOA(PointOfAlias<V> p) {

	}

	@Override
	public void onNormalPropagation(AccessGraph sourceFact, Unit curr, Unit succ, AccessGraph d2) {

	}

	private String getShortLabel(Unit u) {
		if (u instanceof AssignStmt) {
			AssignStmt assignStmt = (AssignStmt) u;
			if (assignStmt.getRightOp() instanceof InstanceFieldRef) {
				InstanceFieldRef fr = (InstanceFieldRef) assignStmt.getRightOp();
				return assignStmt.getLeftOp() + " = " + fr.getBase() + "." + fr.getField().getName();
			}
			if (assignStmt.getLeftOp() instanceof InstanceFieldRef) {
				InstanceFieldRef fr = (InstanceFieldRef) assignStmt.getLeftOp();
				return fr.getBase() + "." + fr.getField().getName() + " = " + assignStmt.getRightOp();
			}
		}
		if (u instanceof Stmt && ((Stmt) u).containsInvokeExpr()) {
			InvokeExpr invokeExpr = ((Stmt) u).getInvokeExpr();
			if (invokeExpr instanceof StaticInvokeExpr)
				return (u instanceof AssignStmt ? ((AssignStmt) u).getLeftOp() + " = " : "")
						+ invokeExpr.getMethod().getName() + "("
						+ invokeExpr.getArgs().toString().replace("[", "").replace("]", "") + ")";
			if (invokeExpr instanceof InstanceInvokeExpr) {
				InstanceInvokeExpr iie = (InstanceInvokeExpr) invokeExpr;
				return (u instanceof AssignStmt ? ((AssignStmt) u).getLeftOp() + " = " : "") + iie.getBase() + "."
						+ invokeExpr.getMethod().getName() + "("
						+ invokeExpr.getArgs().toString().replace("[", "").replace("]", "") + ")";
			}
		}
		return u.toString();
	}

}
