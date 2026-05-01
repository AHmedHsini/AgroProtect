package  tn.esprit.agroprotect.Marketplace.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @Size(max = 50)
    @NotBlank
    private String code;

    @Column(nullable = false, length = 200)
    @Size(max = 200)
    @NotBlank
    private String subject;

    @Column(nullable = false, length = 2000)
    @Lob
    @Size(max = 2000)
    private String body;

}