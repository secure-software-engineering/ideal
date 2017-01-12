package iterator;

import java.util.LinkedHashSet;

public class IteratorTarget8 {
  public static void main(String... args) {
    invoke();
  }

  private static boolean invoke() {
    LinkedHashSet<Object> list = new LinkedHashSet<>();
    list.add(new Object());
    for (Object each : list) {
      if (each.hashCode() == 1)
        return true;
    }
    return false;
  }
}
