package targets.file;

public class Target27 {
  public static void main(String[] args) {
    File first = createOpenedFile();
    first.close();

    File second = createOpenedFile();
    second.getClass();
  }

  public static File createOpenedFile() {
    File f = new File();
    f.open();
    return f;
  }
}
