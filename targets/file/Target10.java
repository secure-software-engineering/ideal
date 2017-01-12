package file;

public class Target10 {
  public static void main(String[] args) {
    File file = new File();
    file.open();
    flows(file, true);
  }

  private static void flows(File file, boolean b) {
    if (b)
      file.close();
  }
}
