package file;

public class Target11 {
  public static void main(String[] args) {
    Target11 target11 = new Target11();
    File file = new File();
    Flow flow = (args != null ? target11.new ImplFlow1() : target11.new ImplFlow2());
    flow.flow(file);
  }

  private static void flows(File file, boolean b) {
    if (b)
      file.close();
  }

  public class ImplFlow1 implements Flow {

    @Override
    public void flow(File file) {
      file.open();
    }

  }
  public class ImplFlow2 implements Flow {

    @Override
    public void flow(File file) {
      file.close();
    }

  }
  private interface Flow {
    void flow(File file);
  }
}
