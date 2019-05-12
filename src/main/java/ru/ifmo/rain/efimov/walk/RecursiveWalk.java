package ru.ifmo.rain.efimov.walk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Two input arg: <br/>
 * 1. Path to file with list of files or directories to walk <br/>
 * 2. File name with result (will created if not exists). <br/>
 * <p>
 * Result of program is list of hex hash of files and files in directories in format:
 * 'hex hash' 'path to file' (without quotes with space). FNVHash used.
 */
public class RecursiveWalk {

    private static final String START_OF_CMD_MSG = "------------------------RECURSIVE_WALK_MSG: ";
    private static final String START_OF_CMD_ERROR_MSG = "------------------------RECURSIVE_WALK_ERROR: ";

    public static void main(String[] args) {
        try {
            mainLogic(args);
        } catch (Exception t) {
            cmdErr(t);
        }
    }

    private static void mainLogic(String[] args) {
        if (null ==args || args.length != 2) {
            cmdMsg("command format: " +
                    "\n\tWalk <input file> <output file>");
        } else {
            Path[] files = new Path[2];
            try {
                files[0] = Paths.get(args[0]);
                files[1] = Paths.get(args[1]);
            } catch (InvalidPathException e) {
                cmdMsg("Invalid path given");
                return;
            }
            if (Files.notExists(files[0])) {
                cmdMsg("Input file doesn't exist");
                return;
            }
            try {
                walk(files[0], files[1]);
            } catch (IOException e) {
                cmdErr(e);
            }
        }
    }

    private static void walk(Path in, Path out) throws IOException {
        new RecursiveWalker().walk(in, out);
    }

    private static void cmdMsg(String m) {
        System.out.println(START_OF_CMD_MSG + m);
    }

    private static void cmdErr(Throwable t) {
        System.err.println(START_OF_CMD_ERROR_MSG + t.getMessage());
    }

}