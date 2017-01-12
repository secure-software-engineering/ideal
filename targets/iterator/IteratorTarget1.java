package iterator;

import java.util.LinkedList;
import java.util.List;

public class IteratorTarget1 {
  public static void main(String... args) {
    List<Object> list = new LinkedList<>();
    list.add(new Object());
    java.util.Iterator<Object> iterator = list.iterator();
    iterator.hasNext();
    iterator.next();
    iterator.next();
  }
}
