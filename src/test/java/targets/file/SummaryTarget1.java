package targets.file;

public class SummaryTarget1 {
  public static void main(String[] args) {
    File file1 = new File();
    call(file1);
    file1.close();
    File file = new File();
    File alias = file;
    call(alias);
    file.close();
  }

  private static void call(File alias) {
    alias.open();
  }
}
