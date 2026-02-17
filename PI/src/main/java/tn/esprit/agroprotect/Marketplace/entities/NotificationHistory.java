package com.example.agroprotect.entities;

import jakarta.persistence.*;
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
@Table(name = "notification_history")
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long to;

    @Column(name = "sender_id")
    private Long from;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "sent_date")
    private LocalDateTime sentDate;

    @Enumerated(EnumType.STRING)
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