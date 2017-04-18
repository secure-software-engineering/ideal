package file;

public class Target12 {
  public static void main(String[] args) {
    ObjectWithField container = new ObjectWithField();
    flows(container);
    if (args != null)
      container.field.close();
  }

  private static void flows(ObjectWithField container) {
    container.field = new File();
    File field = container.field;
    field.open();
  }
}
