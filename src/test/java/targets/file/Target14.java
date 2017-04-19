package targets.file;

public class Target14 {
  public static void main(String[] args) {
    File file = null;
    if (args != null)
      file = new File();

    file.open();
    if (file != null)
      file.close();
  }

  private static void flows(ObjectWithField container) {
    container.field = new File();
    File field = container.field;
    field.open();
  }
}
