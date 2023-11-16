package ro.ubbcluj.map.socialnetworkgui.service;

import ro.ubbcluj.map.socialnetworkgui.domain.Entity;

import java.util.Optional;

public interface ServiceInterface<ID, E extends Entity<ID>> {

    /**
     * Adauga o entitate.
     *
     * @param entity: un obiect de tip Entity
     * @return true daca entity a fost adaugat cu succes; false, altfel
     */
    boolean addEntity(E entity);

    /**
     * Sterge o entitate.
     *
     * @param entity: entitatea care se va sterge
     * @return entitatea, daca a fost stearsa cu succes; null, daca nu a fost stearsa
     */
    Optional<E> deleteEntity(E entity);

    /**
     * Returneaza un iterator.
     *
     * @return un iterator care refera obiecte de tipul Entity
     */
    Iterable<E> getAll();

    /**
     * Creeaza o prietenie.
     *
     * @param entity1: o entitate
     * @param entity2: o entitatr
     * @return true, daca s-a format prietenia; false, altfel
     */
    boolean createFriendship(E entity1, E entity2);

    /**
     * Sterge o prietenie.
     *
     * @param entity1: o entitate
     * @param entity2: o entitate
     * @return true, daca s-a sters prietenia, false altfel
     */
    boolean deleteFriendship(E entity1, E entity2);

    /**
     * Modifica entitatea entity
     *
     * @param entity: o entitate
     * @return Optional.empty() daca a fost updated, Optional.of(entity) altfel
     */
    Optional<E> updateEntity(E entity);
}
