package dev.itsdaksh.controlplane.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "event_allowed_domains",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "event_id",
                                "domain"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAllowedDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    private String domain;
}