package com.orbit.organization.customer.entity;

import com.orbit.enums.CustomerStatus;
import com.orbit.organization.outstation.entity.Outstation;
import com.orbit.organization.supplier.entity.Supplier;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_customer_code",
                        columnNames = "customer_code"
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_code", nullable = false)
    private String customerCode;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    private String displayName;

    private String email;

    private String phone;

    private String vatNumber;

    private String street;

    private String city;

    private String postalCode;

    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "supplier_customers",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "supplier_id"),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_customer_supplier",
                            columnNames = {"customer_id", "supplier_id"}
                    )
            }
    )
    private List<Supplier> suppliers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Outstation> outstations = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
