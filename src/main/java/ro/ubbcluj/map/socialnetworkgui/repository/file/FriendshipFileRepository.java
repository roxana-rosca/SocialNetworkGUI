package ro.ubbcluj.map.socialnetworkgui.repository.file;

import ro.ubbcluj.map.socialnetworkgui.domain.Friendship;
import ro.ubbcluj.map.socialnetworkgui.domain.Tuple;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.Validator;

import java.util.List;

public class FriendshipFileRepository extends AbstractFileRepository<Tuple<Long, Long>, Friendship> {
    public FriendshipFileRepository(String fileName, Validator<Friendship> validator) {
        super(fileName, validator);
    }

    @Override
    public Friendship extractEntity(List<String> attributes) {
        Friendship newFriendship = new Friendship();
        newFriendship.setId(new Tuple<Long,Long>(
                Long.parseLong(attributes.get(0)),
                Long.parseLong(attributes.get(1))
        ));

        return newFriendship;
    }

    @Override
    protected String createEntityAsString(Friendship entity) {
        return entity.getId().getLeft() + ";" + entity.getId().getRight()+";"+entity.getDate();
    }
}
