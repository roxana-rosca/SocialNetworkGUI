package ro.ubbcluj.map.socialnetworkgui.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User extends Entity<Long> {
    private String firstName;
    private String lastName;
    private String userName;
    private List<User> friends;
    private Integer noFriends;

    public User(String firstName, String lastName, String userName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.friends = new ArrayList<>();
        noFriends = 0;
    }

    public void addFriend(User friend) {
        friends.add(friend);
    }

    public void deleteFriend(User friend) {
        friends.remove(friend);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<User> getFriends() {
        return friends;
    }

    public Integer getNoFriends(){
        this.noFriends = friends.size();
        return noFriends;
    }

    /**
     * Genereaza modul de afisare al unui User.
     *
     * @return un string ce reprezinta User-ul
     */
    @Override
    public String toString() {
        return firstName + ' ' +
                lastName +
                ", friends=" + friends.size() + '\'' +
                ", username=@" + userName ;
    }


    /**
     * Verifica daca 2 obiecte sunt egale.
     *
     * @param o: un obiect
     * @return true, daca cele 2 obiecte sunt egale; false, altfel
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User that = (User) o;
        return getFirstName().equals(that.getFirstName()) &&
                getLastName().equals(that.getLastName()) &&
                //getFriends().equals(that.getFriends()) &&
                getUserName().equals(that.getUserName());
    }

    /**
     * Genereaza un hashcode.
     *
     * @return hashcode-ul generat
     */
    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getUserName());
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }
}