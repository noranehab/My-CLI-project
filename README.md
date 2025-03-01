# My-CLI-project
Project Overview:

Developed in Java using Maven for dependency management.
Provides file system operations, such as navigating directories, listing files, and executing commands.
Uses Java NIO (Path, Files, Paths) for efficient file handling.
The Main.java file acts as the entry point, delegating execution to MyCLI.java.
Supports interactive user input with a command system.

Testing with JUnit 5:

Uses JUnit 5 for unit testing, ensuring command execution and file operations work correctly.
Implements assertions (assertTrue, assertFalse, assertEquals) to verify expected outcomes.
Captures console output using ByteArrayOutputStream for testing command responses.
Uses temporary test directories (testDir) to simulate file system operations without affecting real files.
Employs reflection techniques to validate internal state changes.
