package model;

import io.qameta.allure.internal.shadowed.jackson.annotation.JsonIgnoreProperties;
import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import lombok.Value;

@JsonIgnoreProperties
@Value
@AllArgsConstructor
public class User {
    private String email;
    private String password;
    private String name;

    public static String generationEmail() {
        Faker faker = new Faker();
        return faker.internet().emailAddress();
    }
    public static String generationPass() {
        Faker faker = new Faker();
        return faker.internet().password(6, 8);
    }
    public static String generationName() {
        Faker faker = new Faker();
        return faker.name().firstName();
    }
}