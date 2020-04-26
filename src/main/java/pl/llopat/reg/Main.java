package pl.llopat.reg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main
{

    public static void main(String[] args) throws IOException, InterruptedException {
        if (!Arrays.asList(7, 8).contains(args.length)) {
            String message = "Need to provide 5 arguments:\n" +
                    "\t-path to template file, e.g. d:\\Essex19inchAAAA.reg\n" +
                    "\t-number of copies\n" +
                    "\t-start value for AAAA, e.g. 05\n" +
                    "\t-start value for BBBB, e.g. 0030\n" +
                    "\t-value for DDDD, e.g. 1111\n" +
                    "\t-value for EEEE, e.g. 2222\n" +
                    "\t-value for GGGG, e.g. 3333\n" +
                    "\t-[Optional] generation delay (in seconds)\n";
            System.out.println(message);
            return;
        }

        Path path = Paths.get(args[0]);
        System.out.println("Reading template from: " + path.toString());
        int noOfCopies = Integer.parseInt(args[1]);
        System.out.println("Will generate " + noOfCopies + " files.");
        String startValueAAAA = args[2];
        int startValueAAAALength = startValueAAAA.length();
        String startValueBBBB = args[3];
        int startValueBBBBLength = startValueBBBB.length();
        String constantValueDDDD = args[4];
        String constantValueEEEE = args[5];
        String constantValueGGGG = args[6];

        System.out.println(String.format("Provided values are AAAA: %s, BBBB: %s ,DDDD: %s, EEEE: %s and GGGG: %s",
                                            startValueAAAA, startValueBBBB, constantValueDDDD, constantValueEEEE, constantValueGGGG));

        int delayInSeconds = 0;
        if (args.length == 8) {
            delayInSeconds = Integer.parseInt(args[7]);
            System.out.println("Delay between out files generation: " + delayInSeconds);
        }

        String templateFileName = path.toFile().getName();

        Scanner scanner = new Scanner(templateFileName);
        if (scanner.findInLine("AAAA") == null) {
            System.out.println("File name does not contain AAAA !");
            return;
        }
        String outputDirectory = path.getParent().toAbsolutePath().toString();
        System.out.println("Result files will be written to " + outputDirectory);

        List<String> template = Files.readAllLines(path, StandardCharsets.UTF_8);

        for (int i = 0; i < noOfCopies; i++) {
            StringBuilder output = new StringBuilder();
            for (String line : template) {
                scanner = new Scanner(line);
                output.append(
                        line
                            .replaceAll("AAAA", calculateValue(startValueAAAA, startValueAAAALength, i))
                            .replaceAll("BBBB", calculateValue(startValueBBBB, startValueBBBBLength, i * 2))
                            .replaceAll("CCCC", calculateValue(startValueBBBB, startValueBBBBLength, i * 2, 1))
                            .replaceAll("DDDD", constantValueDDDD)
                            .replaceAll("EEEE", constantValueEEEE)
                            .replaceAll("GGGG", constantValueGGGG)
                );
                output.append(System.getProperty("line.separator"));
            }
            createFile(outputDirectory,
                    templateFileName.replaceAll("AAAA", calculateValue(startValueAAAA, startValueAAAALength, i)), output.toString());
            Thread.sleep((delayInSeconds * 1000));
        }
    }

    private static String calculateValue(String startValue, int startValueLength, int currentIndex) {
        return calculateValue(startValue, startValueLength, currentIndex, 0);
    }

    private static String calculateValue(String startValue, int startValueLength, int currentIndex, int plus) {
        int currentValue = Integer.parseInt(startValue) + currentIndex + plus;
        int currentValueLength = String.valueOf(currentValue).length();
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < startValueLength - currentValueLength; i++) {
            value.append("0");
        }
        value.append(String.valueOf(Integer.valueOf(currentValue)));
        return value.toString();
    }

    public static void createFile(String path, String fileName, String content) {
        File file = new File(path, fileName);
        try {
            FileOutputStream output = new FileOutputStream(file, true);
            output.write(content.getBytes());
            output.close();
            System.out.println("Wrote to " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create " + file.getAbsolutePath() + " file");
        }
    }
}
