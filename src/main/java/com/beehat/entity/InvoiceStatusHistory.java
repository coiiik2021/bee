package com.beehat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="invoice_status_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "previous_status")
    private Byte previousStatus;

    @Column(name = "new_status")
    private Byte newStatus;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private Employee updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "note")
    private String note;

}
