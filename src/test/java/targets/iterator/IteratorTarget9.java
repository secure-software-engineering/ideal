package iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorTarget9 {

  public static void main(String[] args) {
    List l1 = new ArrayList();
    List l2 = new ArrayList();

    l1.add("foo");
    l1.add("moo");
    l1.add("zoo");

    Object v;
    for (Iterator it1 = l1.iterator(); it1.hasNext(); v = it1.next()) {
      System.out.println(foo(it1));
    }
  }

  public static Object foo(Iterator it) {
    return it.next();
  }
}
