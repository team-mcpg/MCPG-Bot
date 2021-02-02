package fr.milekat.MCPG_Discord.utils;

public class Numbers {
    public static String getString(int number) {
        switch (number) {
            case 1: {
                return "1️⃣";
            }
            case 2: {
                return "2️⃣";
            }
            case 3: {
                return "3️⃣";
            }
            case 4: {
                return "4️⃣";
            }
            case 5: {
                return "5️⃣";
            }
            case 6: {
                return "6️⃣";
            }
            case 7: {
                return "7️⃣";
            }
            case 8: {
                return "8️⃣";
            }
            case 9: {
                return "9️⃣";
            }
        }
        return ":zero:";
    }

    public static int getInt(String number) {
        switch (number) {
            case "1️⃣": {
                return 1;
            }
            case "2️⃣": {
                return 2;
            }
            case "3️⃣": {
                return 3;
            }
            case "4️⃣": {
                return 4;
            }
            case "5️⃣": {
                return 5;
            }
            case "6️⃣": {
                return 6;
            }
            case "7️⃣": {
                return 7;
            }
            case "8️⃣": {
                return 8;
            }
            case "9️⃣": {
                return 9;
            }
        }
        return 0;
    }
}
