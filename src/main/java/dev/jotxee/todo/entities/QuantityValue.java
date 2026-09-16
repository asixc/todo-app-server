package dev.jotxee.todo.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class QuantityValue {

    @Column(name = "quantity_value", precision = 19, scale = 4)
    private BigDecimal value;

    @Column(name = "quantity_unit", length = 16)
    private String unit;

    protected QuantityValue() {
    }

    private QuantityValue(BigDecimal value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    public static QuantityValue of(BigDecimal value, String unit) {
        return new QuantityValue(value, unit);
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
