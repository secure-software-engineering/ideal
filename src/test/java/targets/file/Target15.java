package targets.file;

public class Target15 {
  public static void main(String[] args) {
    ObjectWithField a = new ObjectWithField();
    ObjectWithField b = a;
    flows(a, b);
  }

  private static void flows(ObjectWithField a, ObjectWithField b) {
    File file = new File();
    file.open();
    a.field = file;
    File alias = b.field;
    alias.close();
  }
}
