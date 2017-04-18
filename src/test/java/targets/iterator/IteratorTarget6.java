package iterator;

import java.util.LinkedList;

public class IteratorTarget6 {
  public static void main(String... args) {
    LinkedList<Object> list = new LinkedList<>();
    LinkedList<Object> list2 = new LinkedList<>();
    list.add(new Object());
    for (Object each : list) {
      list2.add(each);
    }
  }
}
