package ro.ubbcluj.map.socialnetworkgui.domain;

import java.time.LocalDate;

public class Friendship extends Entity<Tuple<Long, Long>> {

    LocalDate date;

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
        return super.toString() + " " + date;
    }
}
