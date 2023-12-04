package ro.ubbcluj.map.socialnetworkgui.domain.validator;

import ro.ubbcluj.map.socialnetworkgui.domain.Message;

public class MessageValidator implements Validator<Message> {
    @Override
    public void validate(Message entity) throws ValidationException {
        if(entity.getMessage().isEmpty())
            throw new ValidationException("Message can't be empty!");
    }
}
