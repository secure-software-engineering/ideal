package file;

public class Target23 {
  public static void main(String[] args) {
    ObjectWithField a = null;
    if (args != null)
      a = new ObjectWithField();
    File file = new File();
    file.open();
    a.field = file;
    if (a != null) {
        a.field.close();
    }
  }
}
