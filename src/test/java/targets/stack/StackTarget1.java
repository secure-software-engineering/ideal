package targets.stack;

import java.io.IOException;
import java.util.Stack;

public class StackTarget1 {
  public static void main(String... args) throws IOException {
    Stack s = new Stack();
    if (args == null)
      s.peek();
    else {
      Stack r = s;
      r.pop();
    }
  }
}
