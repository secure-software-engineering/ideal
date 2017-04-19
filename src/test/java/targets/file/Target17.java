package targets.file;

public class Target17 {
  public static void main(String[] args) {
    ObjectWithField a = new ObjectWithField();
    ObjectWithField b = a;
    File file = new File();
    a.field = file;
    file.open();
    if (b.field != null)
      b.field.close();
  }
}
