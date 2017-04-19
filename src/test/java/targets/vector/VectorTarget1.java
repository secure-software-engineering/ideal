package targets.vector;

import java.io.IOException;
import java.util.Vector;

public class VectorTarget1 {
  public static void main(String... args) throws IOException {
    Vector s = new Vector();
    s.lastElement();
    s.elementAt(0);
  }
}
