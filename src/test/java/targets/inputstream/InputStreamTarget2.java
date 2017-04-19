package targets.inputstream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamTarget2 {
  public static void main(String... args) throws IOException {
    InputStream inputStream = new FileInputStream("");
    inputStream.close();
    inputStream.close();
    inputStream.read();
  }
}
