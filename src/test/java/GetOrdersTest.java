import client.StellarBurgersClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.Credentials;
import model.Number;
import model.Order;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
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
    @DisplayName("POST /api/orders/all получение заказов")
    @Description("Получение заказов авторизованного пользователя")
    public void getOrdersLoginUser() {
        Order order = new Order();
        clientLogin.createOrder(order);
        clientLogin.getOrders()
                .assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("total", notNullValue())
                .body("totalToday", notNullValue())
                .body("orders.number", notNullValue());
    }

    @Test
    @DisplayName("POST /api/orders/all получение заказов")
    @Description("Получение заказов неавторизованного пользователя")
    public void getOrdersUnauthUser() {
        Order order = new Order();
        client.createOrder(order);
        client.getOrders()
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
