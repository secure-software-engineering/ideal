package file;

public class Target28 {
	public static void main(String[] args) {
		ObjectWithField a = new ObjectWithField();
		ObjectWithField b = a;
		File file = new File();
		file.open();
		wrappedFlows(a, b, file);
	}

	private static void wrappedFlows(ObjectWithField a, ObjectWithField b, File file) {
	  flows(a,b,file);
	}

	private static void flows(ObjectWithField a, ObjectWithField b, File file) {
		a.field = file;
		File alias = b.field;
		if (alias != null)
			alias.close();
	}
}
