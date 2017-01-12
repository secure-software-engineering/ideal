package typestate.tests.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.google.common.collect.Table.Cell;

import boomerang.accessgraph.AccessGraph;
import soot.SootMethod;
import typestate.ResultCollection;
import typestate.TypestateAnalysis;
import typestate.TypestateDomainValue;

public abstract class TypestateTestingFramework extends TestingFramework<TypestateDomainValue> {

  public void expectNErrors(String targetClass, int n) {
    String userdir = System.getProperty("user.dir");
    String sootCp = userdir + "/targetsBin";
    expectNErrors(targetClass, sootCp, n);
  }

  public void expectNErrors(String targetClass, String sootCp, int n) {
    run(targetClass, sootCp);
    Set<Cell<SootMethod, AccessGraph, TypestateDomainValue>> errors = ((TypestateAnalysis) getAnalysis()).getErrors();
    assertEquals(errors.toString(), n, errors.size());
  }
  public void expectNFacts(String targetClass, int n) {
    String userdir = System.getProperty("user.dir");
    String sootCp = userdir + "/targetsBin";
    run(targetClass, sootCp);
    ResultCollection pathEdges = ((TypestateAnalysis) getAnalysis()).getPathEdgesAtEndOfMethods();
    assertEquals(pathEdges.toString(), n, pathEdges.size());
  }

  public void expectAtLeastOneError(String targetClass) {
    String userdir = System.getProperty("user.dir");
    String sootCp = userdir + "/targetsBin";
    run(targetClass, sootCp);
    Set<Cell<SootMethod, AccessGraph, TypestateDomainValue>> errors = ((TypestateAnalysis) getAnalysis()).getErrors();
    assertTrue("Expected at least one finding.", errors.size() > 0);
  }
}
