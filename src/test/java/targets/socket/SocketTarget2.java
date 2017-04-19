package targets.socket;

import java.io.IOException;
import java.net.Socket;

public class SocketTarget2 {
  public static void main(String... args) throws IOException {
    Socket socket = new Socket();
    socket.sendUrgentData(2);
  }
}
