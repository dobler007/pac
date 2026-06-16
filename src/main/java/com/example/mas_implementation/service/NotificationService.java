package com.example.mas_implementation.service;

import com.example.mas_implementation.model.Game;
import com.example.mas_implementation.model.Notification;
import com.example.mas_implementation.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Central dispatch point for outbound notifications. It abstracts the delivery channel
 * (email or SMS) away from the callers in the service layer.
 *
 * <p>In the current build delivery is simulated: every message is written to the application
 * log. Replacing the body of {@link #send} with a real provider (e.g. SendGrid for email,
 * Twilio for SMS) would enable live delivery without changing any caller.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Delivers a persisted notification. The channel is determined by which optional side of the
     * notification is populated: an {@code Email} record means e-mail, a {@code Text} record means
     * SMS. Exactly one is expected to be set (the either-or rule from the analysis model).
     */
    public void send(Notification notification) {
        if (notification == null) {
            return;
        }
        String channel = notification.getEmailNot() != null ? "EMAIL"
                       : notification.getText() != null ? "SMS"
                       : "NONE";
        log.info("[NOTIFICATION/{}] {}", channel, notification.getMessage());
        notification.send();
    }

    /** Notifies the player promoted from a game's waitlist that a spot has opened up. */
    public void notifyWaitlistPromotion(Player player, Game game) {
        if (player == null || game == null) {
            return;
        }
        log.info("[NOTIFICATION] Player {} has been moved from the waitlist into game {}.",
                player.getLogin(), game.getId());
    }
}
