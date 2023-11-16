package ro.ubbcluj.map.socialnetworkgui.utils;

import java.util.UUID;

/**
 * Clasa ce contine metode care nu tin neaparat de entitatile proiectului.
 * Contine functii generale self made.
 */


public class Utils {
    /**
     * Genereaza un ID unic de tipul Long.
     *
     * @return un id unic
     */
    public static Long generateUniqueLongID() {
        long val = -1;
        do {
            val = UUID.randomUUID().getMostSignificantBits();
        } while (val < 0);
        return val;
    }
}