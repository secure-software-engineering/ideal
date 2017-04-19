package targets.iterator;

import java.util.HashSet;

public class IteratorTarget5 {
  public static void main(String... args) {
    HashSet<Object> list = new HashSet<>();
    // LinkedHashSet<Object> list2 = new LinkedHashSet<>();
    list.add(new Object());
    for (Object each : list) {
      // list2.add(each);
    }
  }
}
