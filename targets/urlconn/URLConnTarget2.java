package urlconn;

import java.io.IOException;
import java.net.HttpURLConnection;

public class URLConnTarget2 {
  public static void main(String... args) throws IOException {
    HttpURLConnection httpURLConnection = new HttpURLConnection(null) {

      @Override
      public void connect() throws IOException {
        // TODO Auto-generated method stub
        System.out.println("");
      }

      @Override
      public boolean usingProxy() {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public void disconnect() {
        // TODO Auto-generated method stub

      }
    };
    httpURLConnection.setDoOutput(true);
    httpURLConnection.setAllowUserInteraction(false);

    httpURLConnection.connect();
  }
}
