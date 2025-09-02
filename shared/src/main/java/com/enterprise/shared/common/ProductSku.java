package com.enterprise.shared.common;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a product SKU.
 * Implements immutability and validation for product identification.
 */
public class ProductSku {
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z0-9]{8,12}$");
    private final String value;

    public ProductSku(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }

        String normalizedValue = value.trim().toUpperCase();
        if (!SKU_PATTERN.matcher(normalizedValue).matches()) {
            throw new IllegalArgumentException("Invalid SKU format. Must be 8-12 alphanumeric characters");
        }

        this.value = normalizedValue;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProductSku that = (ProductSku) obj;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
