package eu.it.fbk.threedom.pcFilter;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.args4j.*;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    @Argument(required = true, metaVar = "Input file")
    File inFile;

    @Option(name = "-o", metaVar = "output")
    File outFile = new File("");

//    @Option(name = "-p", metaVar = "animation period (ms)")
//    int animationPeriod = 0;

    private void parseArgs(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            // parse the arguments.
            parser.parseArgument(args);

        } catch( CmdLineException e ) {
            // if there's a problem in the command line,
            // you'll getQTB this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.print("Usage: PointCloud Filter");
            parser.printSingleLineUsage(System.err);
            System.err.println();

            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
            System.err.print("animation period set to 0 means no animation");

            // print option sample. This is useful some time
            System.err.println("  Example: pcFilter "+parser.printExample(OptionHandlerFilter.ALL));
            System.exit(1);
        }
    }

    private void run() throws Exception {
        ///////////////////////////////////////////////////////
        // read all lines
        Path path = Paths.get(inFile.toURI());
        List<String> data = Files.readAllLines(path);


        ///////////////////////////////////////////////////////
        // filter the data
        ///////////////////////////////////////////////////////
        //PcFilter pcf = new PcFilter(data, animationPeriod);
        PcFilter pcf = new PcFilter(data, 1);
//        char[][] solution = pcf.solve();

        // convert char array to list of strings
        List<String> dataOut = new ArrayList<>();

//        for (int i = 0; i < solution.length; i++) {
//            StringBuilder line = new StringBuilder();
//            for (int j = 0; j < solution[0].length; j++) {
//                line.append(solution[i][j] + " ");
//            }
//            dataOut.add(line.toString());
//        }


        ///////////////////////////////////////////////////////
        // writing result
        try {
            if(outFile.getName() == "")
                outFile = new File(FilenameUtils.removeExtension(inFile.getName()) + "_result.txt");

            Path out = Paths.get(outFile.toURI());
            Files.createFile(out);
            Files.write(out, dataOut);
        }catch(FileAlreadyExistsException foee){
            System.out.println("\nWARNING! the output file already exists");
        }
    }

    /**
     * Main method
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.parseArgs(args);
        main.run();
    }

}
