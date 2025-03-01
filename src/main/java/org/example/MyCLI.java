package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class MyCLI {

    public static Path currentDir = Paths.get(System.getProperty("user.dir")); // Current directory

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String command;

        System.out.println("Welcome to the CLI! Type 'help' for a list of commands.");

        while (true) {
            System.out.print("> ");
            command = scanner.nextLine().trim();
            if (command.isEmpty()) continue; // Skip empty input

            String[] parts = command.split("\\s+"); // Split by whitespace
            String cmd = parts[0];



            try {
                switch (cmd) {
                    case "mv":
                        moveOrRename(parts);
                        break;
                    case "exit":
                        System.out.println("Exiting CLI...");
                        return;
                    case "help":
                        displayHelp();
                        break;
                    case "pwd":
                        System.out.println(currentDir.toString());
                        break;
                    case "cd":
                        if (parts.length > 1) {
                            changeDirectory(parts[1]);
                        } else {
                            System.out.println("Usage: cd <directory>");
                        }
                        break;
                    case "ls":
                        listDirectory(parts);
                        break;
                    case "mkdir":
                        if (parts.length > 1) {
                            createDirectory(parts[1]);
                        } else {
                            System.out.println("Usage: mkdir <directory_name>");
                        }
                        break;
                    case "rmdir":
                        if (parts.length > 1) {
                            removeDirectory(parts[1]);
                        } else {
                            System.out.println("Usage: rmdir <directory_name>");
                        }
                        break;
                    case "touch":
                        if (parts.length > 1) {
                            createFile(parts[1]);
                        } else {
                            System.out.println("Usage: touch <file_name>");
                        }
                        break;
                    case "rm":
                        if (parts.length > 1) {
                            removeFile(parts[1]);
                        } else {
                            System.out.println("Usage: rm <file_name>");
                        }
                        break;
                    case "cat":
                        cat(parts);
                        break;
                    case "less":
                        if (parts.length > 1) {
                            less(parts);
                        } else {
                            System.out.println("Usage: less <file_name>");
                        }
                    case "wc":
                        if (parts.length > 1) {
                            wc(parts);
                        }
                        else {
                            System.out.println("Usage: wc <file_name>");
                        }
                        break;
                    default:
                        System.out.println("Invalid command. Type 'help' for a list of commands.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // Display help for commands
    public static void displayHelp() {
        System.out.println("Supported commands:");
        System.out.println("  pwd        - Show current directory path");
        System.out.println("  cd <dir>   - Change directory to <dir>");
        System.out.println("  ls         - List files and directories in the current directory");
        System.out.println("  mkdir <dir>- Create a new directory");
        System.out.println("  rmdir <dir>- Remove an empty directory");
        System.out.println("  touch <file>- Create a new file");
        System.out.println("  rm <file>  - Remove a file");
        System.out.println("  cat <file> - Display contents of a file");
        System.out.println("  mv <source...> <dest> - Move/rename files");
        System.out.println("  exit       - Exit the CLI");
        System.out.println("  help       - Show this help message");
    }

    public static void changeDirectory(String dir) {
        Path newPath = currentDir.resolve(dir).normalize();
        System.out.println("Attempting to change to: " + newPath); // Debugging line
        if (Files.isDirectory(newPath)) {
            currentDir = newPath;
            System.out.println("Changed directory to: " + currentDir);
        } else {
            System.out.println("Directory not found: " + dir);
        }
    }


    public static void listDirectory(String[] parts) {
        Path directory = currentDir; // Default to the current directory

        // Check if a directory path is provided in parts[1]
        if (parts.length > 1 && !parts[1].startsWith("-")) {
            directory = Paths.get(parts[1]);
        }

        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(directory);
            List<Path> items = new ArrayList<>();

            for (Path entry : stream) {
                // Check if -a is specified in the command
                boolean includeHidden = parts.length > 1 && parts[1].equals("-a");

                // Add files to the list based on the presence of -a flag
                if (includeHidden || !entry.getFileName().toString().startsWith(".")) {
                    items.add(entry.getFileName()); // Include or exclude hidden files based on the flag
                }
            }

            // Process options for listing
            if (parts.length > 2) {
                if (parts[2].equals("-r")) {
                    Collections.reverse(items);
                    items.forEach(System.out::println); // Print in reverse order
                } else {
                    System.out.println("Invalid option for ls");
                }
            } else if (parts.length > 1 && parts[1].equals("-r")) {
                Collections.reverse(items);
                items.forEach(System.out::println); // Reverse order if only -r is specified
            } else {
                items.forEach(System.out::println); // Default behavior: list normally
            }

        } catch (IOException e) {
            System.out.println("Error listing directory: " + e.getMessage());
        } catch (InvalidPathException e) {
            System.out.println("Invalid directory path provided.");
        }
    }



    public static void createDirectory(String dirName) {
        Path newDir = currentDir.resolve(dirName);
        System.out.println("Attempting to create directory at: " + newDir.toString());
        try {
            Files.createDirectory(newDir);
            System.out.println("Directory created: " + dirName);
        } catch (IOException e) {
            e.printStackTrace(); // Print stack trace for more details
            System.out.println("Could not create directory: " + e.getMessage());
        }
    }

    public static Path getCurrentDir() {
        return currentDir;
    }

    public static void removeDirectory(String dirName) {
        Path dir = currentDir.resolve(dirName);
        try {
            Files.delete(dir);
            System.out.println("Directory removed: " + dirName);
        } catch (IOException e) {
            System.out.println("Could not remove directory: " + e.getMessage());
        }
    }

    public static void createFile(String fileName) {
        Path file = currentDir.resolve(fileName);
        try {
            Files.createFile(file);
            System.out.println("File created: " + fileName);
        } catch (IOException e) {
            System.out.println("Could not create file: " + e.getMessage());
        }
    }

    public static void removeFile(String fileName) {
        Path file = currentDir.resolve(fileName);
        try {
            Files.delete(file);
            System.out.println("File removed: " + fileName);
        } catch (IOException e) {
            System.out.println("Could not remove file: " + e.getMessage());
        }
    }

    public static void displayFile(String fileName) {
        Path file = currentDir.resolve(fileName);
        try {
            List<String> lines = Files.readAllLines(file);
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
        }
    }
    public static void moveOrRename(String[] args) {
        if (args.length < 3) {
            System.out.println("Error: Source and destination paths required.");
            return;
        }

        // Resolve source path
        Path src = Paths.get(args[1]).isAbsolute() ? Paths.get(args[1]) : currentDir.resolve(args[1]);

        // Resolve destination path
        Path dest = Paths.get(args[2]);

        // Check if the source file exists
        if (!Files.exists(src)) {
            System.out.println("Error: Source file or directory does not exist.");
            return;
        }

        // Check if the destination exists
        if (Files.exists(dest)) {
            if (Files.isDirectory(dest)) {
                // If the destination is a directory, construct a new path for the file inside that directory
                dest = dest.resolve(src.getFileName());
            } else {
                // If the destination file already exists, print an error message
                System.out.println("Error: Destination file already exists.");
                return;
            }
        } else {
            // If the destination does not exist, check if its parent directory exists

            System.out.println("Error: Destination directory does not exist.");
            return;

        }

        try {
            // Move or rename the file/directory
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Moved/Renamed " + src.getFileName() + " to " + dest.getFileName());
        } catch (NoSuchFileException e) {
            System.out.println("Error: Source file or directory does not exist.");
        } catch (FileAlreadyExistsException e) {
            System.out.println("Error: Destination file already exists.");
        } catch (DirectoryNotEmptyException e) {
            System.out.println("Error: Destination directory is not empty.");
        } catch (IOException e) {
            System.out.println("Error: Could not move/rename file or directory. " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
    }




    public static void cat(String[] args) {
        // Check if there are enough arguments
        if (args.length < 2) {
            System.out.println("Error: No file specified.");
            return;
        }

        String commandPart = String.join(" ", args); // Combine args to a single string
        String[] parts;

        // Check for pipe operator
        if (commandPart.contains("|")) {
            // Split by '|' to handle piping
            parts = commandPart.split("\\|", 2);
            String filePart = parts[0].trim(); // This will be the input for cat
            String nextCommandPart = parts[1].trim(); // This is the command that follows the pipe

            // Handle the cat command and read the file
            Path catFilePath = currentDir.resolve(filePart.split("\\s+")[1]);
            if (Files.isRegularFile(catFilePath)) {
                try (BufferedReader reader = Files.newBufferedReader(catFilePath)) {
                    StringBuilder outputBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputBuilder.append(line).append("\n"); // Collect the output
                    }

                    // Now pass this output to the next command
                    String output = outputBuilder.toString();
                    executeNextCommand(nextCommandPart, output);
                } catch (IOException e) {
                    System.out.println("Error: Could not read file.");
                }
            } else {
                System.out.println("Error: Source file not found.");
            }
        } else {
            // Check for redirection operator
            if (commandPart.contains(">")) {
                // Handle output redirection
                parts = commandPart.split(">", 2);
                String filePart = parts[1].trim();
                boolean appendMode = false;

                // Determine if it's an append or overwrite
                if (filePart.startsWith(">")) {
                    appendMode = true; // it's a double arrow
                    filePart = filePart.substring(1).trim(); // Remove leading '>'
                }

                // Handle the redirection
                Path outputPath = currentDir.resolve(filePart);
                try (BufferedWriter writer = Files.newBufferedWriter(outputPath,
                        StandardOpenOption.CREATE,appendMode ? StandardOpenOption.APPEND :
                                StandardOpenOption.TRUNCATE_EXISTING)) {
                    Path inputFilePath = currentDir.resolve(args[1]);
                    if (Files.isRegularFile(inputFilePath)) {
                        try (BufferedReader reader = Files.newBufferedReader(inputFilePath)) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                writer.write(line);
                                writer.newLine(); // Write to output file
                            }
                        }
                        System.out.println("Output written to " + outputPath);
                    } else {
                        System.out.println("Error: Source file not found.");
                    }
                } catch (IOException e) {
                    System.out.println("Error: Unable to write to output file.");
                }
            } else {
                // Original behavior for cat command without redirection
                Path filePath = currentDir.resolve(args[1]);

                // Check if the file exists
                if (!Files.exists(filePath)) {
                    System.out.println("Error: File not found.");
                    return;
                }

                // Attempt to read and display the contents of the file
                try (BufferedReader reader = Files.newBufferedReader(filePath)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line); // Output each line
                    }
                } catch (IOException e) {
                    System.out.println("Error: Could not read file.");
                }
            }
        }
    }


    private static void wc(String[] args) {
        // Check if there are enough arguments
        if (args.length < 2) {
            System.out.println("Error: No file specified.");
            return;
        }

        Path filePath = Paths.get(currentDir.toString(), args[1]); // Resolve the file path

        try {
            // Read all lines from the file
            List<String> lines = Files.readAllLines(filePath);

            // Initialize the word count
            int wordCount = 0;

            // Count words in each line
            for (String line : lines) {
                // Trim the line and count the words
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    // Split by whitespace and count
                    wordCount += trimmedLine.split("\\s+").length;
                }
            }

            // Print the total word count
            System.out.println("Word Count: " + wordCount);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }



    private static void less(String[] args) {
        if (args.length < 2) {
            System.out.println("Error: No file specified for less.");
            return;
        }

        Path filePath = currentDir.resolve(args[1]);
        if (!Files.exists(filePath)) {
            System.out.println("Error: File not found.");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            int linesPerPage = 10; // Adjust this value to your preference
            int lineCount = 0;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                lineCount++;
                if (lineCount % linesPerPage == 0) {
                    System.out.print("Press Enter to continue...");
                    new Scanner(System.in).nextLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Error: Could not read file for less.");
        }
    }


    private static void executeNextCommand(String command, String input) {
        if (command.equals("wc")) {
            int wordCount = input.split("\\s+").length;
            System.out.println("Word Count: " + wordCount);
        } else if (command.equals("grep")) {
            // Example: Filter lines containing 'searchTerm'
            String searchTerm = input.split("\\s+")[1]; // Assume the second part is the search term
            Arrays.stream(input.split("\n"))
                    .filter(line -> line.contains(searchTerm))
                    .forEach(System.out::println);
        } else if (command.equals("less")) {
            // Call less command with the provided input
            System.out.println("Passing input to less command...");
            less(input.split("\\s+")); // Pass the whole input for less command
        } else {
            System.out.println("Error: Unsupported command after pipe.");
        }
    }
    public static String sortOutput(String output) {
        if (output == null) return "Error: No input provided for sort.";

        String[] lines = output.split("\n");
        Arrays.sort(lines);
        return String.join("\n", lines);
    }

}