package com.noragami.restreview.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "settlements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;
    @ManyToOne
    @JoinColumn(name = "from_user", nullable = false)
    private UserEntity fromUser;
    @ManyToOne
    @JoinColumn(name = "to_user", nullable = false)
    private UserEntity toUser;
    @Column(nullable = false)
    private BigDecimal amount;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp settledAt;
}
