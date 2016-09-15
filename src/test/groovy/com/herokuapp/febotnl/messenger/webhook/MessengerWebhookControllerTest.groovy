package com.herokuapp.febotnl.messenger.webhook

import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import spock.lang.Specification

/**
 * Test for MessengerWebhookController
 */
class MessengerWebhookControllerTest extends Specification {
    def 'VerifyChallenge'() {
        given:
        Environment env = Mock(Environment)
        env.getRequiredProperty('facebook-webhook-token') >> 't0k3n'
        MessengerWebhookController controller = new MessengerWebhookController(env)

        when:
        ResponseEntity<String> result = controller.verifyChallenge('subscribe', 'ch@113ng3', 't0k3n')

        then:
        result
        result.statusCode.'2xxSuccessful'
        result.body == 'ch@113ng3'
    }
}
