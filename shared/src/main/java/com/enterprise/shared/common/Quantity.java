package com.enterprise.shared.common;

import java.util.Objects;

/**
 * Value Object representing a quantity of items.
 * Ensures validation and immutability for inventory quantities.
 */
public class Quantity {
    private final Integer value;

    public Quantity(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.value = value;
    }

    public static Quantity of(Integer value) {
        return new Quantity(value);
    }

    public static Quantity zero() {
        return new Quantity(0);
    }

    public Integer getValue() {
        return value;
    }

    public boolean isZero() {
        return value == 0;
    }

    public boolean isPositive() {
        return value > 0;
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    public Quantity subtract(Quantity other) {
        int result = this.value - other.value;
        if (result < 0) {
            throw new IllegalArgumentException("Cannot subtract more than available quantity");
        }
        return new Quantity(result);
    }

    public boolean isGreaterThan(Quantity other) {
        return this.value > other.value;
    }

    public boolean isGreaterThanOrEqual(Quantity other) {
        return this.value >= other.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Quantity quantity = (Quantity) obj;
        return Objects.equals(value, quantity.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
