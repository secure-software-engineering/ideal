package stack;

import java.io.IOException;
import java.util.Stack;

public class StackTarget4 {
  public static void main(String... args) throws IOException {
    Stack s = new Stack();
    s.peek();
    s.pop();
    
    Stack c = new Stack();
    c.add(new Object());
    c.peek();
    c.pop();
  }
}
