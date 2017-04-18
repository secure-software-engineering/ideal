package file;

public class Target19 {
  public static void main(String[] args) {
    File b = null;
    File a = new File();
    a.open();
    File e = new File();
    e.open();
    if (args != null) {
      b = a;
    } else {
      b = e;
    }
    b.close();
  }
}
