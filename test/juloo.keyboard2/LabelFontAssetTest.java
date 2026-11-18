package juloo.keyboard2;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Test;
import static org.junit.Assert.*;

/** The label font that Theme.getLabelFont() loads must ship as a real
    TrueType file, with its license next to it. */
public class LabelFontAssetTest
{
  public LabelFontAssetTest() {}

  @Test
  public void labelFontIsTrueType() throws Exception
  {
    File font = new File("assets/0xProto-Regular.ttf");
    assertTrue("assets/0xProto-Regular.ttf missing", font.isFile());
    assertTrue("font too small: " + font.length(), font.length() > 50000);
    byte[] magic = new byte[4];
    FileInputStream in = new FileInputStream(font);
    try { assertEquals(4, in.read(magic)); } finally { in.close(); }
    assertArrayEquals(new byte[]{ 0, 1, 0, 0 }, magic);
    String license = new String(
        Files.readAllBytes(new File("assets/0xProto-OFL.txt").toPath()),
        StandardCharsets.UTF_8);
    assertTrue(license.contains("SIL OPEN FONT LICENSE"));
  }
}
