package targets.file;

public class Target4 {
  public static void main(String[] args) {
    File file = new File();
    File alias = file;
    call(alias);
  }

  private static void call(File alias) {
    alias.open();
  }
}
