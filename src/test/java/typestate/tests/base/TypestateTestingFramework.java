package typestate.tests.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.junit.Assert;

import com.google.common.collect.Table.Cell;

import boomerang.accessgraph.AccessGraph;
import soot.SootMethod;
import typestate.ResultCollection;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public abstract class TypestateTestingFramework extends TestingFramework<TypestateDomainValue> {

	private String getSootCp(){
	    String userdir = System.getProperty("user.dir");
		String sootCp = userdir +File.separator+ "target/test-classes";
		String javaHome = System.getProperty("java.home");
		if(javaHome == null || javaHome.equals(""))
			throw new RuntimeException("Could not get property java.home!");
		
		sootCp += File.pathSeparator + javaHome+ "/lib/rt.jar";
	    return sootCp;
	}
  public void expectNErrors(String targetClass, int n) {
    expectNErrors(targetClass, getSootCp(), n);
  }

  public void expectNErrors(String targetClass, String sootCp, int n) {
    run(targetClass, sootCp);
    Set<Cell<SootMethod, AccessGraph, TypestateDomainValue>> errors = ((TypestateAnalysis) getAnalysis()).getErrors();
    if(errors.size() < n)
    	throw new RuntimeException("Unsound results " + errors);
    assertEquals(errors.toString(), n, errors.size());
  }
  public void expectNFacts(String targetClass, int n) {
    run(targetClass, getSootCp());
    ResultCollection pathEdges = ((TypestateAnalysis) getAnalysis()).getPathEdgesAtEndOfMethods();
    assertEquals(pathEdges.toString(), n, pathEdges.size());
  }

  public void expectAtLeastOneError(String targetClass) {
    run(targetClass, getSootCp());
    Set<Cell<SootMethod, AccessGraph, TypestateDomainValue>> errors = ((TypestateAnalysis) getAnalysis()).getErrors();
    if(errors.size() == 0)
    	throw new RuntimeException("Unsound. Expected at least one finding.");
  }
}
