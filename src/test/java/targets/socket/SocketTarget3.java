package targets.socket;

import java.io.IOException;
import java.net.Socket;

public class SocketTarget3 {
  public static void main(String... args) throws IOException {
    Socket socket = new Socket();
    socket.sendUrgentData(2);
    socket.sendUrgentData(2);
  }
}
