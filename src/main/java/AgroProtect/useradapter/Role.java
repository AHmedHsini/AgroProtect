package AgroProtect.useradapter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * MINIMAL ROLE ENTITY - Extracted from identity module
 *
 * Table: roles
 *
 * When integrating: DELETE and use tn.esprit.agroprotect.identity.entity.Role
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "is_system_role")
    @Builder.Default
    private Boolean isSystemRole = false;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Needed for User relationship
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    // Helper for microcredit role checking
    public boolean isFarmer() {
        return "FARMER".equals(name) || "AGRICULTEUR".equals(name);
    }
}