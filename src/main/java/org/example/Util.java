package org.example;

import java.util.List;
import java.util.Scanner;


public class Util {
    public static String captureString(Scanner s, String prompt) {
        String out = "";
        while (out.isBlank()) {
            System.out.print(prompt);
            out = s.nextLine();
        }
        return out;
    }
    public static int captureInt(Scanner scanner, String promptMessage) {
        int number = 0;
        boolean valid = false;

        while (!valid) {
            try {
                System.out.println(promptMessage);
                String input = scanner.nextLine();
                number = Integer.parseInt(input);
                valid = true;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }

        return number;
    }

    public static int captureIntWithRange(Scanner s, List<String> printList, int rangeStart, int rangeEnd) {
        boolean captured = false;
        int out = 0;

        while(!captured) {
            try {
                for(String line: printList) {
                    System.out.println(line);
                }
                String input = s.nextLine();
                out = Integer.parseInt(input);

                if (out < rangeStart || out > rangeEnd) {
                    throw new Exception();
                }
                captured = true;
            } catch (Exception e) {
                System.out.println("Enter valid integer in range");
            }
        }
        return out;
    }

    public static double captureDouble(Scanner s, String prompt) {
        double out = -1;
        while (out < 0) {
            System.out.print(prompt);
            String input = s.nextLine();
            try {
                out = Double.parseDouble(input);
                if (out < 0) {
                    System.out.println("Please enter a non-negative number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }
        return out;
    }

    public static boolean captureBoolean(Scanner s, List<String> printList, String trueValue, String falseValue) {
        boolean captured = false;
        boolean out = false;

        while(!captured) {
            try {
                for(String line: printList) {
                    System.out.println(line);
                }
                String value = s.nextLine();
                if (value.equalsIgnoreCase(trueValue)) {
                    out = true;
                } else if (value.equalsIgnoreCase(falseValue)) {
                    out = false;
                } else {
                    throw new Exception();
                }
                captured = true;
            } catch (Exception e) {
                System.out.println("Requires " + trueValue + " or " + falseValue + " as input");
            }
        }
        return out;
    }
}
