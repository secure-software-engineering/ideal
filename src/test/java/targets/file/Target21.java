package targets.file;

public class Target21 {
  public static void main(String[] args) {
    ObjectWithField a = new ObjectWithField();
    ObjectWithField b = a;
    File file = new File();
    file.open();
    flows(a, b, file);
    b.field.close();
  }

  private static void flows(ObjectWithField a, ObjectWithField b, File file) {
    a.field = file;
  }
}
