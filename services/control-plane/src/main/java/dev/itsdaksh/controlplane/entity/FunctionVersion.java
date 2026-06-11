package dev.itsdaksh.controlplane.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "function_versions",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "function_id",
                                "version_number"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private Function function;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private String fileHash;

    @Column(nullable = false)
    private Long fileSizeBytes;

    @CreationTimestamp
    private LocalDateTime createdAt;
}