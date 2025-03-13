import client.StellarBurgersClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.*;
import model.Number;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class CreateOrderTest {
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
    @DisplayName("POST /api/orders создание заказа с авторизованным пользователем")
    @Description("Создание заказа с авторизованным пользователем и верным хешем ингредиентов")
    public void createOrderLoginUserAndCorrectIngredients() {
        ValidatableResponse response = clientLogin.getHashIngredients();
        String[] ingredients = clientLogin.choiceIngredient(response);

        Order order = new Order(ingredients);
        ValidatableResponse responseCreateOrder = clientLogin.createOrder(order);
        int statusCode = responseCreateOrder.extract().statusCode();
        boolean success = responseCreateOrder.extract().jsonPath().getBoolean("success");
        String name = responseCreateOrder.extract().jsonPath().getString("name");
        int number = responseCreateOrder.extract().as(Number.class).getNumber();
        assertEquals("Ожидается код ответа 200", SC_OK, statusCode);
        assertEquals("Ожидается true", true, success);
        assertNotNull(name);
        assertNotNull(number);
    }

    @Test
    @DisplayName("POST /api/orders создание заказа с авторизованным пользователем")
    @Description("Создание заказа с авторизованным пользователем без ингредиентов")
    public void createOrderLoginUserAndWithoutIngredients() {

        Order order = new Order();
        ValidatableResponse responseCreateOrder = clientLogin.createOrder(order);
        int statusCode = responseCreateOrder.extract().statusCode();
        boolean success = responseCreateOrder.extract().jsonPath().getBoolean("success");
        String message = responseCreateOrder.extract().jsonPath().getString("message");
        assertEquals("Ожидается код ответа 400", SC_BAD_REQUEST, statusCode);
        assertEquals("Ожидается false", false, success);
        assertEquals("Ожидается другое сообщение",
                "Ingredient ids must be provided", message);
    }

    @Test
    @DisplayName("POST /api/orders создание заказа с авторизованным пользователем")
    @Description("Создание заказа с авторизованным пользователем с некорректным хешем ингредиентов")
    public void createOrderLoginUserAndIncorrectIngredients() {
        String[] ingredients = {"simba"};
        Order order = new Order(ingredients);
        clientLogin.createOrder(order).assertThat().statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("POST /api/orders создание заказа с неавторизованным пользователем")
    @Description("Создание заказа с неавторизованным пользователем и верным хешем ингредиентов")
    public void createOrderUnauthUserAndCorrectIngredients() {
        ValidatableResponse response = client.getHashIngredients();
        String[] ingredients = client.choiceIngredient(response);

        Order order = new Order(ingredients);
        ValidatableResponse responseCreateOrder = client.createOrder(order);
        int statusCode = responseCreateOrder.extract().statusCode();
        boolean success = responseCreateOrder.extract().jsonPath().getBoolean("success");
        String name = responseCreateOrder.extract().jsonPath().getString("name");
        int number = responseCreateOrder.extract().as(Number.class).getNumber();
        assertEquals("Ожидается код ответа 200", SC_OK, statusCode);
        assertEquals("Ожидается true", true, success);
        assertNotNull(name);
        assertNotNull(number);
    }

    @Test
    @DisplayName("POST /api/orders создание заказа с неавторизованным пользователем")
    @Description("Создание заказа с неавторизованным пользователем без ингредиентов")
    public void createOrderUnauthUserAndWithoutIngredients() {

        Order order = new Order();
        ValidatableResponse responseCreateOrder = client.createOrder(order);
        int statusCode = responseCreateOrder.extract().statusCode();
        boolean success = responseCreateOrder.extract().jsonPath().getBoolean("success");
        String message = responseCreateOrder.extract().jsonPath().getString("message");
        assertEquals("Ожидается код ответа 400", SC_BAD_REQUEST, statusCode);
        assertEquals("Ожидается false", false, success);
        assertEquals("Ожидается другое сообщение",
                "Ingredient ids must be provided", message);
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
