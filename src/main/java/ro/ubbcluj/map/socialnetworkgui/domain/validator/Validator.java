package ro.ubbcluj.map.socialnetworkgui.domain.validator;

public interface Validator<T> {
    void validate(T entity) throws ValidationException; // exceptie verificata
}
