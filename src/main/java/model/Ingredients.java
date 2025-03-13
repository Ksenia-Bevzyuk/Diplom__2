package model;

import io.qameta.allure.internal.shadowed.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Value;
import java.util.List;

@JsonIgnoreProperties
@Value
@AllArgsConstructor
public class Ingredients {
    private List<DataOfOrder> data;
}
