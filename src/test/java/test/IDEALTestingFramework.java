package test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.google.common.collect.Lists;

import boomerang.accessgraph.AccessGraph;
import boomerang.preanalysis.PreparationTransformer;
import ideal.Analysis;
import ideal.ResultReporter;
import soot.ArrayType;
import soot.Body;
import soot.G;
import soot.Local;
import soot.Modifier;
import soot.PackManager;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.options.Options;
import test.ExpectedResults.State;
import typestate.TypestateDomainValue;
@SuppressWarnings( "deprecation" )
public abstract class IDEALTestingFramework {
	private IInfoflowCFG icfg;
	@Rule
	public TestName name = new TestName();
	private SootMethod sootTestMethod;

	@Before
	public void performQuery() {
		initializeSootWithEntryPoint(name.getMethodName());
		analyze(name.getMethodName());
		// To never execute the @Test method...
		org.junit.Assume.assumeTrue(false);
	}

	  private Analysis<TypestateDomainValue> analysis;
	  protected long analysisTime;

	  protected abstract Analysis<TypestateDomainValue> createAnalysis(ResultReporter<TypestateDomainValue> reporter);

	  protected Analysis<TypestateDomainValue> getAnalysis(ResultReporter<TypestateDomainValue> reporter) {
	    if (analysis == null)
	      analysis = createAnalysis(reporter);
	    return analysis;
	  }

