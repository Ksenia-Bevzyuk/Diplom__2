package client;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import model.*;
import static io.restassured.RestAssured.given;

public class StellarBurgersClient {
    RequestSpecification requestSpec;
    String accessToken;
    UpdateUser user;

    public StellarBurgersClient(String accessToken) {
        RequestSpecBuilder builder = new RequestSpecBuilder()

                .setBaseUri("https://stellarburgers.nomoreparties.site")
                .setContentType("application/json")
                .addHeader("Authorization", accessToken);

        requestSpec = builder.build();
    }

    @Step ("Создание пользователя")
    public ValidatableResponse createUser(User user) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .spec(requestSpec)
                .body(user)
                .post(EndPoints.CREATE_USER)
                .then()
                .log()
                .all();
    }

    @Step ("Получение токена")
    public String getAccessToken(ValidatableResponse response) {
        return accessToken = response.extract().jsonPath().getString("accessToken");
    }

    @Step ("Логин пользователя")
    public ValidatableResponse loginUser(Credentials credentials) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .spec(requestSpec)
                .body(credentials)
                .post(EndPoints.LOGIN_USER)
                .then()
                .log()
                .all();
    }

    @Step ("Удаление пользователя")
    public ValidatableResponse deleteUser() {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .spec(requestSpec)
                .delete(EndPoints.DELETE_USER)
                .then()
                .log()
                .all();
    }

    @Step ("Генерация json")
    public String genJson(String field, String value) {
        return "{\""+ field + "\": \"" + value + "q" + "\"}";
    }

    @Step ("Изменение данных пользователя")
    public ValidatableResponse changeUserData(String changeData) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .spec(requestSpec)
                .body(changeData)
                .patch(EndPoints.CHANGE_DATA_USER)
                .then()
                .log()
                .all();
    }

    @Step ("Получение хешей ингредиентов")
    public ValidatableResponse getHashIngredients() {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .spec(requestSpec)
                .get(EndPoints.GET_HASH_INGREDIENT)
                .then()
                .log()
                .all();
    }

    @Step ("Выбор ингредиентов")
    public String[] choiceIngredient(ValidatableResponse response) {
        Ingredients hashIngredient = response.extract().as(Ingredients.class);

        return new String[]{hashIngredient.getData().get(0).getId(),
                hashIngredient.getData().get(1).getId()};
    }


    @Step ("Создание заказа")
    public ValidatableResponse createOrder(Order order) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .spec(requestSpec)
                .body(order)
                .post(EndPoints.CREATE_ORDER)
                .then()
                .log()
                .all();
    }

    @Step ("Получение заказов пользователя")
    public ValidatableResponse getOrders() {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .spec(requestSpec)
                .get(EndPoints.GET_ORDERS)
                .then()
                .log()
                .all();
    }
}