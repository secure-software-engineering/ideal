package printstream;

import java.io.IOException;
import java.io.PrintStream;

public class PrintStreamTarget1 {
  public static void main(String... args) throws IOException {
    PrintStream inputStream = new PrintStream("");
    inputStream.close();
    inputStream.flush();
  }
}
