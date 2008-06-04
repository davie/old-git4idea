package com.assembla.git.commands;

import com.assembla.git.GitFile;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.diagnostic.Logger;

import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.io.File;

class GitStatusParser {
    private final String basePath;
    private static final Logger LOG = Logger.getInstance("com.assembla.git.GitVcs");


    public GitStatusParser(String basePath) {
        this.basePath = basePath;
    }

    public Set<GitFile> parse(String output) throws VcsException {
        Set<GitFile> result = new HashSet<GitFile>();
        for (StringTokenizer i = new StringTokenizer(output, "\n\r"); i.hasMoreTokens();) {
            final String s = i.nextToken();
            String tab = "\t";
            if (s.contains(tab)) {
                String[] larr = s.split(tab);
                // git adds a header and footer
                if (larr.length != 2) {
                    throw new VcsException("can't parse git output >" + s + "<");
                } else {
                    String fileName = larr[1];
                    String statusDescription = larr[0];
                    GitFile file = new GitFile(basePath + File.separator + fileName, convertStatus(statusDescription));
                    result.add(file);
                }
            }
        }
        return result;
    }


    /**
     * Helper method to convert String status' from the Git output to a GitFile status
     *
     * @param status The status from Git as a String.
     * @return The Git file status.
     * @throws com.intellij.openapi.vcs.VcsException
     *          something bad had happened
     */
    private GitFile.Status convertStatus(String status) throws VcsException {
        // statuses wrong
        if (status.equals("A"))
            return GitFile.Status.ADDED;
        else if (status.equals("M"))
            return GitFile.Status.MODIFIED;
        else if (status.equals("?"))
            return GitFile.Status.UNVERSIONED;
        else if (status.equals("R") || status.equals("!"))
            return GitFile.Status.DELETED;
        else if (status.equals("C"))
            return GitFile.Status.UNMODIFIED;
        else if (status.equals("I"))
            return GitFile.Status.IGNORED;

        LOG.warn("Unknown status: " + status);

        return GitFile.Status.UNMODIFIED;
    }
}
