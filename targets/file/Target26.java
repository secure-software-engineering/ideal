package file;

public class Target26 {
  public static void main(String[] args) {
    File first = createOpenedFile();
    clse(first);

    File second = createOpenedFile();
    second.hashCode();
  }

  private static void clse(File first) {
    first.close();
  }

  public static File createOpenedFile() {
    File f = new File();
    f.open();
    return f;
  }
}
