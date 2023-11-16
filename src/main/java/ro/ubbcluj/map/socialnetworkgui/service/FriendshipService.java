package ro.ubbcluj.map.socialnetworkgui.service;

import ro.ubbcluj.map.socialnetworkgui.domain.Friendship;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.repository.Repository;

import java.util.Optional;

public class FriendshipService implements ServiceInterface<Tuple<Long, Long>, Friendship> {
    private final Repository<Tuple<Long, Long>, Friendship> friendshipRepo;

    public FriendshipService(Repository<Tuple<Long, Long>, Friendship> friendshipRepo) {
        this.friendshipRepo = friendshipRepo;
    }

    @Override
    public boolean addEntity(Friendship entity) {
        return friendshipRepo.save(entity).isEmpty();
    }

    @Override
    public Optional<Friendship> deleteEntity(Friendship entity) {
        return friendshipRepo.delete(entity.getId());
    }

    @Override
    public Iterable<Friendship> getAll() {
        return friendshipRepo.findAll();
    }

    @Override
    public boolean createFriendship(Friendship entity1, Friendship entity2) {
        return addEntity(entity1);
    }

    @Override
    public boolean deleteFriendship(Friendship entity1, Friendship entity2) {
        return deleteEntity(entity1).isPresent();
    }

    @Override
    public Optional<Friendship> updateEntity(Friendship entity) {
        return Optional.empty();
    }

    /**
     * Returneaza o prietenie.
     *
     * @param idUser1: id-ul unui user
     * @param idUser2: id-ul altui user
     * @return prietenia dintre cei 2 useri
     */
    public Optional<Friendship> getFriendship(Long idUser1, Long idUser2) {
        Tuple<Long, Long> f = new Tuple<>(idUser1, idUser2);
        return friendshipRepo.findOne(f);
    }
}
