package typestate.tests.base;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ideal.Analysis;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.options.Options;

public abstract class TestingFramework<V> {

  private Analysis<V> analysis;
  protected long analysisTime;

  @SuppressWarnings("static-access")
  protected void initializeSoot(String targetClass, String sootCp) {
    G.v().reset();
    Options.v().set_whole_program(true);
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().setPhaseOption("cg.spark", "on");
    Options.v().set_prepend_classpath(true);
    Options.v().setPhaseOption("cg", "trim-clinit:false");
    Options.v().set_no_bodies_for_excluded(true);
    Options.v().set_allow_phantom_refs(true);

    List<String> includeList = new LinkedList<String>();
    includeList.add("java.lang.*");
    includeList.add("java.util.*");
    includeList.add("java.io.*");
    includeList.add("sun.misc.");
    includeList.add("java.net.");
    includeList.add("javax.servlet.");
    includeList.add("javax.crypto.");
    includeList.add("java.security.");

    includeList.add("android.");
    includeList.add("org.apache.http.");
    Options.v().set_include(includeList);
    Options.v().set_soot_classpath(sootCp);
    Options.v().set_main_class(targetClass);
    Scene.v().addBasicClass(targetClass, SootClass.BODIES);
    Scene.v().loadNecessaryClasses();
    SootClass c = Scene.v().forceResolve(targetClass, SootClass.BODIES);
    if (c != null) {
      c.setApplicationClass();
    }
    SootMethod methodByName = c.getMethodByName("main");
    List<SootMethod> ePoints = new LinkedList<>();
    ePoints.add(methodByName);
    Scene.v().setEntryPoints(ePoints);
  }

  public void run(String targetClass, String classPath) {
    initializeSoot(targetClass, classPath);
    Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {
      protected void internalTransform(String phaseName,
          @SuppressWarnings("rawtypes") Map options) {
        System.out.println(Scene.v().getMainMethod().getActiveBody());
        System.out.println(Scene.v().getReachableMethods().size());
        TestingFramework.this.getAnalysis().run();
      }
    });
    PackManager.v().getPack("wjtp").add(transform);
    PackManager.v().getPack("cg").apply();
    PackManager.v().getPack("wjtp").apply();
  }

  protected abstract Analysis<V> createAnalysis();

  protected Analysis<V> getAnalysis() {
    if (analysis == null)
      analysis = createAnalysis();
    return analysis;
  }

}
