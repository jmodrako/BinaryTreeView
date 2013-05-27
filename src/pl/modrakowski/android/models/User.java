package pl.modrakowski.android.models;

/**
 * User: Jack Modrakowski
 * Date: 5/27/13
 * Time: 4:30 PM
 * Departament: IT Mobile
 * Company: Implix
 */
public class User {
    public int userId;
    public int parentId;
    public String firstName;
    public String lastName;
    public String description;
    public String role;
    public int image;
    public int placement;

    public User(int userId, int parentId, String firstName, String lastName, String description, String role, int image, int placement) {
        this.userId = userId;
        this.parentId = parentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.description = description;
        this.role = role;
        this.image = image;
        this.placement = placement;
    }
}
