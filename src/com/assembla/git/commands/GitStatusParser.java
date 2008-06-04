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
            String separator = " ";
            if (s.contains(separator)) {
                String[] statusAndFileName = s.split(separator);
                if (statusAndFileName.length != 2) {
                    throw new VcsException("can't parse git output >" + s + "<");
                } else {
                        String fileName = statusAndFileName[1];
                        String status = statusAndFileName[0];

                        GitFile file = new GitFile(basePath + File.separator + fileName, convertStatus(status));
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
        else if (status.equals("C"))
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
        System.out.println("Unknown status: " + status);

        return GitFile.Status.UNMODIFIED;
    }
}
