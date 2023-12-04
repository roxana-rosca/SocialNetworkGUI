package ro.ubbcluj.map.socialnetworkgui.utils;

import ro.ubbcluj.map.socialnetworkgui.domain.FriendRequest;

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

    public static FriendRequest decideStatus(String status){
        return switch (status) {
            case "accepted" -> FriendRequest.ACCEPTED;
            case "rejected" -> FriendRequest.REJECTED;
            case "pending" -> FriendRequest.PENDING;
            default -> null;
        };
    }

    public static String statusToString(FriendRequest status){
        return switch (status){
            case ACCEPTED -> "accepted";
            case PENDING -> "pending";
            case REJECTED -> "rejected";
        };
    }
}