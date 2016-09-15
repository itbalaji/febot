package com.herokuapp.febotnl.messenger.webhook

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
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
            new ResponseEntity<String>(challenge, HttpStatus.ACCEPTED)
        else
            new ResponseEntity<String>(HttpStatus.UNAUTHORIZED)
    }

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<String> webhook(@RequestParam('hub.mode') String mode,
                                   @RequestParam('hub.challenge') String challenge,
                                   @RequestBody body,
                                   @RequestParam Map<String, String> params) {
        // TODO **Implement**
        log.info(environment.properties.keySet().toListString())
        new ResponseEntity<String>(HttpStatus.OK)
    }
}
