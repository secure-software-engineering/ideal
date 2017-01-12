package vector;

import java.io.IOException;
import java.util.Vector;

public class VectorTarget2 {
  public static void main(String... args) throws IOException {
    Vector s = new Vector();
    s.add(new Object());
    if (args == null)
      s.firstElement();
    else
      s.elementAt(0);
  }
}
