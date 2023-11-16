package ro.ubbcluj.map.socialnetworkgui.domain.validator;

import ro.ubbcluj.map.socialnetworkgui.domain.User;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.ValidationException;
import ro.ubbcluj.map.socialnetworkgui.domain.validator.Validator;

public class UserValidator implements Validator<User> {

    /**
     * Verifica daca datele lui entity sunt valide.
     *
     * @param entity: un User
     * @throws ValidationException daca entity contine date invalide.
     */
    @Override
    public void validate(User entity) throws ValidationException {
        validateName(entity.getFirstName(), NameType.FIRSTNAME);
        validateName(entity.getLastName(), NameType.LASTNAME);
    }

    /**
     * Verifica daca firstName este valid:
     * - nu este null
     * - are mai putin de 100 de caractere
     * - are lungimea mai mare decat 0
     * - primul caracter este o litera
     *
     * @param name: numele unui User
     * @throws ValidationException daca prenumele este invalid
     */
    private void validateName(String name, NameType type) throws ValidationException {
        String nameType = "";
        if (type == NameType.FIRSTNAME)
            nameType = "First name ";
        else if (type == NameType.LASTNAME)
            nameType = "Last Name ";

        if (name == null)
            throw new ValidationException(nameType + "can't be null!");
        if (name.length() > 100)
            throw new ValidationException(nameType + "can't have more than 100 characters!");
        if (name.isEmpty())
            throw new ValidationException(nameType + "can't be empty!");
        if (!Character.isAlphabetic(name.charAt(0)))
            throw new ValidationException("First character of the " + nameType + "has to be a letter!");
    }

    private enum NameType {
        FIRSTNAME, LASTNAME;
    }

}
