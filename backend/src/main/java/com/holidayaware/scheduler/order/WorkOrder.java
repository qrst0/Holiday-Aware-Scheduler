package com.holidayaware.scheduler.order;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "work_order")
class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "required_days", nullable = false)
    private int requiredDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_flag", length = 10)
    private RiskFlag riskFlag;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    protected WorkOrder() {
    }

    WorkOrder(String productCode, int quantity, String countryCode, LocalDate startDate,
              LocalDate dueDate, int requiredDays, OrderStatus status, LocalDateTime createdAt) {
        this.productCode = productCode;
        this.quantity = quantity;
        this.countryCode = countryCode;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.requiredDays = requiredDays;
        this.status = status;
        this.createdAt = createdAt;
        this.lastUpdated = Instant.now();
    }

    Long getId() {
        return id;
    }

    String getProductCode() {
        return productCode;
    }

    int getQuantity() {
        return quantity;
    }

    String getCountryCode() {
        return countryCode;
    }

    LocalDate getStartDate() {
        return startDate;
    }

    LocalDate getDueDate() {
        return dueDate;
    }

    int getRequiredDays() {
        return requiredDays;
    }

    OrderStatus getStatus() {
        return status;
    }

    RiskFlag getRiskFlag() {
        return riskFlag;
    }

    void update(String productCode, int quantity, String countryCode, LocalDate startDate,
                LocalDate dueDate, int requiredDays, OrderStatus status) {
        this.productCode = productCode;
        this.quantity = quantity;
        this.countryCode = countryCode;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.requiredDays = requiredDays;
        this.status = status;
        this.lastUpdated = Instant.now();
    }

    void backdate(LocalDateTime createdAt, Instant lastUpdated) {
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    void setRiskFlag(RiskFlag riskFlag) {
        this.riskFlag = riskFlag;
    }

    Instant getLastUpdated() {
        return lastUpdated;
    }

    LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
