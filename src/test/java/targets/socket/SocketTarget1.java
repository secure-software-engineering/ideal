package targets.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketTarget1 {
  public static void main(String... args) throws IOException {
    Socket socket = new Socket();
    socket.connect(new SocketAddress() {});
    socket.sendUrgentData(2);
  }
}
