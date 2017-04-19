package targets.file;

public class Target25 {
  public static void main(String[] args) {
    ObjectWithField a = new ObjectWithField();
    ObjectWithField b = new ObjectWithField();
    File file = new File();
    if (args != null) {
      b.field = file;
    } else {
      a.field = file;
    }
    a.field.open();
    b.field.close();
  }
}
