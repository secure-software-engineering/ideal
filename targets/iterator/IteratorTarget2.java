package iterator;

import java.util.LinkedList;
import java.util.List;

public class IteratorTarget2 {
  public static void main(String... args) {
    List<Object> list = new LinkedList<>();
    list.add(new Object());
    list.add(new Object());
    for (Object l : list) {
      System.out.println(l);
    }

  }
}
