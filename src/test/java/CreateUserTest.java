import client.StellarBurgersClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.User;
import org.junit.After;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;

public class CreateUserTest {
    private User user;
    private StellarBurgersClient client;
    private ValidatableResponse response;

    @Test
    @DisplayName("POST /api/auth/register корректно заполнены все поля")
    @Description("Успешное создание пользователя, заполнены все поля")
    public void createUserAllFieldCorrectTest() {
        String genEmail = User.generationEmail();

        user = new User(genEmail, "password", "Павел");
        client = new StellarBurgersClient("");
        response = client.createUser(user);

        response.assertThat().statusCode(SC_OK).body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("POST /api/auth/register попытка создать двух одинаковых пользователей")
    @Description("Код 403 при создании двух одинаковых пользователей")
    public void createTwoIdenticalStatusCode403Test() {
        String genEmail = User.generationEmail();
        user = new User(genEmail, "password", "Павел");
        client = new StellarBurgersClient("");
        response = client.createUser(user);

        ValidatableResponse response1 = client.createUser(user);

        int statusCode = response1.extract().statusCode();
        boolean success = response1.extract().jsonPath().getBoolean("success");
        String message = response1.extract().jsonPath().getString("message");
        assertEquals("Ожидается код ответа 403", SC_FORBIDDEN, statusCode);
        assertEquals("Ожидается false", false, success);
        assertEquals("Ожидается другое сообщение",
                "User already exists", message);
    }

    @Test
    @DisplayName("POST /api/auth/register попытка создать пользователя с email = null")
    @Description("Код 403 при заполненных полях password и name, email - null")
    public void createUserWithFieldPassAndNameOnlyTest() {
        user = new User(null, "password", "Павел");
        client = new StellarBurgersClient("");

        response = client.createUser(user);
        int statusCode = response.extract().statusCode();
        boolean success = response.extract().jsonPath().getBoolean("success");
        String message = response.extract().jsonPath().getString("message");
        assertEquals("Ожидается код ответа 403", SC_FORBIDDEN, statusCode);
        assertEquals("Ожидается false", false, success);
        assertEquals("Ожидается другое сообщение",
                "Email, password and name are required fields", message);
    }

    @Test
    @DisplayName("POST /api/auth/register попытка создать пользователя с password = null")
    @Description("Код 403 при заполненных полях email и name, password - null")
    public void createUserWithFieldEmailAndNameOnlyTest() {
        String genEmail = User.generationEmail();
        user = new User(genEmail, null, "Павел");
        client = new StellarBurgersClient("");

        response = client.createUser(user);
        int statusCode = response.extract().statusCode();
        boolean success = response.extract().jsonPath().getBoolean("success");
        String message = response.extract().jsonPath().getString("message");
        assertEquals("Ожидается код ответа 403", SC_FORBIDDEN, statusCode);
        assertEquals("Ожидается false", false, success);
        assertEquals("Ожидается другое сообщение",
                "Email, password and name are required fields", message);
    }

    @Test
    @DisplayName("POST /api/auth/register попытка создать пользователя с name = null")
    @Description("Код 403 при заполненных полях email и password, name - null")
    public void createUserWithFieldEmailAndPassOnlyTest() {
        String genEmail = User.generationEmail();
        user = new User(genEmail, null, "Павел");
        client = new StellarBurgersClient("");

        response = client.createUser(user);
        int statusCode = response.extract().statusCode();
        boolean success = response.extract().jsonPath().getBoolean("success");
        String message = response.extract().jsonPath().getString("message");
        assertEquals("Ожидается код ответа 403", SC_FORBIDDEN, statusCode);
        assertEquals("Ожидается false", false, success);
        assertEquals("Ожидается другое сообщение",
                "Email, password and name are required fields", message);
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