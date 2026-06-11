package dev.itsdaksh.controlplane.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "functions")
public class Function {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer timeoutMs;

    @Column(nullable = false)
    private Integer memoryLimitMb;

    @Column(nullable = false)
    private Boolean cacheEnabled;

    private Integer cacheTtlSeconds;
}
