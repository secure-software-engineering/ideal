package file;

public class Target13 {
  public static void main(String[] args) {
    ObjectWithField container = new ObjectWithField();
    flows(container);
    if (container.field != null)
      container.field.close();
  }

  private static void flows(ObjectWithField container) {
    container.field = new File();
    File field = container.field;
    field.open();
  }
}
