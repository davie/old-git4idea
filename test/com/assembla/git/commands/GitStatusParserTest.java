package com.assembla.git.commands;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import com.assembla.git.GitFile;

import java.util.Set;

import static junit.framework.Assert.assertEquals;


public class GitStatusParserTest {
    private static final String BASE_PATH = "base_path";
    private String GIT_OUTPUT_WITH_ONE_MODIFICATION = "# On branch master\n" +
    "# Changed but not updated:\n" +
    "#   (use \"git add <file>...\" to update what will be committed)\n" +
    "#\n" +
    "#\tmodified:   src/SomeFile.java\n" +
    "#\n" +
    "no changes added to commit (use \"git add\" and/or \"git commit -a\")";

    @Test
    public void testSomething() throws Exception {
        GitStatusParser parser = new GitStatusParser(BASE_PATH);

        Set<GitFile> files = parser.parse(GIT_OUTPUT_WITH_ONE_MODIFICATION);
        assertEquals(1, files.size());
        GitFile gitFile = files.iterator().next();
        assertEquals(GitFile.Status.MODIFIED, gitFile.getStatus());
    }
}
