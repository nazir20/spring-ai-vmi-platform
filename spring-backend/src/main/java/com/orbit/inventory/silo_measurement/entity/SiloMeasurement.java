package com.orbit.inventory.silo_measurement.entity;

import com.orbit.enums.MeasurementSource;
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
        name = "silo_measurements",
        indexes = {
                @Index(name = "idx_measurement_silo", columnList = "silo_id"),
                @Index(name = "idx_measurement_time", columnList = "measured_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SiloMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "silo_id", nullable = false)
    private Silo silo;

    @Column(name = "fill_level", nullable = false, precision = 12, scale = 2)
    private BigDecimal fillLevel;

    @Column(name = "fill_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal fillPercentage;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MeasurementSource source = MeasurementSource.MANUAL;

    @Column(name = "sensor_id", length = 100)
    private String sensorId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
