package com.orbit.inventory.silo_consumption.entity;

import com.orbit.enums.ConsumptionSource;
import com.orbit.organization.silo.entity.Silo;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "silo_consumptions",
        indexes = {
                @Index(name = "idx_consumption_silo", columnList = "silo_id"),
                @Index(name = "idx_consumption_period", columnList = "period_start, period_end")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SiloConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "silo_id", nullable = false)
    private Silo silo;

    @Column(name = "quantity_consumed", nullable = false, precision = 12, scale = 2)
    private BigDecimal quantityConsumed;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ConsumptionSource source = ConsumptionSource.MANUAL;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
