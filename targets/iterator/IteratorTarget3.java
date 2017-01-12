package iterator;

public class IteratorTarget3 {
  public static void main(String... args) {
    MyLinkedList<Object> list = new MyLinkedList<>();
    list.add(new Object());
    java.util.Iterator<Object> iterator = list.iterator();
    iterator.hasNext();
    iterator.next();
    iterator.next();
  }
}
