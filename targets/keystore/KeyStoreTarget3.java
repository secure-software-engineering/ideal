package keystore;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class KeyStoreTarget3 {
  public static void main(String... args)
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

    java.io.FileInputStream fis = null;
    try {
      fis = new java.io.FileInputStream("keyStoreName");
      ks.load(fis, null);
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
    ks.aliases();
  }
}

