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

import com.google.common.base.Joiner;

import boomerang.AliasResults;
import heros.EdgeFunction;
import heros.solver.Pair;
import heros.solver.PathEdge;
import ideal.AnalysisSolver;
import ideal.flowfunctions.WrappedAccessGraph;
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

	private File jsonFile;
	private Map<SootMethod, ExplodedSuperGraph> methodToCfg = new HashMap<>();
	private Map<Object, Integer> objectToInteger = new HashMap<>();
	private IInfoflowCFG icfg;
	private Integer mainMethodId;
	private HashMap<ESGNode,V> esgNodeToLatticeVal = new HashMap<>();
	private static int esgNodeCounter = 0;
	
	
	public JSONDebugger(File file, IInfoflowCFG icfg) {
		this.jsonFile = file;
		this.icfg = icfg;
	}
	@Override
	public void computedSeeds(Map<PathEdge<Unit, WrappedAccessGraph>, EdgeFunction<V>> seedToInitivalValue) {

	}

	@Override
	public void beforeAnalysis() {

	}
	
	
	
	@Override
	public void addSummary(SootMethod methodToSummary, PathEdge<Unit, WrappedAccessGraph> summary) {
		for (Unit callSite : icfg.getCallersOf(methodToSummary)) {
			ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(callSite));
			for(Unit start : icfg.getStartPointsOf(methodToSummary)){
				cfg.addSummary(new ESGNode(start, summary.factAtSource()),
						new ESGNode(summary.getTarget(), summary.factAtTarget()));
			}
		}
	}

	@Override
	public void normalFlow(Unit start, WrappedAccessGraph startFact, Unit target, WrappedAccessGraph targetFact) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		cfg.addEdge(
				new ESGEdge(new ESGNode(start, startFact), new ESGNode(target, targetFact), "normalFlow"));
	}

	private ExplodedSuperGraph generateCFG(SootMethod sootMethod) {
		ExplodedSuperGraph cfg = methodToCfg.get(sootMethod);
		if (cfg == null) {
			cfg = new ExplodedSuperGraph(sootMethod);
			methodToCfg.put(sootMethod, cfg);
		}
		return cfg;
	}

	@Override
	public void callFlow(Unit start, WrappedAccessGraph startFact, Unit target, WrappedAccessGraph targetFact) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		ESGNode callSiteNode = new ESGNode(start, startFact);
		CalleeESGNode calleeNode = new CalleeESGNode(target,targetFact, callSiteNode);
		cfg.addEdge(new ESGEdge(callSiteNode, calleeNode, "callFlow"));
	}

	@Override
	public void callToReturn(Unit start, WrappedAccessGraph startFact, Unit target, WrappedAccessGraph targetFact) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(start));
		cfg.addEdge(new ESGEdge(new ESGNode(start, startFact), new ESGNode(target, targetFact),
				"call2ReturnFlow"));
	}

	@Override
	public void returnFlow(Unit start, WrappedAccessGraph startFact, Unit target, WrappedAccessGraph targetFact) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(target));
		ESGNode nodeInMethod = new ESGNode(target, targetFact);
		cfg.addEdge(new ESGEdge(new CalleeESGNode(start, startFact, nodeInMethod), nodeInMethod, "returnFlow"));
	}

	@Override
	public void indirectFlowAtCall(WrappedAccessGraph source, Unit curr, WrappedAccessGraph target) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf( curr));
		cfg.addEdge(new ESGEdge(new ESGNode(curr, source), new ESGNode(curr, target), "indirectFlow"));
	}
	
	@Override
	public void indirectFlowAtWrite(WrappedAccessGraph source, Unit curr, WrappedAccessGraph target) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf( curr));
		cfg.addEdge(new ESGEdge(new ESGNode(curr, source), new ESGNode(curr, target), "indirectFlow"));
	}

	@Override
	public void killAsOfStrongUpdate(WrappedAccessGraph d1, Unit callSite, WrappedAccessGraph callNode,
			Unit returnSite, WrappedAccessGraph returnSiteNode) {
		ExplodedSuperGraph cfg = generateCFG(icfg.getMethodOf(callSite));
		cfg.addTopEdge(new ESGEdge(new ESGNode(callSite, callNode), new ESGNode(returnSite, returnSiteNode), "topEdge"));
	}
	@Override
	public void setValue(Unit start, WrappedAccessGraph startFact, V value) {
		esgNodeToLatticeVal.put(new ESGNode(start,startFact),value);
	}

	@Override
	public void startWithSeed(PathEdge<Unit, WrappedAccessGraph> seed) {

	}

	@Override
	public void startPhase1WithSeed(PathEdge<Unit, WrappedAccessGraph> seed, AnalysisSolver<V> solver) {

	}

	@Override
	public void startPhase2WithSeed(PathEdge<Unit, WrappedAccessGraph> s, AnalysisSolver<V> solver) {
	}

	@Override
	public void finishPhase1WithSeed(PathEdge<Unit, WrappedAccessGraph> seed, AnalysisSolver<V> solver) {

	}

	@Override
	public void finishPhase2WithSeed(PathEdge<Unit, WrappedAccessGraph> s, AnalysisSolver<V> solver) {

	}

	@Override
	public void finishWithSeed(PathEdge<Unit, WrappedAccessGraph> seed, boolean timeout, boolean isInErrorState, AnalysisSolver<V> solver) {

	}

	@Override
	public void afterAnalysis() {
		writeToFile();
	}

	@Override
	public void startAliasPhase(Set<PointOfAlias<V>> pointsOfAlias) {

	}

	@Override
	public void startForwardPhase(Set<PathEdge<Unit, WrappedAccessGraph>> worklist) {

	}

	@Override
	public void onAliasesComputed(WrappedAccessGraph boomerangAccessGraph, Unit curr, WrappedAccessGraph d1,
			AliasResults res) {

	}

	@Override
	public void onAliasTimeout(WrappedAccessGraph boomerangAccessGraph, Unit curr, WrappedAccessGraph d1) {

	}

	@Override
	public void beforeAlias(WrappedAccessGraph boomerangAccessGraph, Unit curr, WrappedAccessGraph d1) {

	}


	@Override
	public void detectedStrongUpdate(Unit callSite, WrappedAccessGraph receivesUpdate) {

	}

	@Override
	public void onAnalysisTimeout(PathEdge<Unit, WrappedAccessGraph> seed) {

	}

	@Override
	public void solvePOA(PointOfAlias<V> p) {

	}

	@Override
	public void onNormalPropagation(WrappedAccessGraph sourceFact, Unit curr, Unit succ,WrappedAccessGraph d2) {

	}

	private static class CalleeESGNode extends ESGNode {

		private ESGNode linkedNode;

		CalleeESGNode(Unit u, WrappedAccessGraph a, ESGNode linkedNode) {
			super(u, a);
			this.linkedNode = linkedNode;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((linkedNode == null) ? 0 : linkedNode.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			CalleeESGNode other = (CalleeESGNode) obj;
			if (linkedNode == null) {
				if (other.linkedNode != null)
					return false;
			} else if (!linkedNode.equals(other.linkedNode))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "CalleeESGNode "+ super.toString() + " linked to "+ linkedNode;
		}
	}

	private static class ESGNode {
		Unit u;
		WrappedAccessGraph a;

		ESGNode(Unit u, WrappedAccessGraph a) {
			this.u = u;
			this.a = a;
			esgNodeCounter++;
			if (esgNodeCounter % 1000 == 0) {
				System.err.println("Warning: Using JSONOutputDebugger, might slow down performance.");
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((u == null) ? 0 : u.hashCode());

			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ESGNode other = (ESGNode) obj;
			if (a == null) {
				if (other.a != null)
					return false;
			} else if (!a.equals(other.a))
				return false;
			if (u == null) {
				if (other.u != null)
					return false;
			} else if (!u.equals(other.u))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return a +" @ "+ u;
		}
	}

	private class ESGEdge {
		private ESGNode start;
		private ESGNode target;
		private String type;

		public ESGEdge(ESGNode start, ESGNode target, String type) {
			this.start = start;
			this.target = target;
			this.type = type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((start == null) ? 0 : start.hashCode());
			result = prime * result + ((target == null) ? 0 : target.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ESGEdge other = (ESGEdge) obj;
			if (start == null) {
				if (other.start != null)
					return false;
			} else if (!start.equals(other.start))
				return false;
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target))
				return false;
			return true;
		}
	}

	public Integer id(Object u) {
		if (objectToInteger.get(u) != null)
			return objectToInteger.get(u);
		int size = objectToInteger.size() + 1;
		objectToInteger.put(u, size);
		return size;
	}

	private class ExplodedSuperGraph {
		private SootMethod method;
		private LinkedList<WrappedAccessGraph> facts = new LinkedList<>();
		private LinkedList<ESGNode> nodes = new LinkedList<>();
		private LinkedList<CalleeESGNode> calleeNodes = new LinkedList<>();
		private LinkedList<ESGEdge> edges = new LinkedList<>();
		private LinkedList<ESGEdge> topEdges = new LinkedList<>();
		private Set<Pair<ESGNode, ESGNode>> summaries = new HashSet<>();

		ExplodedSuperGraph(SootMethod m) {
			this.method = m;
		}

		public void addTopEdge(JSONDebugger<V>.ESGEdge esgEdge) {
			addEdge(esgEdge);
			topEdges.add(esgEdge);
		}

		public void addSummary(ESGNode start, ESGNode target) {
			summaries.add(new Pair<ESGNode, ESGNode>(start, target));
		}

		void addNode(ESGNode g) {
			if (!nodes.contains(g))
				nodes.add(g);
			if (!facts.contains(g.a) && !(g instanceof CalleeESGNode))
				facts.add(g.a);
			if (g instanceof CalleeESGNode)
				calleeNodes.add((CalleeESGNode) g);
		}

		void addEdge(ESGEdge g) {
			addNode(g.start);
			addNode(g.target);
			if (!edges.contains(g))
				edges.add(g);
		}

		private JSONObject toJSONObject() {
			linkSummaries();
			JSONObject o = new JSONObject();
			o.put("methodName", StringEscapeUtils.escapeHtml4(method.toString()));
			o.put("methodId", id(method));
			JSONArray data = new JSONArray();
			LinkedList<Unit> stmtsList = new LinkedList<>();
			int offset = 0;
			int labelYOffset = 0;
			int charSize = 8;
			for (WrappedAccessGraph g : facts) {
				labelYOffset = Math.max(labelYOffset, charSize * g.toString().length());
			}
			int index = 0;
			for (Unit u : method.getActiveBody().getUnits()) {
				
				JSONObject nodeObj = new JSONObject();
				JSONObject pos = new JSONObject();
				stmtsList.add(u);
				pos.put("x", 10);
				pos.put("y", stmtsList.size() * 30 + labelYOffset);
				nodeObj.put("position", pos);
				JSONObject label = new JSONObject();
				label.put("label", u.toString());
				label.put("shortLabel", getShortLabel(u));
				if (icfg.isCallStmt(u)) {
					label.put("callSite", icfg.isCallStmt(u));
					JSONArray callees = new JSONArray();
					for (SootMethod callee : icfg.getCalleesOfCallAt(u))
						callees.add(new Method(callee));
					label.put("callees", callees);
				}
				if (icfg.isExitStmt(u)) {
					label.put("returnSite", icfg.isExitStmt(u));
					JSONArray callees = new JSONArray();
					Set<SootMethod> callers = new HashSet<>();
					for (Unit callsite : icfg.getCallersOf(icfg.getMethodOf(u)))
						callers.add(icfg.getMethodOf(callsite));

					for (SootMethod caller : callers)
						callees.add(new Method(caller));
					label.put("callers", callees);
				}
				label.put("stmtId", id(u));
				label.put("id", "stmt" + id(u));

				label.put("stmtIndex", index);
				index++;

				nodeObj.put("data", label);
				nodeObj.put("classes", "stmt label " + (icfg.isExitStmt(u) ? " returnSite " :" ")+ (icfg.isCallStmt(u) ? " callSite " :" "));
				data.add(nodeObj);
				offset = Math.max(offset, getShortLabel(u).toString().length());
				
				for(Unit succ : icfg.getSuccsOf(u)){
					JSONObject cfgEdgeObj = new JSONObject();
					JSONObject dataEntry = new JSONObject();
					dataEntry.put("source", "stmt" + id(u));
					dataEntry.put("target", "stmt" + id(succ));
					dataEntry.put("directed", "true");
					cfgEdgeObj.put("data", dataEntry);
					cfgEdgeObj.put("classes", "cfgEdge label method" + id(method));
					data.add(cfgEdgeObj);
				}
			}

			LinkedList<WrappedAccessGraph> factsList = new LinkedList<>();

			for (WrappedAccessGraph u : facts) {
				JSONObject nodeObj = new JSONObject();
				JSONObject pos = new JSONObject();
				factsList.add(u);
				pos.put("x", factsList.size() * 30 + offset * charSize);
				pos.put("y", labelYOffset);
				nodeObj.put("position", pos);
				JSONObject label = new JSONObject();
				label.put("label", u.toString());
				label.put("factId", id(u));
				nodeObj.put("classes", "fact label");
				nodeObj.put("data", label);
				data.add(nodeObj);
			}

			for (ESGNode node : nodes) {
				JSONObject nodeObj = new JSONObject();
				JSONObject pos = new JSONObject();
				if (node instanceof CalleeESGNode) {
					CalleeESGNode calleeESGNode = (CalleeESGNode) node;
					pos.put("x", (factsList.indexOf(calleeESGNode.linkedNode.a) + 1) * 30 + 10 + offset * charSize);
					pos.put("y", (stmtsList.indexOf(calleeESGNode.linkedNode.u)) * 30 + labelYOffset);
				} else {
					assert stmtsList.indexOf(node.u) != -1;
					pos.put("x", (factsList.indexOf(node.a) + 1) * 30 + offset * charSize);
					pos.put("y",
							(stmtsList.indexOf(node.u)) * 30 + labelYOffset);
				}

				nodeObj.put("position", pos);
				String classes = "esgNode method" + id(method) + "  ";

				JSONObject additionalData = new JSONObject();
				additionalData.put("id", "n" + id(node));
				additionalData.put("stmtId", id(node.u));
				additionalData.put("factId", id(node.a));
				if(esgNodeToLatticeVal.get(node) != null)
					additionalData.put("ideValue",StringEscapeUtils.escapeHtml4(esgNodeToLatticeVal.get(node).toString()));
				nodeObj.put("classes", classes);
				nodeObj.put("group", "nodes");
				nodeObj.put("data", additionalData);

				data.add(nodeObj);
			}
			for (ESGEdge edge : edges) {
				JSONObject nodeObj = new JSONObject();
				JSONObject dataEntry = new JSONObject();
				dataEntry.put("id", "e" + id(edge));
				dataEntry.put("source", "n" + id(edge.start));
				dataEntry.put("target", "n" + id(edge.target));
				dataEntry.put("directed", "true");
				nodeObj.put("data", dataEntry);
				nodeObj.put("classes", "esgEdge method" + id(method) + " " + edge.type+ " " +(topEdges.contains(edge)? " TOP " : ""));
				nodeObj.put("group", "edges");
				data.add(nodeObj);
			}
			o.put("data", data);
			return o;
		}

		private void linkSummaries() {
			for (Pair<ESGNode, ESGNode> p : summaries) {
				ESGNode start = p.getO1();
				ESGNode target = p.getO2();
				Set<CalleeESGNode> starts = new HashSet<>();
				Set<CalleeESGNode> targets = new HashSet<>();
				for (CalleeESGNode n : calleeNodes) {
					if (n.a.equals(start.a) && (n.u.equals(start.u)))
						starts.add(n);
					if (n.a.equals(target.a) &&(n.u.equals(target.u)))
						targets.add(n);
				}
				for (CalleeESGNode summaryStart : starts) {
					for (CalleeESGNode summaryTarget : targets) {
							if (icfg.getSuccsOf(summaryStart.linkedNode.u).contains(summaryTarget.linkedNode.u))
								addEdge(new ESGEdge(summaryStart, summaryTarget, "summaryFlow"));
					}
				}
			}
		}

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

	private void writeToFile() {
		try (FileWriter file = new FileWriter(jsonFile)) {
			List<String> stringList = new LinkedList<String>();
			List<String> methods = new LinkedList<String>();
			for (ExplodedSuperGraph c : methodToCfg.values()) {
				stringList.add(c.toJSONObject().toJSONString());
				methods.add(new Method(c.method).toJSONString());
			}
			file.write("var methods = [");
			file.write(Joiner.on(",\n").join(stringList));
			file.write("];\n");
			file.write("var methodList = [");
			file.write(Joiner.on(",\n").join(methods));
			file.write("];\n");

			file.write("var activeMethod = \"" + mainMethodId + "\";");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class Method extends JSONObject {

		Method(SootMethod m) {
			this.put("name", StringEscapeUtils.escapeHtml4(m.toString()));
			this.put("id", id(m));
		}

	}
}
