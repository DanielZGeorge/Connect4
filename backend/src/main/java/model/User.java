package model;

public class User {
    private int userId;
    private String username;
    private String email;
    private int eloRating;

    public User(int userId, String username, String email, int eloRating) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.eloRating = eloRating;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getEloRating() {
        return eloRating;
    }
}