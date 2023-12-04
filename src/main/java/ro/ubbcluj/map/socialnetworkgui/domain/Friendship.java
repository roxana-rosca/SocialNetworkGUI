package ro.ubbcluj.map.socialnetworkgui.domain;

import ro.ubbcluj.map.socialnetworkgui.utils.Utils;

import java.time.LocalDate;

public class Friendship extends Entity<Tuple<Long, Long>> {

    LocalDate date;
    FriendRequest frienshipStatus;

    public Friendship() {
        this.date = LocalDate.now();
    }

    public Friendship(LocalDate date){
        this.date = date;
    }

    /**
     * @return data cand s-a creat prietenia
     */
    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return super.toString() + " " + date + " status: " + Utils.statusToString(frienshipStatus);
    }

    public void setFrienshipStatus(FriendRequest status){
        this.frienshipStatus = status;
    }

    public FriendRequest getFrienshipStatus(){
        return this.frienshipStatus;
    }


    public void setDate(LocalDate date) {
        this.date = date;
    }
}

