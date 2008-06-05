package com.assembla.git.commands;

import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.project.Project;
import com.assembla.git.GitFileRevision;
import com.assembla.git.GitRevisionNumber;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

class GitLogParser {
    public List<VcsFileRevision> getRevisionsFrom(FilePath filePath, String result, Project project) throws VcsException {
        List<VcsFileRevision> revisions = new ArrayList<VcsFileRevision>();


        int revision = 0;
        for (StringTokenizer i = new StringTokenizer(result, "\n\r"); i.hasMoreTokens();) {
            final String line = i.nextToken();
                String[] fields = line.split("\\|");// FIXME this will break if there's a pipe in the commit message

                GitFileRevision rev = new GitFileRevision(project);
                rev.setPath(filePath);

                // FIXME git revisions are SHA1, not ints
                rev.setRevision(new GitRevisionNumber(fields[0]));
                rev.setAuthor(fields[1]);
                rev.setRevisionDate(getDate(fields[2]));
                rev.setMessage(fields[3]);
                revisions.add(rev);
        }
        return revisions;
    }

    private Date getDate(String stringValue) {
        Date date = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss Z");
            date = df.parse(stringValue);
        } catch (ParseException e) {
            //ignore this for now
        }
        return date;
    }
}
