package com.herokuapp.febotnl.messenger.webhook

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import static org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Webhook for receiving messages from Messenger
 */
@Slf4j
@RestController
@RequestMapping('/messenger/webhook')
class MessengerWebhookController {
    Environment environment

    @Autowired
    MessengerWebhookController(Environment environment) {
        this.environment = environment
    }

    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<String> verifyChallenge(@RequestParam('hub.mode') String mode,
                                           @RequestParam('hub.challenge') String challenge,
                                           @RequestParam('hub.verify_token') String verifyToken) {
        if ('subscribe' == mode && environment.getRequiredProperty('facebook-webhook-token') == verifyToken)
            ResponseEntity.ok(challenge)
        else
            new ResponseEntity<String>(BAD_REQUEST)
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity webhook(@RequestBody body,
                                   @RequestParam Map<String, String> params) {
        log.info('facebook-webhook-token {}', environment.containsProperty('facebook-webhook-token'))
        log.info('facebook-page-access-token {}', environment.containsProperty('facebook-page-access-token'))
        log.info('params {}', params)
        if (body.object == 'page') {
            body.entry.each {
                def pageId = it.id
                def timestamp = it.time
                log.info('Received event for page {} at {}', pageId, timestamp)

                it.messaging.each {event ->
                    if (event.optin) {
                        log.info('Received optin')
                        // TODO **Implement**
                    }
                    else if (event.message) {
                        log.info('Received message')
                        // TODO **Implement**
                    }
                    else if (event.delivery) {
                        log.info('Received delivery receipt')
                        // TODO **Implement**
                    }
                    else if (event.postback) {
                        log.info('Received post back')
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
}
