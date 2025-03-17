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
import static org.junit.Assume.assumeTrue;

public class ChangeUserDataTest {
    private User user;
    private StellarBurgersClient clientLogin;
    private StellarBurgersClient client;
    private Credentials credentials;
    private ValidatableResponse responseCreateUser;
    private ValidatableResponse responseLoginUser;
    private String genEmail;
    private String genPass;
    private String genName;

    @Before
    @DisplayName("Перед каждым тестом")
    @Description("Генерация данных для создания пользователя," +
            "создание пользователя и вход перед каждым тестом")
    public void before() {
        genEmail = User.generationEmail();
        genPass = User.generationPass();
        genName = User.generationName();

        user = new User(genEmail, genPass, genName);
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
        String changeUserName = clientLogin.genJson("name", genName);
        clientLogin.changeUserData(changeUserName)
                .assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.name", equalTo(genName + "q"));
    }

    @Test
    @DisplayName("PATCH /api/auth/user изменение email")
    @Description("Изменение email авторизованного пользователя")
    public void changeEmailLoginUserTest() {
        String changeUserEmail = clientLogin.genJson("email", genEmail);
        clientLogin.changeUserData(changeUserEmail)
                .assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(genEmail + "q"));
    }

    @Test
    @DisplayName("PATCH /api/auth/user изменение name")
    @Description("Изменение name неавторизованного пользователя")
    public void changeNameUnauthUserTest() {
        String changeUserName = client.genJson("name", genName);
        client.changeUserData(changeUserName)
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("PATCH /api/auth/user изменение email")
    @Description("Изменение email неавторизованного пользователя")
    public void changeEmailUnauthUserTest() {
        String changeUserEmail = client.genJson("email", genEmail);
        client.changeUserData(changeUserEmail)
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
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
