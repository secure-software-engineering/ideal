package targets.file;

public class Target8 {
  public static void main(String[] args) {
    ObjectWithField container = new ObjectWithField();
    container.field = new File();
    ObjectWithField otherContainer = new ObjectWithField();
    File a = container.field;
    otherContainer.field = a;
    flows(container);
  }

  private static void flows(ObjectWithField container) {
    File field = container.field;
    field.open();
  }
}
