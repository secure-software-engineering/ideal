package file;

public class Target2 {
  public static void main(String[] args) {
    File file = new File();
    File alias = file;
    call(alias, file);
  }

  private static void call(File file1, File file2) {
    file1.open();
    file2.close();
  }
}
