package file;

public class Target30 {
  public static void main(String[] args) {
    ObjectWithField a = new ObjectWithField();
    ObjectWithField b = a;
    File file = new File();
    bar(a,file);
    File c = b.field;
    c.close();
  }

	private static void bar(ObjectWithField a, File file) {
		file.open();
		a.field = file;
	}
}
