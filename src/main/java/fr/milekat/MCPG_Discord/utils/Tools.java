package fr.milekat.MCPG_Discord.utils;

import java.util.Optional;

public class Tools {
    /**
     *      Permet de réduire une Strind de 1 caractère
     *      Source : https://www.xenovation.com/blog/development/java/remove-last-character-from-string-java
     * @param str String à réduire
     * @return String réduite
     */
    public static String remLastChar(String str) {
        return Optional.ofNullable(str)
                .filter(sStr -> sStr.length() != 0)
                .map(sStr -> sStr.substring(0, sStr.length() - 1))
                .orElse(str);
    }
}
