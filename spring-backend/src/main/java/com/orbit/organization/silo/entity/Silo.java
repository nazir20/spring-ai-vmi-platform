package com.orbit.organization.silo.entity;

import com.orbit.enums.SiloStatus;
import com.orbit.enums.SiloStockSource;
import com.orbit.inventory.product.entity.Product;
import com.orbit.organization.outstation.entity.Outstation;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "silos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_silo_code",
                        columnNames = "silo_code"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Silo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "silo_code", nullable = false, unique = true, length = 30)
    private String siloCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal capacity;

    @Column(name = "minimum_stock", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumStock;

    @Column(name = "maximum_stock", nullable = false, precision = 12, scale = 2)
    private BigDecimal maximumStock;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "stock_observed_at")
    private Instant stockObservedAt;

    @Column(name = "last_measured_at")
    private Instant lastMeasuredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "stock_source", length = 20)
    private SiloStockSource stockSource;

    @Column(name = "material_code", length = 50)
    private String materialCode;

    @Column(name = "material_name", length = 150)
    private String materialName;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SiloStatus status = SiloStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "outstation_id", nullable = false)
    private Outstation outstation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void recordStock(BigDecimal quantity, Instant observedAt, SiloStockSource source) {
        currentStock = quantity;
        stockObservedAt = observedAt;
        stockSource = source;
    }
}
