import client.StellarBurgersClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.Credentials;
import model.UpdateUser;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

public class ChangeUserDataTest {
    private User user;
    private StellarBurgersClient clientLogin;
    private StellarBurgersClient client;
    private Credentials credentials;
    private ValidatableResponse responseCreateUser;
    private ValidatableResponse responseLoginUser;
    private String genEmail;

    @Before
    public void before() {
        genEmail = User.generationEmail();

        user = new User(genEmail, "password", "Павел");
        client = new StellarBurgersClient("");
        responseCreateUser = client.createUser(user);
        assumeTrue(responseCreateUser.extract().statusCode() == SC_OK);

        credentials = Credentials.fromUser(user);
        responseLoginUser = client.loginUser(credentials);
        assumeTrue(responseLoginUser.extract().statusCode() == SC_OK);

        String accessToken = client.getAccessToken(responseCreateUser);
        clientLogin = new StellarBurgersClient(accessToken);
    }

    @Test
    @DisplayName("PATCH /api/auth/user изменение name")
    @Description("Изменение name авторизованного пользователя")
    public void changeNameLoginUserTest() {
        String changeUserName = clientLogin.genJson("name", "Павел");
        ValidatableResponse response = clientLogin.changeUserData(changeUserName);
        response.assertThat().statusCode(SC_OK).body("success", equalTo(true));
        String name = response.extract().body().as(UpdateUser.class).getUser().getName();
        assertEquals("Павелq", name);
    }

    @Test
    @DisplayName("PATCH /api/auth/user изменение email")
    @Description("Изменение email авторизованного пользователя")
    public void changeEmailLoginUserTest() {
        String changeUserEmail = clientLogin.genJson("email", genEmail);
        ValidatableResponse response = clientLogin.changeUserData(changeUserEmail);
        response.assertThat().statusCode(SC_OK).body("success", equalTo(true));
        String email = response.extract().body().as(UpdateUser.class).getUser().getEmail();
        assertEquals(genEmail + "q", email);
    }

    @Test
    @DisplayName("PATCH /api/auth/user изменение name")
    @Description("Изменение name неавторизованного пользователя")
    public void changeNameUnauthUserTest() {
        String changeUserName = client.genJson("name", "Павел");
        ValidatableResponse response = client.changeUserData(changeUserName);
        int statusCode = response.extract().statusCode();
        boolean success = response.extract().jsonPath().getBoolean("success");
        String message = response.extract().jsonPath().getString("message");
        assertEquals("Ожидается код ответа 401", SC_UNAUTHORIZED, statusCode);
        assertEquals("Ожидается false", false, success);
        assertEquals("Ожидается другое сообщение",
                "You should be authorised", message);
    }

    @Test
    @DisplayName("PATCH /api/auth/user изменение email")
    @Description("Изменение email неавторизованного пользователя")
    public void changeEmailUnauthUserTest() {
        String changeUserEmail = client.genJson("email", genEmail);
        ValidatableResponse response = client.changeUserData(changeUserEmail);
        int statusCode = response.extract().statusCode();
        boolean success = response.extract().jsonPath().getBoolean("success");
        String message = response.extract().jsonPath().getString("message");
        assertEquals("Ожидается код ответа 401", SC_UNAUTHORIZED, statusCode);
        assertEquals("Ожидается false", false, success);
        assertEquals("Ожидается другое сообщение",
                "You should be authorised", message);
    }

    @After
    @DisplayName("DELETE /api/auth/user")
    @Description("Удаление записей о пользователе после каждого теста")
    public void deleteUser() {
        if (responseCreateUser.extract().statusCode() == SC_OK) {
            String accessToken = clientLogin.getAccessToken(responseCreateUser);
            StellarBurgersClient clientForDelete = new StellarBurgersClient(accessToken);
            ValidatableResponse responseDelUser = clientForDelete.deleteUser();
            responseDelUser.assertThat().statusCode(SC_ACCEPTED);
        }
    }
}
