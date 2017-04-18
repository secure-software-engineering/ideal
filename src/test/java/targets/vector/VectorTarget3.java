package vector;

import java.io.IOException;
import java.util.Vector;

public class VectorTarget3 {
  public static void main(String... args) throws IOException {
    Vector s = new Vector();
    if (args == null)
      s.lastElement();
    else
      s.elementAt(0);
  }
}
