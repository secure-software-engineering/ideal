package iterator;

import java.util.LinkedHashSet;

public class IteratorTarget7 {
  public static void main(String... args) {
    LinkedHashSet<Object> list = new LinkedHashSet<>();
    LinkedHashSet<Object> list2 = new LinkedHashSet<>();
    list.add(new Object());
    for (Object each : list) {
      list2.add(each);
    }
  }
}
