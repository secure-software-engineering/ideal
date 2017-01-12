package file;

public class Target22 {
  public static void main(String[] args) {
    ObjectWithField a = new ObjectWithField();
    File file = new File();
    file.open();
    a.field = file;
    if (a.field != null) {
        a.field.close();
    }
  }
}
