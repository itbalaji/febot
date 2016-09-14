package com.github.itbalaji.messenger.webhook

import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Webhook for receiving messages from Messenger
 */
@Slf4j
@RestController
@RequestMapping('/messenger/webhook')
class MessengerWebhookController {

    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<String> verifyChallenge(@RequestParam Map<String, String> params) {
        return new ResponseEntity<String>('', HttpStatus.ACCEPTED)
    }
}
