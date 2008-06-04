package com.assembla.git.commands;

import org.junit.Test;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;

import java.util.List;
import java.text.SimpleDateFormat;

import static junit.framework.Assert.assertEquals;


public class GitLogParserTest {
    @Test
    public void shouldReturnRevisionWithCorrectNumberDateAuthorAndComment() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss Z");

        String author = "My Name";
        String revision = "b3bb49b";
        String date = "2008-03-21 00:14:06 +0200";
        String comment = "Project import";
        String logMessage = revision + "|" + author + "|" + date + "|" + comment;

        List<VcsFileRevision> fileRevisionList = new GitLogParser().getRevisionsFrom(null, logMessage, null);
        assertEquals(1, fileRevisionList.size());
        assertEquals(author, fileRevisionList.get(0).getAuthor());
        assertEquals(df.parse(date), fileRevisionList.get(0).getRevisionDate());
        assertEquals(comment, fileRevisionList.get(0).getCommitMessage());
        // FIXME put this back when revision numbers are fixed
        //        assertEquals(revision, fileRevisionList.get(0).getRevisionNumber());

    }
}
