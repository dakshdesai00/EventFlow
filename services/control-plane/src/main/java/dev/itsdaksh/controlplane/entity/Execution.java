package dev.itsdaksh.controlplane.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "executions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Execution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private Function function;
    @Column(nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    private String workerId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    private Instant startedAt;

    private Instant endedAt;

    private Long durationMs;

    @Column(length = 5000)
    private String errorMessage;

    @Column(columnDefinition = "TEXT")

    private String payload;
}