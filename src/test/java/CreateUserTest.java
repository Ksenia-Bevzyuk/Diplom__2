import client.StellarBurgersClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class CreateUserTest {
    private User user;
    private StellarBurgersClient client;
    private ValidatableResponse response;
    private String genEmail;
    private String genPass;
    private String genName;

    @Before
    @DisplayName("Перед каждым тестом")
    @Description("Генерация данных для создания пользователя")
    public void before() {
        genEmail = User.generationEmail();
        genPass = User.generationPass();
        genName = User.generationName();
    }

    @Test
    @DisplayName("POST /api/auth/register корректно заполнены все поля")
    @Description("Успешное создание пользователя, заполнены все поля")
    public void createUserAllFieldCorrectTest() {
        user = new User(genEmail, genPass, genName);
        client = new StellarBurgersClient("");
        response = client.createUser(user);

        response.assertThat().statusCode(SC_OK).body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("POST /api/auth/register попытка создать двух одинаковых пользователей")
    @Description("Код 403 при создании двух одинаковых пользователей")
    public void createTwoIdenticalStatusCode403Test() {
        user = new User(genEmail, genPass, genName);
        client = new StellarBurgersClient("");
        client.createUser(user);

        response = client.createUser(user);
        response.assertThat()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("POST /api/auth/register попытка создать пользователя с email = null")
    @Description("Код 403 при заполненных полях password и name, email - null")
    public void createUserWithFieldPassAndNameOnlyTest() {
        user = new User(null, genPass, genName);
        client = new StellarBurgersClient("");

        response = client.createUser(user);
        response.assertThat()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message",
                        equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("POST /api/auth/register попытка создать пользователя с password = null")
    @Description("Код 403 при заполненных полях email и name, password - null")
    public void createUserWithFieldEmailAndNameOnlyTest() {
        user = new User(genEmail, null, genName);
        client = new StellarBurgersClient("");

        response = client.createUser(user);
        response.assertThat()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message",
                        equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("POST /api/auth/register попытка создать пользователя с name = null")
    @Description("Код 403 при заполненных полях email и password, name - null")
    public void createUserWithFieldEmailAndPassOnlyTest() {
        user = new User(genEmail, null, genName);
        client = new StellarBurgersClient("");

        response = client.createUser(user);
        response.assertThat()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message",
                        equalTo("Email, password and name are required fields"));
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