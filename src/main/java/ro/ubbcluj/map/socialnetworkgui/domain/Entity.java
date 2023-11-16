package ro.ubbcluj.map.socialnetworkgui.domain;

import java.io.Serializable;
import java.util.Objects;

public class Entity<ID> implements Serializable {

    // private static final long serialVersionUID = 7331115341259248461L;
    protected ID id;

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    /**
     * Verifica daca entitatea este egala cu obiectul o.
     *
     * @param o: un obiect
     * @return true, daca entitatea este egala cu o; false, altfel
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity)) return false;
        Entity<?> entity = (Entity<?>) o; // wildcard
        return getId().equals(entity.getId());
    }

    /**
     * Genereaza un hashcode.
     *
     * @return hashcode-ul generat
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /**
     * Genereaza modul de afisare a unei entitati.
     *
     * @return un string ce reprezinta afisarea unei entitati
     */
    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                '}';
    }
}