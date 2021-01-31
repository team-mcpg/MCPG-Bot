package fr.milekat.MCPG_Discord.utils;

public class Numbers {
    public static String getString(int number) {
        switch (number) {
            case 1: {
                return ":one:";
            }
            case 2: {
                return ":two:";
            }
            case 3: {
                return ":three:";
            }
            case 4: {
                return ":four:";
            }
            case 5: {
                return "five:";
            }
            case 6: {
                return ":six:";
            }
            case 7: {
                return ":seven:";
            }
            case 8: {
                return ":eight:";
            }
            case 9: {
                return ":nine:";
            }
        }
        return ":zero:";
    }

    public static int getInt(String number) {
        switch (number) {
            case ":one:": {
                return 1;
            }
            case ":two:": {
                return 2;
            }
            case ":three:": {
                return 3;
            }
            case ":four:": {
                return 4;
            }
            case "five:": {
                return 5;
            }
            case ":six:": {
                return 6;
            }
            case ":seven:": {
                return 7;
            }
            case ":eight:": {
                return 8;
            }
            case ":nine:": {
                return 9;
            }
        }
        return 0;
    }
}
