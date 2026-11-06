import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordCheck {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

        String hash = "$2a$12$CSHN00FB2bbM3BRLIT/8seAfUND.Ery58DasnwZFlUx67Nqqk/3oG";

        System.out.println(
            encoder.matches("Qwerty228!", hash)
        );
    }
}
