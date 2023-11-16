package ro.ubbcluj.map.socialnetworkgui.domain.validator;

import ro.ubbcluj.map.socialnetworkgui.domain.Friendship;

public class FriendshipValidator implements Validator<Friendship>{
    /**
     * Verifica daca datele unei prietenii sunt valide.
     * - are valorile din tuplu != null
     * - cele 2 entitati sunt diferite
     * @param entity: un obiect de tip Friendship
     * @throws ValidationException daca prietenia este invalida
     */
    @Override
    public void validate(Friendship entity) throws ValidationException {
        if(entity.getId().getLeft() == null || entity.getId().getRight() == null){
            throw new ValidationException("Id-ul dintr-o prietenie nu poate sa fie valid!");
        }
        if(entity.getId().getLeft().equals(entity.getId().getRight())){
            throw new ValidationException("Cele 2 entitati trebuie sa fie diferite!");
        }
    }
}
