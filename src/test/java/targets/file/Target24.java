package targets.file;

public class Target24 {
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
    if (b != null)
    	 b.close();
  }
}
