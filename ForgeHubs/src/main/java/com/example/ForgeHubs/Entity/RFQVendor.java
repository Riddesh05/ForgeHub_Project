package com.example.ForgeHubs.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "RFQVendors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RFQVendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    // RFQ assigned to vendor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "RFQId",
            nullable = false
    )
    private RFQ rfq;

    // Vendor is a User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "VendorId",
            nullable = false
    )
    private User vendor;
}