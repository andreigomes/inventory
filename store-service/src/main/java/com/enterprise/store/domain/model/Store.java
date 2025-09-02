package com.enterprise.store.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Store Aggregate Root - Represents a physical store location.
 * Contains store-specific information and inventory management capabilities.
 */
public class Store {
    private final UUID id;
    private final String storeCode;
    private final String name;
    private final Address address;
    private final StoreStatus status;
    private final Instant createdAt;
    private Instant lastUpdated;
    private Long version;

    public Store(UUID id, String storeCode, String name, Address address,
                StoreStatus status, Instant createdAt, Instant lastUpdated, Long version) {
        this.id = id;
        this.storeCode = storeCode;
        this.name = name;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.version = version;
    }

    public Store(String storeCode, String name, Address address) {
        this.id = UUID.randomUUID();
        this.storeCode = storeCode;
        this.name = name;
        this.address = address;
        this.status = StoreStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.lastUpdated = Instant.now();
        this.version = 1L;
    }

    public boolean isActive() {
        return status == StoreStatus.ACTIVE;
    }

    public void activate() {
        this.status = StoreStatus.ACTIVE;
        this.lastUpdated = Instant.now();
    }

    public void deactivate() {
        this.status = StoreStatus.INACTIVE;
        this.lastUpdated = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public String getStoreCode() { return storeCode; }
    public String getName() { return name; }
    public Address getAddress() { return address; }
    public StoreStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastUpdated() { return lastUpdated; }
    public Long getVersion() { return version; }

    public enum StoreStatus {
        ACTIVE, INACTIVE, MAINTENANCE
    }
}

/**
 * Value Object representing store address.
 */
class Address {
    private final String street;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String country;

    public Address(String street, String city, String state, String zipCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    // Getters
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
    public String getCountry() { return country; }
}
