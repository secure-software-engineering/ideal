package targets.iterator;

import java.util.Iterator;

public class MyLinkedList<V> {

  public void add(Object object) {
    // TODO Auto-generated method stub

  }

  public Iterator<V> iterator() {
    return new MyIterator<V>();
  }

}
