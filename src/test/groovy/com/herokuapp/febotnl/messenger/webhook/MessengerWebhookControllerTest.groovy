package com.herokuapp.febotnl.messenger.webhook

import com.herokuapp.febotnl.messenger.Constants
import com.herokuapp.febotnl.messenger.model.SendApiResponse
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static com.herokuapp.febotnl.messenger.Constants.GRAPH_API_URL

/**
 * Test for MessengerWebhookController
 */
class MessengerWebhookControllerTest extends Specification {
    Environment env
    MessengerWebhookController controller
    RestTemplate template

    def setup() {
        env = Mock(Environment)
        env.getRequiredProperty('facebook-webhook-token') >> 't0k3n'
        template = Mock(RestTemplate)
        controller = new MessengerWebhookController(env, template)
    }

    def 'VerifyChallenge'() {
        when: 'When the token matches'
        ResponseEntity<String> result = controller.verifyChallenge('subscribe', 'ch@113ng3', 't0k3n')

        then:
        result
        result.statusCode.'2xxSuccessful'
        result.body == 'ch@113ng3'

        when: 'When there is token mismatch'
        result = controller.verifyChallenge('subscribe', 'ch@113ng3', 'wr0ngT0k3n')

        then:
        result
        result.statusCode.'4xxClientError'
        !result.body
    }

    def 'Webhook'() {
        given:
        def body = [
                object: 'page',
                entry: [
                        [
                                messaging: [
                                        [
                                                sender: [
                                                        id: 'senderid213'
                                                ],
                                                message: [
                                                        sticker_id: Constants.STICKER_THUMBS_UP
                                                ]
                                        ]
                                ]
                        ]
                ]
        ]

        when:
        ResponseEntity<String> result = controller.webhook(body)

        then:
        result
        result.statusCode.'2xxSuccessful'
        1 * template.postForObject(GRAPH_API_URL, {it.message.text == 'you are welcome 😊'}, SendApiResponse, _)
    }
}
