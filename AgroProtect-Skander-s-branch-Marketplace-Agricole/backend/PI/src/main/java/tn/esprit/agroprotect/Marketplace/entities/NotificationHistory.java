package  tn.esprit.agroprotect.Marketplace.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "notification_history", indexes = {
    @Index(name = "idx_notif_history_sent_date", columnList = "sent_date"),
    @Index(name = "idx_notif_history_status", columnList = "status")
})
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = true)
    private Long to;

    @Column(name = "sender_id")
    private Long from;

    @Column(nullable = false, length = 200)
    @Size(max = 200)
    private String subject;

    @Column(nullable = false, length = 2000)
    @Size(max = 2000)
    private String content;

    @Column(name = "sent_date")
    private LocalDateTime sentDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    private StatusNotification status;

    @Column(name = "template_id")
    private Long templateId;

    @PrePersist
    protected void onCreate() {
        sentDate = LocalDateTime.now();
        if (status == null) {
            status = StatusNotification.ENVOYE;
        }
    }
}