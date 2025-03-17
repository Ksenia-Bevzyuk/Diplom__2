package model;

import io.qameta.allure.internal.shadowed.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Value;

@JsonIgnoreProperties
@Value
@AllArgsConstructor
public class Credentials {
    private String email;
    private String password;

    public static Credentials fromUser(User user) {
        return new Credentials(user.getEmail(), user.getPassword());
    }
}