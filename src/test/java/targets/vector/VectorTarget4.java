package targets.vector;

import java.util.Vector;

public class VectorTarget4 {
	public static void main(String[] args) {
		try {
			Vector v = new Vector();
			v.removeAllElements();
			v.firstElement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
