package targets.socket;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class SocketTarget4 {
  public static Socket createSocket() {
    return new Socket();
  }

  public static Collection<Socket> createSockets() {
    Collection<Socket> result = new LinkedList<>();
    for (int i = 0; i < 5; i++) {
      result.add(new Socket());
    }
    return result;
  }



  public static void talk(Socket s) throws IOException {
    s.getChannel();
  }

  public static void main(String... args) throws IOException {
    Collection<Socket> sockets = createSockets();
    for (Iterator<Socket> it = sockets.iterator(); it.hasNext();) {
      Socket s = (Socket) it.next();
      s.connect(null);
      talk(s);
    }
  }
}
