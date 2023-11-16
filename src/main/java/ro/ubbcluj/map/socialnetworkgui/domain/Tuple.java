package ro.ubbcluj.map.socialnetworkgui.domain;

import java.util.Objects;


/**
 * Defineste un tuplu de elemente generice.
 *
 * @param <E1> - primul tip de entitate
 * @param <E2> - al doilea tip de entitate
 */
public class Tuple<E1, E2> {
    private E1 e1;
    private E2 e2;

    public Tuple(E1 e1, E2 e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public E1 getLeft() {
        return e1;
    }

    public void setLeft(E1 e1) {
        this.e1 = e1;
    }

    public E2 getRight() {
        return e2;
    }

    public void setRight(E2 e2) {
        this.e2 = e2;
    }

    /**
     * Genereaza modul de afisare al unui obiect de tipul Tuple.
     *
     * @return un string ce reprezinta valoarea obiectului Tuple
     */
    @Override
    public String toString() {
        return " " + e1 + "," + e2;

    }

    /**
     * Verifica daca 2 obiecte sunt egale.
     *
     * @param obj: un obiect
     * @return true, daca sunt egale; false, altfel
     */
    @Override
    public boolean equals(Object obj) {
        return this.e1.equals(((Tuple) obj).e1) && this.e2.equals(((Tuple) obj).e2);
    }

    /**
     * Genereaza un hashcode.
     *
     * @return hashcode-ul generat
     */
    @Override
    public int hashCode() {
        return Objects.hash(e1, e2);
    }
}
