package com.volapp.reservation.evenements;

import com.volapp.reservation.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publie les evenements de reservation sur l'exchange "volapp.evenements".
 * Une publication qui echoue est journalisee sans faire echouer l'operation metier.
 */
@Component
public class EvenementPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EvenementPublisher.class);

    public static final String CLE_CONFIRMEE = "reservation.confirmee";
    public static final String CLE_ANNULEE = "reservation.annulee";

    private final RabbitTemplate rabbitTemplate;

    public EvenementPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publierConfirmation(ReservationConfirmee evenement) {
        publier(CLE_CONFIRMEE, evenement);
    }

    public void publierAnnulation(ReservationAnnulee evenement) {
        publier(CLE_ANNULEE, evenement);
    }

    private void publier(String cle, Object evenement) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, cle, evenement);
        } catch (RuntimeException e) {
            logger.error("Impossible de publier l'evenement {} : {}", cle, e.getMessage(), e);
        }
    }
}
