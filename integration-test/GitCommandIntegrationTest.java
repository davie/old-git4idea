import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.matchers.IsCollectionContaining;
import static org.junit.Assert.assertThat;
import org.hamcrest.Matcher;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.allOf;
import com.assembla.git.commands.GitStatusCommand;
import com.assembla.git.GitFile;
import com.assembla.git.IErrorHandler;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.util.Set;
import java.io.File;

public class GitCommandIntegrationTest {
    private File baseDir;
    private static final String GIT_EXECUTABLE = "git";

    @Before
    public void setup(){
        baseDir = tempDirectory("/tmp/abcde");
    }

    @Test
    public void anEmptyRepositoryWithAnewFileShouldShowOneUnversioned() throws Exception {
        GitStatusCommand gitCommand = new GitStatusCommand(sysOutPrintingErrorHandler(), baseDir, GIT_EXECUTABLE);

        //create an empty repo
        gitCommand.init();

        // create a new unversioned file
        File.createTempFile("newFile", ".txt", baseDir);

        // check the status
        Set<GitFile> files = gitCommand.status("");

        assertEquals(1, files.size());
        GitFile file = files.iterator().next();
        assertEquals(GitFile.Status.UNVERSIONED, file.getStatus() );
    }

    @Test
    public void aRepositoryWithAnAddedAndUnversionedFileShouldShowStatusesCorrectly() throws Exception {
        GitStatusCommand gitCommand = new GitStatusCommand(sysOutPrintingErrorHandler(), baseDir, GIT_EXECUTABLE);

        //create an empty repo
        gitCommand.init();

        // create a new file
        File addedFile = File.createTempFile("newFile", ".txt", baseDir);
        gitCommand.add(addedFile);
        File unversionedFile = File.createTempFile("unversionedFile", ".txt", baseDir);


        // check the status
        Set<GitFile> files = gitCommand.status("");
        assertEquals(2, files.size());

        GitFile fileWithStatusUnversioned = new GitFile(unversionedFile.getAbsolutePath(), GitFile.Status.UNVERSIONED);
        GitFile fileWithStatusNew = new GitFile(addedFile.getAbsolutePath(), GitFile.Status.ADDED);

        assertThat(files, is(collectionContaining(allOf(
                equalTo(fileWithStatusNew)
        ))));

        assertThat(files, is(collectionContaining(allOf(
                equalTo(fileWithStatusUnversioned)
        ))));

    }

    private IsCollectionContaining<GitFile> collectionContaining(Matcher<GitFile> matchers) {
        return new IsCollectionContaining<GitFile>(matchers);
    }

    private IErrorHandler sysOutPrintingErrorHandler() {
        return new IErrorHandler() {
            public void displayErrorMessage(String message) {
                System.out.println(message);
            }
        };
    }

    private IErrorHandler testFailingErrorHandler() {
        return new IErrorHandler() {
            public void displayErrorMessage(String message) {
                fail("caught error: " + message);
            }
        };
    }


    @After
    public void tearDown(){
        deleteFilesUnder(baseDir);
    }

    private void deleteFilesUnder(File path) {
        if(path.isDirectory()){
            for (File file: path.listFiles()){
                deleteFilesUnder(file);
            }
        }
        path.delete();
    }

    private File tempDirectory(String path) {
        File baseDir = new File(path);
        baseDir.mkdir();
        baseDir.deleteOnExit();
        return baseDir;
    }
}
