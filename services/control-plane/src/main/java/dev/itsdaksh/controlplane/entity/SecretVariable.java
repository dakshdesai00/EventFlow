package dev.itsdaksh.controlplane.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "secret_variables", uniqueConstraints = {
    @UniqueConstraint(name = "uk_secret_var_function_key", columnNames = {"function_id", "key"}),
    @UniqueConstraint(name = "uk_secret_var_project_key", columnNames = {"project_id", "key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecretVariable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id")
    private Function function;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String value;
}
