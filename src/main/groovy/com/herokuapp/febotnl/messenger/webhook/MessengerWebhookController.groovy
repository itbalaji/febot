package com.herokuapp.febotnl.messenger.webhook

import com.herokuapp.febotnl.messenger.model.SendApiResponse
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.*
import static com.herokuapp.febotnl.messenger.Constants.*


/**
 * Webhook for receiving messages from Messenger
 */
@Slf4j
@RestController
@RequestMapping('/messenger/webhook')
class MessengerWebhookController {
    final String verifyToken
    final RestTemplate restTemplate
    final String pageAccessToken

    @Autowired
    MessengerWebhookController(Environment environment, @Qualifier('messengerRestTemplate') RestTemplate restTemplate) {
        verifyToken = environment.getRequiredProperty('facebook-webhook-token')
        pageAccessToken = environment.getRequiredProperty('facebook-page-access-token')
        this.restTemplate = restTemplate
    }

    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<String> verifyChallenge(@RequestParam('hub.mode') String mode,
                                           @RequestParam('hub.challenge') String challenge,
                                           @RequestParam('hub.verify_token') String verifyToken) {
        if ('subscribe' == mode && this.verifyToken == verifyToken)
            ResponseEntity.ok(challenge)
        else
            new ResponseEntity<String>(BAD_REQUEST)
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity webhook(@RequestBody body) {
        if (body.object == 'page') {
            body.entry.each {
                it.messaging.each {event ->
                    if (event.optin) {
                        log.info('Received optin {}', event.optin)
                        // TODO **Implement**
                    }
                    else if (event.message) {
                        log.info('Received message {}', event.message)
                        processMessage(event.sender.id, event.message)
                    }
                    else if (event.delivery) {
                        log.info('Received delivery receipt {}', event.delivery)
                        // TODO **Implement**
                    }
                    else if (event.postback) {
                        log.info('Received post back {}', event.postback)
                        // TODO **Implement**
                    }
                    else {
                        log.warn('Unknown messaging event {} received at webhook', event)
                    }
                }
            }
        }
        new ResponseEntity(OK)
    }

    private void processMessage(sender, message) {
        if (message.sticker_id) {
            processSticker(sender, message.sticker_id)
        }
        else if (message.text) {
            // TODO handle text messages
        }
        else if (message.attachments) {
            message.attachments.each {
                if (it.type == 'image') {
                    // TODO handle image
                }
                else if (it.type == 'location') {
                    processLocation(sender, it.payload)
                }
            }
        }
    }

    private void processLocation(sender, location) {
        log.info('Received location {} from {}', location, sender)
        // TODO process location
    }

    private void processSticker(sender, id) {
        if (id == STICKER_THUMBS_UP) {
            sendTextMessage(sender, 'you are welcome 😊')
        }
    }

    private void sendTextMessage(sender, String message) {
        def data = [
                recipient: [
                        id: sender
                ],
                message: [
                        text: message
                ]
        ]
        sendDataToMessenger(data)
    }

    private SendApiResponse sendDataToMessenger(data) {
        restTemplate.postForObject(GRAPH_API_URL, data, SendApiResponse, [access_token: pageAccessToken])
    }
}