	private void analyze(final String methodName) {
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {

			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				icfg = new InfoflowCFG(new JimpleBasedInterproceduralCFG(true));
				Set<ExpectedResults> expectedResults = parseExpectedQueryResults(sootTestMethod);
		        IDEALTestingFramework.this.getAnalysis(new TestingResultReporter(expectedResults)).run();
		        List<ExpectedResults> unsound = Lists.newLinkedList();
		        List<ExpectedResults> imprecise = Lists.newLinkedList();
		        for(ExpectedResults r : expectedResults){
		        	if(!r.satisfied){
		        		unsound.add(r);
		        	}
		        }
		        for(ExpectedResults r : expectedResults){
		        	if(r.imprecise){
		        		imprecise.add(r);
		        	}
		        }
		        if(!unsound.isEmpty())
		        	throw new RuntimeException("Unsound results: " + unsound);
		        if(!imprecise.isEmpty())
		        	Assert.fail("Imprecise results: " + imprecise);
				try {
//					compareQuery(expectedResults, TestFramework.this.getAnalysis().getResults());
				} catch (AssertionError e) {
//					TestFramework.this.options.removeVizFile();
					throw e;
				}
//				TestFramework.this.options.removeVizFile();
			}


		});
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.prepare", new PreparationTransformer()));
		PackManager.v().getPack("wjtp").add(transform);
		PackManager.v().getPack("cg").apply();
		PackManager.v().getPack("wjtp").apply();
	}


	private Set<ExpectedResults> parseExpectedQueryResults(SootMethod sootTestMethod) {
		Set<ExpectedResults> results = new HashSet<>();
		parseExpectedQueryResults(sootTestMethod,results,new HashSet<SootMethod>());
		return results;
	}

	private void parseExpectedQueryResults(SootMethod m, Set<ExpectedResults> queries, Set<SootMethod> visited) {
		if (!m.hasActiveBody() || visited.contains(m))
			return;
		visited.add(m);
		Body activeBody = m.getActiveBody();
		for (Unit callSite : icfg.getCallsFromWithin(m)) {
			for (SootMethod callee : icfg.getCalleesOfCallAt(callSite))
				parseExpectedQueryResults(callee, queries, visited);
		}
		for (Unit u : activeBody.getUnits()) {
			if (!(u instanceof Stmt))
				continue;

			Stmt stmt = (Stmt) u;
			if (!(stmt.containsInvokeExpr()))
				continue;
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			String invocationName = invokeExpr.getMethod().getName();
			if (!invocationName.startsWith("mayBeIn") && !invocationName.startsWith("mustBeIn"))
				continue;
			Value param = invokeExpr.getArg(0);
			if (!(param instanceof Local))
				continue;
			Local queryVar = (Local) param;
			AccessGraph val = new AccessGraph(queryVar, queryVar.getType());
			if(invocationName.startsWith("mayBeIn")){
				if(invocationName.contains("Error"))
					queries.add(new MayBe(stmt,val,State.ERROR));
				else
					queries.add(new MayBe(stmt,val,State.ACCEPTING));
			} else if(invocationName.startsWith("mustBeIn")){
				if(invocationName.contains("Error"))
					queries.add(new MustBe(stmt,val,State.ERROR));
				else
					queries.add(new MustBe(stmt,val,State.ACCEPTING));
			}
		}
	}

	@SuppressWarnings("static-access")
	private void initializeSootWithEntryPoint(String methodName) {
		G.v().reset();
		Options.v().set_whole_program(true);
		Options.v().setPhaseOption("jb", "use-original-names:true");
		Options.v().setPhaseOption("cg.spark", "on");
		Options.v().setPhaseOption("cg.spark", "verbose:true");
		Options.v().set_output_format(Options.output_format_none);
		String userdir = System.getProperty("user.dir");
		String sootCp = userdir + "/target/test-classes";
		if (includeJDK()) {
			String javaHome = System.getProperty("java.home");
			if (javaHome == null || javaHome.equals(""))
				throw new RuntimeException("Could not get property java.home!");
			sootCp += File.pathSeparator + javaHome + "/lib/rt.jar";
			sootCp += File.pathSeparator + javaHome + "/lib/jce.jar";
			System.out.println(sootCp);
			Options.v().setPhaseOption("cg", "trim-clinit:false");
			Options.v().set_no_bodies_for_excluded(true);
			Options.v().set_allow_phantom_refs(true);

			List<String> includeList = new LinkedList<String>();
			includeList.add("java.lang.*");
			includeList.add("java.util.*");
			includeList.add("java.io.*");
			includeList.add("sun.misc.*");
			includeList.add("java.net.*");
			includeList.add("javax.servlet.*");
			includeList.add("javax.crypto.*");

			includeList.add("android.*");
			includeList.add("org.apache.http.*");

			includeList.add("de.test.*");
			includeList.add("soot.*");
			includeList.add("com.example.*");
			includeList.add("libcore.icu.*");
			includeList.add("securibench.*");
			Options.v().set_include(includeList);

		} else {
			Options.v().set_no_bodies_for_excluded(true);
			Options.v().set_allow_phantom_refs(true);
			// Options.v().setPhaseOption("cg", "all-reachable:true");
		}

		Options.v().set_exclude(excludedPackages());
		Options.v().set_soot_classpath(sootCp);
		// Options.v().set_main_class(this.getTargetClass());
		SootClass sootTestCaseClass = Scene.v().forceResolve(getTestCaseClassName(), SootClass.BODIES);

		for (SootMethod m : sootTestCaseClass.getMethods()) {
			if (m.getName().equals(methodName))
				sootTestMethod = m;
		}
		if (sootTestMethod == null)
			throw new RuntimeException("The method with name " + methodName + " was not found in the Soot Scene.");
		Scene.v().addBasicClass(getTargetClass(), SootClass.BODIES);
		Scene.v().loadNecessaryClasses();
		SootClass c = Scene.v().forceResolve(getTargetClass(), SootClass.BODIES);
		if (c != null) {
			c.setApplicationClass();
		}

		SootMethod methodByName = c.getMethodByName("main");
		List<SootMethod> ePoints = new LinkedList<>();
		ePoints.add(methodByName);
		Scene.v().setEntryPoints(ePoints);
	}

	private String getTargetClass() {
		SootClass sootClass = new SootClass("dummyClass");
		SootMethod mainMethod = new SootMethod("main",
				Arrays.asList(new Type[] { ArrayType.v(RefType.v("java.lang.String"), 1) }), VoidType.v(),
				Modifier.PUBLIC | Modifier.STATIC);
		sootClass.addMethod(mainMethod);
		JimpleBody body = Jimple.v().newBody(mainMethod);
		mainMethod.setActiveBody(body);
		RefType testCaseType = RefType.v(getTestCaseClassName());
		System.out.println(getTestCaseClassName());
		Local allocatedTestObj = Jimple.v().newLocal("dummyObj", testCaseType);
		body.getLocals().add(allocatedTestObj);
		body.getUnits().add(Jimple.v().newAssignStmt(allocatedTestObj, Jimple.v().newNewExpr(testCaseType)));
		body.getUnits().add(
				Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(allocatedTestObj, sootTestMethod.makeRef())));
		Scene.v().addClass(sootClass);
		return sootClass.toString();
	}

	private String getTestCaseClassName() {
		return this.getClass().getName().replace("class ", "");
	}

	protected boolean includeJDK() {
		return true;
	}

	public List<String> excludedPackages() {
		List<String> excludedPackages = new LinkedList<>();
		excludedPackages.add("java.*");
		excludedPackages.add("sun.*");
		excludedPackages.add("javax.*");
		excludedPackages.add("com.sun.*");
		excludedPackages.add("com.ibm.*");
		excludedPackages.add("org.xml.*");
		excludedPackages.add("org.w3c.*");
		excludedPackages.add("apple.awt.*");
		excludedPackages.add("com.apple.*");
		return excludedPackages;
	}

	/**
	 * The methods parameter describes the variable that a query is issued for.
	 * Note: We misuse the @Deprecated annotation to highlight the method in the
	 * Code.
	 */

	@Deprecated
	protected static void mayBeInErrorState(Object variable) {

	}

	@Deprecated
	protected static void mustBeInErrorState(Object variable) {

	}

	@Deprecated
	protected static void mayBeInAcceptingState(Object variable) {

	}

	@Deprecated
	protected void mustBeInAcceptingState(Object variable) {

	}

	/**
	 * This method can be used in test cases to create branching. It is not
	 * optimized away.
	 * 
	 * @return
	 */
	protected boolean staticallyUnknown() {
		return true;
	}
	
	
}
