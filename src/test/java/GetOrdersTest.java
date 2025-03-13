import client.StellarBurgersClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.Credentials;
import model.Number;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

public class GetOrdersTest {
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
    @DisplayName("POST /api/orders/all получение заказов")
    @Description("Получение заказов авторизованного пользователя")
    public void getOrdersLoginUser() {
        ValidatableResponse responseGetOrders = clientLogin.getOrders();
        int statusCode = responseGetOrders.extract().statusCode();
        boolean success = responseGetOrders.extract().jsonPath().getBoolean("success");
        int total = responseGetOrders.extract().jsonPath().getInt("total");
        int totalToday = responseGetOrders.extract().jsonPath().getInt("totalToday");
        int number = responseGetOrders.extract().as(Number.class).getNumber();
        assertEquals("Ожидается код ответа 200", SC_OK, statusCode);
        assertEquals("Ожидается true", true, success);
        assertNotNull(total);
        assertNotNull(totalToday);
        assertNotNull(number);
    }

    @Test
    @DisplayName("POST /api/orders/all получение заказов")
    @Description("Получение заказов неавторизованного пользователя")
    public void getOrdersUnauthUser() {
        ValidatableResponse responseGetOrders = client.getOrders();
        int statusCode = responseGetOrders.extract().statusCode();
        boolean success = responseGetOrders.extract().jsonPath().getBoolean("success");
        int total = responseGetOrders.extract().jsonPath().getInt("total");
        int totalToday = responseGetOrders.extract().jsonPath().getInt("totalToday");
        int number = responseGetOrders.extract().as(Number.class).getNumber();
        assertEquals("Ожидается код ответа 200", SC_OK, statusCode);
        assertEquals("Ожидается true", true, success);
        assertNotNull(total);
        assertNotNull(totalToday);
        assertNotNull(number);
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
