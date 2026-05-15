package edu.nju;

import org.apache.commons.cli.*;

public class CommandLineUtil {
    private static CommandLine commandLine;
    private static CommandLineParser parser = new DefaultParser();
    private static Options options = new Options();
    private boolean sideEffect;
    public static final String WRONG_MESSAGE = "Invalid input.";

    /**
     * you can define options here
     * or you can create a func such as [static void defineOptions()] and call it
     * before parse input
     */
    static {
        options.addOption(new Option("h", "help", false, "print help message"));
        options.addOption(new Option("p", "print", true, "print arg"));
        options.addOption(new Option("s", false, "side effect"));
    }

    /**
     * step1 add some option rules (you can do it in static{})
     * step2 parse the input
     * step3 handle options
     * 
     * @param args input of program
     */
    public void main(String[] args) {
        parseInput(args);
        handleOptions();
    }

    /**
     * Print the usage of all options
     * Actually, you can print anything to pass the test
     * but you are recommended to use HelpFormatter to see what will happen
     */
    private static void printHelpMessage() {
        System.out.println("help");
    }

    /**
     * Parse the input and handle exception
     * 
     * @param args origin args form input
     */
    public void parseInput(String[] args) {
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * You can handle options here or create your own func
     */
    public void handleOptions() {
        if (commandLine.hasOption("h")) {
            printHelpMessage();
            return;
        }

        if (commandLine.getArgs().length == 0) {
            System.out.println(WRONG_MESSAGE);
            return;
        }

        if (commandLine.hasOption("s")) {
            this.sideEffect = true;
        }

        if (commandLine.hasOption("p")) {
            System.out.println(commandLine.getOptionValue("p"));
        }
    }

    /**
     * edit to return the actual side effect flag
     */
    public boolean getSideEffectFlag() {
        return this.sideEffect;
    }

}