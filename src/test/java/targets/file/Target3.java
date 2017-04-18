package file;

public class Target3 {
  public static void main(String[] args) {
    File file = new File();
    File alias = file;
    call(alias);
    file.close();
  }

  private static void call(File alias) {
    alias.open();
  }
}
