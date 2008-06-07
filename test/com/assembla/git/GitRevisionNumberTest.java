package com.assembla.git;

import org.junit.Test;import static org.junit.Assert.assertEquals;

public class GitRevisionNumberTest {
    @Test
    public void shouldBeAbleToStoreSha1Hash(){
        String sha1 = "ed8bf4cece2834bd49dabc4c9423fbf4e4d2d54b";
        GitRevisionNumber revision = new GitRevisionNumber(sha1);
        assertEquals(sha1, revision.getVersion());
    }

}
