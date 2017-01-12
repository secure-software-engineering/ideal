package iterator;

import java.util.LinkedList;

public class IteratorTarget4 {
  public static void main(String... args) {
    LinkedList<Object> list = new LinkedList<>();
    list.add(new Object());
    for (Object each : list) {
      try {
        each.toString();
      } catch (Throwable e) {
        e.getMessage();
      }
    }
  }
}
