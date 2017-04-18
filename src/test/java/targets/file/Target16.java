package file;

public class Target16 {
  public static void main(String[] args) {
    ObjectWithField a = new ObjectWithField();
    ObjectWithField b = a;
    File file = new File();
    file.open();
    flows(a, b, file);
  }

  private static void flows(ObjectWithField a, ObjectWithField b, File file) {
    a.field = file;
    File alias = b.field;
    if (alias != null)
      alias.close();
  }
}
