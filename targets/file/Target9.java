package file;

public class Target9 {
  public static void main(String[] args) {
    ObjectWithField container = new ObjectWithField();
    flows(container);
    container.field.close();
  }

  private static void flows(ObjectWithField container) {
    container.field = new File();
    File field = container.field;
    field.open();
  }
}
