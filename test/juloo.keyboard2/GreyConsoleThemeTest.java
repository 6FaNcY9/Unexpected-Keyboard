package juloo.keyboard2;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static org.junit.Assert.*;

/** Pins the Grey Console theme to the bandit-nix palette
    (themes/material-sand-dark.yaml). */
public class GreyConsoleThemeTest
{
  public GreyConsoleThemeTest() {}

  static Document parse(String path) throws Exception
  {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder()
      .parse(new File(path));
  }

  static Element findNamed(Document doc, String tag, String name)
  {
    NodeList nodes = doc.getElementsByTagName(tag);
    for (int i = 0; i < nodes.getLength(); i++)
    {
      Element e = (Element)nodes.item(i);
      if (name.equals(e.getAttribute("name")))
        return e;
    }
    return null;
  }

  static List<String> items(Element parent)
  {
    List<String> out = new ArrayList<String>();
    NodeList nodes = parent.getElementsByTagName("item");
    for (int i = 0; i < nodes.getLength(); i++)
      out.add(nodes.item(i).getTextContent().trim());
    return out;
  }

  @Test
  public void greyConsoleColours() throws Exception
  {
    Element style = findNamed(parse("res/values/themes.xml"), "style", "GreyConsole");
    assertNotNull("style GreyConsole not found", style);
    assertEquals("BaseTheme", style.getAttribute("parent"));
    Map<String, String> got = new HashMap<String, String>();
    NodeList nodes = style.getElementsByTagName("item");
    for (int i = 0; i < nodes.getLength(); i++)
    {
      Element e = (Element)nodes.item(i);
      got.put(e.getAttribute("name"), e.getTextContent().trim());
    }
    String[][] expected = {
      { "android:isLightTheme", "false" },
      { "colorKeyboard", "#0d0d0e" },
      { "colorKey", "#18181a" },
      { "colorKeySpaceBar", "#18181a" },
      { "colorKeyAction", "#303034" },
      { "colorKeyActivated", "#303034" },
      { "colorLabel", "#d0d0d0" },
      { "colorSubLabel", "#fdb827" },
      { "colorLabelActivated", "#bc61bc" },
      { "colorLabelLocked", "#00a99d" },
      { "keyBorderWidth", "0dp" },
      { "keyBorderWidthActivated", "0dp" },
      { "emoji_button_bg", "#18181a" },
      { "emoji_color", "#d0d0d0" },
    };
    for (String[] kv : expected)
      assertEquals(kv[0], kv[1], got.get(kv[0]));
  }

  @Test
  public void greyConsoleListed() throws Exception
  {
    Document arrays = parse("res/values/arrays.xml");
    List<String> values = items(findNamed(arrays, "string-array", "pref_theme_values"));
    List<String> entries = items(findNamed(arrays, "string-array", "pref_theme_entries"));
    assertTrue("greyconsole missing from pref_theme_values", values.contains("greyconsole"));
    assertEquals(values.size(), entries.size());
  }
}
