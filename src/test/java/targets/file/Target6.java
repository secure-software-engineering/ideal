package targets.file;

public class Target6 {
  public static void main(String[] args) {
    ObjectWithField container = new ObjectWithField();
    container.field = new File();
    File a = container.field;
    a.open();
  }
}
