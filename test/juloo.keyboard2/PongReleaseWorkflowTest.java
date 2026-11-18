package juloo.keyboard2;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

/** The release job holds the only signing key of an IME (it sees every
    password), so its supply chain stays pinned and its secrets scoped. */
public class PongReleaseWorkflowTest
{
  public PongReleaseWorkflowTest() {}

  static List<String> lines() throws Exception
  {
    return Files.readAllLines(
        new File(".github/workflows/pong-release.yml").toPath(),
        StandardCharsets.UTF_8);
  }

  @Test
  public void actionsArePinnedToCommits() throws Exception
  {
    Pattern pinned = Pattern.compile("^\\s*-?\\s*uses: [\\w.-]+/[\\w./-]+@[0-9a-f]{40}( #.*)?$");
    int uses = 0;
    for (String l : lines())
      if (l.matches("^\\s*-?\\s*uses:.*"))
      {
        uses++;
        assertTrue("not pinned to a commit SHA: " + l.trim(), pinned.matcher(l).matches());
      }
    assertTrue("no actions found", uses >= 4);
  }

  @Test
  public void tokenAndSecretsAreScoped() throws Exception
  {
    String y = String.join("\n", lines());
    assertTrue("missing top-level permissions: contents: read",
        Pattern.compile("(?m)^permissions:\\n\\s+contents: read$").matcher(y).find());
    assertTrue("checkout must not persist the token",
        y.contains("persist-credentials: false"));
    assertTrue("job must use the branch-limited environment 'release'",
        Pattern.compile("(?m)^\\s+environment: release$").matcher(y).find());
  }
}
