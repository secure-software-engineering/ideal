package file;

public class Target7 {
  public static void main(String[] args) {
    ObjectWithField container = new ObjectWithField();
    container.field = new File();
    flows(container);
  }

  private static void flows(ObjectWithField container) {
    File field = container.field;
    field.open();
  }
}
