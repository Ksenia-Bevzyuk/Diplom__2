import client.StellarBurgersClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.Credentials;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class LoginUserTest {
    private User user;
    private StellarBurgersClient client;
    private Credentials credentials;
    private ValidatableResponse response;
    private String genEmail;

    @Before
    public void before() {
        genEmail = User.generationEmail();

        user = new User(genEmail, "password", "Павел");
        client = new StellarBurgersClient("");
        response = client.createUser(user);
        assumeTrue(response.extract().statusCode() == SC_OK);
    }

    @Test
    @DisplayName("POST /api/auth/login успешный вход под существующим пользователем")
    @Description("Успешный вход при передаче в body существующих email и password")
    public void userCorrectLoginSuccess() {
        credentials = Credentials.fromUser(user);
        response = client.loginUser(credentials);

        response.assertThat().statusCode(SC_OK).body("success", equalTo(true));
    }

    @Test
    @DisplayName("POST /api/auth/login попытка входа под несуществующим пользователем с некорректным email")
    @Description("Код 401 при передаче в body несуществующего email и корректного password")
    public void userLoginWithIncorrectEmailAndCorrectPass401Test() {
        credentials = new Credentials(genEmail + "q", "password");
        response = client.loginUser(credentials);

        int statusCode = response.extract().statusCode();
        boolean success = response.extract().jsonPath().getBoolean("success");
        String message = response.extract().jsonPath().getString("message");
        assertEquals("Ожидается код ответа 401", SC_UNAUTHORIZED, statusCode);
        assertEquals("Ожидается false", false, success);
        assertEquals("Ожидается другое сообщение",
                "email or password are incorrect", message);
    }

    @Test
    @DisplayName("POST /api/auth/login попытка входа под несуществующим пользователем с некорректным password")
    @Description("Код 401 при передаче в body существующего email и некорректного password")
    public void userLoginWithCorrectEmailAndIncorrectPass401Test() {
        credentials = new Credentials(genEmail, "pass");
        response = client.loginUser(credentials);

        int statusCode = response.extract().statusCode();
        boolean success = response.extract().jsonPath().getBoolean("success");
        String message = response.extract().jsonPath().getString("message");
        assertEquals("Ожидается код ответа 401", SC_UNAUTHORIZED, statusCode);
        assertEquals("Ожидается false", false, success);
        assertEquals("Ожидается другое сообщение",
                "email or password are incorrect", message);
    }

    @Test
    @DisplayName("POST /api/auth/login попытка входа под несуществующим пользователем с email null")
    @Description("Код 401 при передаче в body email null и корректного password")
    public void userLoginWithNullEmailAndCorrectPass401Test() {
        credentials = new Credentials(null, "password");
        response = client.loginUser(credentials);

        int statusCode = response.extract().statusCode();
        boolean success = response.extract().jsonPath().getBoolean("success");
        String message = response.extract().jsonPath().getString("message");
        assertEquals("Ожидается код ответа 401", SC_UNAUTHORIZED, statusCode);
        assertEquals("Ожидается false", false, success);
        assertEquals("Ожидается другое сообщение",
                "email or password are incorrect", message);
    }

    @Test
    @DisplayName("POST /api/auth/login попытка входа под несуществующим пользователем с password null")
    @Description("Код 401 при передаче в body существующего email и password null")
    public void userLoginWithCorrectEmailAndPassNull401Test() {
        credentials = new Credentials(genEmail, null);
        response = client.loginUser(credentials);

        int statusCode = response.extract().statusCode();
        boolean success = response.extract().jsonPath().getBoolean("success");
        String message = response.extract().jsonPath().getString("message");
        assertEquals("Ожидается код ответа 401", SC_UNAUTHORIZED, statusCode);
        assertEquals("Ожидается false", false, success);
        assertEquals("Ожидается другое сообщение",
                "email or password are incorrect", message);
    }
    @After
    @DisplayName("DELETE /api/auth/user")
    @Description("Удаление записей о пользователе после каждого теста")
    public void deleteUser() {
        if (response.extract().statusCode() == SC_OK) {
            String accessToken = client.getAccessToken(response);
            StellarBurgersClient clientForDelete = new StellarBurgersClient(accessToken);
            ValidatableResponse responseDelUser = clientForDelete.deleteUser();
            responseDelUser.assertThat().statusCode(SC_ACCEPTED);
        }
    }
}