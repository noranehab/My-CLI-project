package org.example;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Field; // Import Field class for reflection
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyCLITest {

    private static final Path testDir = MyCLI.getCurrentDir().resolve("testDir");
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    //    private static Path testDir = Path.of("path/to/test/directory");
    @BeforeAll
    static void setUpClass() throws Exception {
        if (Files.notExists(testDir)) {
            Files.createDirectory(testDir); // Create test directory if it doesn't exist
        }
    }

    @AfterAll
    static void tearDownClass() throws Exception {
        if (Files.exists(testDir)) {
            Files.walk(testDir)
                    .sorted(Comparator.reverseOrder()) // Delete files and subdirectories first
                    .map(Path::toFile)
                    .forEach(file -> file.delete());
        }
    }

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStream)); // Redirect System.out to capture CLI output
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut); // Restore original System.out
        outputStream.reset();
    }


    @Test
    void testPrintWorkingDirectory() {
        String result = MyCLI.currentDir.toString();
        assertEquals(Paths.get(System.getProperty("user.dir")).toString(), result);
    }



    @Test
    void testChangeDirectorySimplified() throws Exception {
        Field field = MyCLI.class.getDeclaredField("currentDir");
        field.setAccessible(true);

        // Create a temporary directory for testing
        Path tempDir = Files.createTempDirectory("testDir");
        Path tempSubDir = tempDir.resolve("subDir");
        Files.createDirectory(tempSubDir); // Create a valid subdirectory

        // Check the initial value of currentDir
        Path initialDir = (Path) field.get(null);
        // System.out.println("Initial currentDir: " + initialDir);

        // Set MyCLI's current directory to tempDir to simulate changing to a subdirectory
        field.set(null, tempDir);

        // Call the method to change to the valid directory
        MyCLI.changeDirectory("subDir");

        // Get the updated currentDir
        Path currentDir = (Path) field.get(null);
        //System.out.println("Updated currentDir: " + currentDir);

        // Verify that currentDir has changed to the expected path
        assertEquals(tempSubDir.toString(), currentDir.toString(),
                "changeDirectory should change to the valid directory");

        // Check if directories exist before cleanup
        assertTrue(Files.exists(tempDir), "Temporary directory should exist.");
        assertTrue(Files.exists(tempSubDir), "Subdirectory should exist.");

        // Cleanup
        Files.deleteIfExists(tempSubDir);
        Files.deleteIfExists(tempDir);
    }


    @Test
    void testListDirectory() throws Exception { //ls
        // Setup: Create a few files and directories for testing
        Path file1 = testDir.resolve("file1.txt");
        Path file2 = testDir.resolve("file2.txt");
        Files.createFile(file1);
        Files.createFile(file2);

        // Verify that the files are created
        assertTrue(Files.exists(file1), "file1.txt should exist");
        assertTrue(Files.exists(file2), "file2.txt should exist");

        // Debug output: List actual files in testDir
        // System.out.println("Files in testDir: " + Files.list(testDir).map(Path::getFileName).collect(Collectors.toList()));

        // Test: List files in the current directory without any options
        MyCLI.listDirectory(new String[]{"ls"});
        String output = outputStream.toString();
        //  System.out.println("Output: " + output); // Debug output
        assertTrue(output.contains("file1.txt"), "ls should list file1.txt");
        assertTrue(output.contains("file2.txt"), "ls should list file2.txt");
    }

    @Test
    void testCreateDirectory() throws Exception { // mkdir
        // Create a unique temporary directory for this test
        Path tempDir = Files.createTempDirectory("testCreateDir");

        // Use reflection to access the private 'currentDir' field
        Field field = MyCLI.class.getDeclaredField("currentDir");
        field.setAccessible(true);
        field.set(null, tempDir); // Set currentDir to the temporary directory

        String dirName = "newDirectory3";  // Name of the directory to create
        Path newDir = tempDir.resolve(dirName);

        // Clean up any existing directory before the test
        if (Files.exists(newDir)) {
            Files.delete(newDir); // Remove the existing directory if it exists
        }

        // Call the method to create the directory
        MyCLI.createDirectory(dirName);

        // Verify that the directory was created
        boolean directoryExists = Files.exists(newDir);
        assertTrue(directoryExists, "createDirectory should create the new directory");

        // Clean up: Remove the created directory after the test
        Files.deleteIfExists(newDir);
        Files.delete(tempDir); // Remove the temporary directory
    }

    @Test
    void testRemoveDirectory() throws Exception { //rmdir
        String dirName = "testDirectory";  // Name of the directory to remove
        Path dir = MyCLI.currentDir.resolve(dirName); // Access currentDir via MyCLI

        // Create the directory first to ensure it exists before testing removal
        Files.createDirectory(dir);

        // Verify that the directory was created
        assertTrue(Files.exists(dir), "The directory should exist before removal.");

        // Call the method to remove the directory
        MyCLI.removeDirectory(dirName);

        // Verify that the directory was removed
        boolean directoryExists = Files.exists(dir);
        assertFalse(directoryExists, "removeDirectory should remove the directory");

        // Cleanup: Remove the directory if it still exists
        try {
            Files.deleteIfExists(dir); // This is to ensure no leftover directory
        } catch (IOException e) {
            // Handle any cleanup error if necessary
            //System.out.println("Error during cleanup: " + e.getMessage());
        }
    }

    @Test
    void testCreateFile() throws Exception { // touch
        // Use reflection to access the private 'currentDir' field
        Field field = MyCLI.class.getDeclaredField("currentDir");
        field.setAccessible(true); // Make the private field accessible

        // Get the currentDir value
        Path currentDir = (Path) field.get(null); // Get the value of the static field

        String fileName = "testCreateFile.txt"; // Name of the file to create
        Path filePath = currentDir.resolve(fileName); // Resolve the path

        // Clean up any existing file before the test
        if (Files.exists(filePath)) {
            Files.delete(filePath); // Remove the existing file if it exists
        }

        // Call the method to create the file
        MyCLI.createFile(fileName);

        // Verify that the file was created
        boolean fileExists = Files.exists(filePath);
        assertTrue(fileExists, "createFile should create the new file.");

        // Clean up: Remove the created file after the test
        Files.deleteIfExists(filePath); // Ensure no leftover file
    }

    @Test
    void testRemoveFile() throws Exception { // rm
        // Use reflection to access the private 'currentDir' field
        Field field = MyCLI.class.getDeclaredField("currentDir");
        field.setAccessible(true); // Make the private field accessible

        // Get the currentDir value
        Path currentDir = (Path) field.get(null); // Get the value of the static field

        String fileName = "testRemoveFile.txt"; // Name of the file to create
        Path filePath = currentDir.resolve(fileName); // Resolve the path

        // Create a test file with sample content
        Files.write(filePath, "Sample content".getBytes()); // Create the file first

        // Ensure the file exists before trying to remove it
        assertTrue(Files.exists(filePath), "The file should exist before removal.");

        // Call the method to remove the file
        MyCLI.removeFile(fileName);

        // Verify that the file was removed
        assertFalse(Files.exists(filePath), "removeFile should delete the file.");
    }

    @Test
    void testDisplayFile() throws Exception { // cat
        // Use reflection to access the private 'currentDir' field
        Field field = MyCLI.class.getDeclaredField("currentDir");
        field.setAccessible(true); // Make the private field accessible

        // Get the currentDir value
        Path currentDir = (Path) field.get(null); // Get the value of the static field

        String fileName = "testDisplayFile.txt"; // Name of the file to create
        Path filePath = currentDir.resolve(fileName); // Resolve the path

        // Create a test file with sample content
        String content = "Hello, World!\nThis is a test file.";
        Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE);

        // Redirect system output to capture it for testing
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        PrintStream originalOut = System.out; // Save the original System.out
        System.setOut(printStream); // Redirect System.out to the printStream

        // Call the method to display the file contents
        MyCLI.displayFile(fileName);

        // Restore the original System.out
        System.setOut(originalOut);

        // Verify the output
        String output = outputStream.toString().trim(); // Capture and trim output
        String expectedOutput = "Hello, World!\nThis is a test file."; // Expected output

        // Normalize line endings for comparison
        output = output.replace("\r\n", "\n"); // Normalize Windows line endings
        expectedOutput = expectedOutput.replace("\r\n", "\n"); // Normalize Windows line endings

        // Assert that the output matches expected content
        assertTrue(output.equals(expectedOutput), "displayFile should print the correct file content.");

        // Cleanup: Remove the test file after the test
        try {
            Files.deleteIfExists(filePath); // Ensure no leftover file
        } catch (IOException e) {
            //  System.out.println("Error during cleanup: " + e.getMessage());
        }
    }


    @Test
    void testDisplayHelp() { // help
        // Call the displayHelp method
        MyCLI.displayHelp();

        // Capture the output
        String output = outputStream.toString();

        // Check if the output contains the expected commands
        assertTrue(output.contains("Supported commands:"), "Help output should start with 'Supported commands:'");
        assertTrue(output.contains("pwd        - Show current directory path"), "Help output should include 'pwd' command");
        assertTrue(output.contains("cd <dir>   - Change directory to <dir>"), "Help output should include 'cd' command");
        assertTrue(output.contains("ls         - List files and directories in the current directory"), "Help output should include 'ls' command");
        assertTrue(output.contains("mkdir <dir>- Create a new directory"), "Help output should include 'mkdir' command");
        assertTrue(output.contains("rmdir <dir>- Remove an empty directory"), "Help output should include 'rmdir' command");
        assertTrue(output.contains("touch <file>- Create a new file"), "Help output should include 'touch' command");
        assertTrue(output.contains("rm <file>  - Remove a file"), "Help output should include 'rm' command");
        assertTrue(output.contains("cat <file> - Display contents of a file"), "Help output should include 'cat' command");
        assertTrue(output.contains("mv <source...> <dest> - Move/rename files"), "Help output should include 'mv' command");
        assertTrue(output.contains("exit       - Exit the CLI"), "Help output should include 'exit' command");
        assertTrue(output.contains("help       - Show this help message"), "Help output should include 'help' command");
    }


    @Test
    void testMoveFileSuccessfully() throws Exception {
        // Create a test file
        Path sourceFile = testDir.resolve("testFile2.txt");
        Files.createFile(sourceFile);

        // Create a destination directory
        Path destinationDir = testDir.resolve("destinationDir2");
        Files.createDirectory(destinationDir);

        // Prepare arguments for moving the file
        String[] args = {"mv", sourceFile.toString(), destinationDir.toString()};

        // Call the method to move the file
        MyCLI.moveOrRename(args);

        // Verify that the file was moved
        assertFalse(Files.exists(sourceFile), "Source file should no longer exist.");
        assertTrue(Files.exists(destinationDir.resolve("testFile2.txt")), "Moved file should exist in the destination directory.");
    }


    @Test
    void testMoveFileToNonExistingDirectory() {
        // Create a test file
        Path sourceFile = testDir.resolve("testFile.txt");
        try {
            Files.createFile(sourceFile); // Create the source file
        } catch (IOException e) {
            // e.printStackTrace(); // Handle any exceptions during file creation
        }

        // Prepare arguments for moving the file to a non-existing directory
        String[] args = {"mv", sourceFile.toString(), testDir.resolve("nonExistingDir22").toString()};

        // Redirect output to capture the CLI output
        PrintStream originalOut = System.out; // Save the original System.out
        System.setOut(new PrintStream(outputStream)); // Redirect to output stream

        // Call the method to move the file
        MyCLI.moveOrRename(args);

        // Check the output
        String output = outputStream.toString().trim();
        assertEquals("Error: Destination directory does not exist.", output, "Expected error message for non-existing directory.");

        // Reset System.out to the original output stream
        System.setOut(originalOut); // Restore original System.out

        // Clean up: delete the test file if it was created
        try {
            Files.deleteIfExists(sourceFile);
        } catch (IOException e) {
            e.printStackTrace(); // Handle any exceptions during cleanup
        }
    }




    @Test
    void testMoveNonExistingFile() {
        // Prepare arguments for moving a non-existing file
        String[] args = {"mv", testDir.resolve("nonExistingFile.txt").toString(), testDir.resolve("destinationDir").toString()};

        // Call the method to move the file
        MyCLI.moveOrRename(args);

        // Check the output
        String output = outputStream.toString().trim();
        assertEquals("Error: Source file or directory does not exist.", output);
    }

    @Test
    void testMoveFileToExistingFile() throws Exception {
        // Create a test file
        Path sourceFile = testDir.resolve("sourceFile.txt");
        Files.createFile(sourceFile);

        // Create another test file as destination
        Path destinationFile = testDir.resolve("destinationFile.txt");
        Files.createFile(destinationFile);

        // Prepare arguments for moving the file to an existing file
        String[] args = {"mv", sourceFile.toString(), destinationFile.toString()};

        // Call the method to move the file
        MyCLI.moveOrRename(args);

        // Check the output
        String output = outputStream.toString().trim();
        assertEquals("Error: Destination file already exists.", output);
    }

    @Test
    void testMoveDirectorySuccessfully() throws Exception {
        // Create a test directory
        Path sourceDir = testDir.resolve("sourceDir");
        Files.createDirectory(sourceDir);

        // Create a destination directory
        Path destinationDir = testDir.resolve("destinationDir");
        Files.createDirectory(destinationDir);

        // Prepare arguments for moving the directory
        String[] args = {"mv", sourceDir.toString(), destinationDir.toString()};

        // Call the method to move the directory
        MyCLI.moveOrRename(args);

        // Verify that the directory was moved
        assertFalse(Files.exists(sourceDir), "Source directory should no longer exist.");
        assertTrue(Files.exists(destinationDir.resolve("sourceDir")), "Moved directory should exist in the destination directory.");
    }









    @Test
    void testCatBasicFileOutput() throws IOException {
        // Create a test file with content
        Path file = testDir.resolve("testFile.txt");
        Files.writeString(file, "Hello, World!");

        // Set currentDir to testDir in MyCLI (if this is possible in your implementation)
        MyCLI.currentDir.resolve(testDir);  // Update this method according to your MyCLI design

        // Redirect output and clear any previous content
        outputStream.reset();
        System.setOut(new PrintStream(outputStream));

        // Call cat on the test file using absolute path
        MyCLI.cat(new String[]{"cat", file.toAbsolutePath().toString()});

        // Flush the output to ensure all data is captured
        System.out.flush();

        // Verify the output
        String expectedOutput = "Hello, World!";
        assertEquals(expectedOutput, outputStream.toString().trim(),
                "Expected file content to be output");
    }
    @Test
    void testRedirectionToOutputFile() throws IOException {
        // Set currentDir for MyCLI to testDir
        MyCLI.currentDir.resolve(testDir);

        // Create test directory if it doesn't exist
        if (!Files.exists(testDir)) {
            Files.createDirectories(testDir);
        }

        // Create a test input file
        Path inputFile = testDir.resolve("inputFile.txt");
        Files.writeString(inputFile, "Redirected content");

        // Remove the output file if it already exists
        Path outputFile = testDir.resolve("outputFile.txt");
        if (Files.exists(outputFile)) {
            Files.delete(outputFile);
        }
        MyCLI.changeDirectory("testDir");
        // Run the cat command with redirection
        MyCLI.cat(new String[]{"cat", "inputFile.txt", ">", "outputFile.txt"});

        // Verify that content was written to outputFile.txt
        assertTrue(Files.exists(outputFile), "Output file was not created.");
        assertEquals("Redirected content", Files.readString(outputFile).trim());
    }


    @Test
    void testAppendingToFile() throws IOException {
        // Create an initial output file with content
        Path outputFile = testDir.resolve("outputFile.txt");
        Files.writeString(outputFile, "Initial content\n");

        // Create a test input file
        Path inputFile = testDir.resolve("inputFile.txt");
        Files.writeString(inputFile, "Appended content");

        MyCLI.changeDirectory("testDir");

        // Run the cat command with appending redirection
        MyCLI.cat(new String[]{"cat", "inputFile.txt", ">>", "outputFile.txt"});

        // Read the content of the output file for verification
        String actualOutput = Files.readString(outputFile).trim(); // Trim to handle newline consistency
        // System.out.println("Actual Output (Append): " + actualOutput);  // Debugging line

        // Verify that content was appended to outputFile.txt
        assertEquals("Initial content\nAppended content", actualOutput);
    }
    @Test
    void testOverwritingToFile() throws IOException {
        // Create an initial output file with content
        Path outputFile = testDir.resolve("outputFile.txt");
        Files.writeString(outputFile, "Initial content");

        // Create a test input file
        Path inputFile = testDir.resolve("inputFile.txt");
        Files.writeString(inputFile, "Overwritten content");
        MyCLI.changeDirectory("testDir");

        // Run the cat command with overwrite redirection
        MyCLI.cat(new String[]{"cat", "inputFile.txt", ">", "outputFile.txt"});

        // Read the content of the output file for verification
        String actualOutput = Files.readString(outputFile).trim(); // Trim to handle newline consistency
        // System.out.println("Actual Output (Overwrite): " + actualOutput);  // Debugging line

        // Verify that content was overwritten in outputFile.txt
        assertEquals("Overwritten content", actualOutput);
    }


    @Test
    public void testFileNotFound() {
        // Set currentDir if it's not set within MyCLI
        MyCLI.currentDir.resolve(Paths.get(System.getProperty("user.dir"))); // Ensure proper directory context for testing

        // Redirect output to a ByteArrayOutputStream to test console output
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Run the cat command with a non-existing file
        MyCLI.cat(new String[]{"cat", "nonExistingFile.txt"});

        // Get the actual output
        String actualOutput = outputStream.toString().trim(); // Trim to avoid issues with whitespace

        // Debug: Print the actual output for comparison
        //System.out.println("Actual Output: " + actualOutput);

        // Check that the error message was outputted
        assertEquals("Error: File not found.", actualOutput);
    }

    @Test
    void testPipeCommand() throws IOException {
        // Create a test input file
        Path inputFile = testDir.resolve("inputFile.txt");
        Files.writeString(inputFile, "Hello, World!\nThis is a test.");

        // Test piping the output to word count
        MyCLI.cat(new String[]{"cat", "inputFile.txt", "|", "wc"});

        // Verify the output of the wc command.
        // You will need to capture the output to validate it.
    }


}