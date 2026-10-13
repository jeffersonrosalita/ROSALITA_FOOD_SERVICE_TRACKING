package Model;

public record User(
        int id,
        String name,
        String password,
        String role,
        String status
) {
}