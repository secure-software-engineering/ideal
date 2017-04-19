package targets.printwriter;

import java.io.IOException;
import java.io.PrintWriter;

public class PrintWriterTarget1 {
  public static void main(String... args) throws IOException {
    PrintWriter inputStream = new PrintWriter("");
    inputStream.close();
    inputStream.flush();
  }
}
