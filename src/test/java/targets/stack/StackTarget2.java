package stack;

import java.io.IOException;
import java.util.Stack;

public class StackTarget2 {
  public static void main(String... args) throws IOException {
    Stack s = new Stack();
    s.add(new Object());
    if (args == null)
      s.peek();
    else
      s.pop();
  }
}
