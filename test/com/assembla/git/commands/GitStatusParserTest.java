package com.assembla.git.commands;

import org.junit.Test;
import static org.junit.Assert.assertThat;
import com.assembla.git.GitFile;

import java.util.Set;

import static junit.framework.Assert.assertEquals;


public class GitStatusParserTest {
    private static final String BASE_PATH = "base_path";
    private static final String MODIFIED_FILE_PATH = "src/SomeFile.java";
    private static final String GIT_OUTPUT_WITH_ONE_MODIFICATION = "C " + MODIFIED_FILE_PATH;

    @Test
    public void testParsingAStatusMessageWithOneModifiedFile() throws Exception {
        GitStatusParser parser = new GitStatusParser(BASE_PATH);

        Set<GitFile> files = parser.parse(GIT_OUTPUT_WITH_ONE_MODIFICATION);
        assertEquals(1, files.size());
        GitFile gitFile = files.iterator().next();
        assertEquals(GitFile.Status.MODIFIED, gitFile.getStatus());
        assertEquals(BASE_PATH + "/" + MODIFIED_FILE_PATH, gitFile.getPath());
    }


}

