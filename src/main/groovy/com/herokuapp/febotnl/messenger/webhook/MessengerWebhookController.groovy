package com.herokuapp.febotnl.messenger.webhook

import com.herokuapp.febotnl.google.places.model.Response
import com.herokuapp.febotnl.google.places.model.ResponseStatus as GooglePlacesResponseStatus
import com.herokuapp.febotnl.google.places.model.Result
import com.herokuapp.febotnl.messenger.model.SendApiResponse
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.commons.codec.digest.HmacUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

import javax.servlet.http.HttpServletRequest
import java.nio.charset.StandardCharsets

import static com.herokuapp.febotnl.messenger.Constants.*
import static org.springframework.http.HttpStatus.*

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
    final String googleKey
    final String facebookAppSecret

    @Autowired
    MessengerWebhookController(Environment environment, @Qualifier('messengerRestTemplate') RestTemplate restTemplate) {
        verifyToken = environment.getRequiredProperty('facebook-webhook-token')
        pageAccessToken = environment.getRequiredProperty('facebook-page-access-token')
        googleKey = environment.getRequiredProperty('google-key')
        facebookAppSecret = environment.getRequiredProperty('facebook-app-secret')
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
    ResponseEntity webhook(HttpServletRequest request, @RequestHeader('X-Hub-Signature') String xHubSignature, @RequestBody body) {
        log.info('Received {} with {} hash', JsonOutput.toJson(body), xHubSignature)
        if(isPayloadFromFacebook(request.inputStream.bytes, xHubSignature)) {
            if (body.object == 'page') {
                body.entry.each {
                    it.messaging.each { event ->
                        if (event.optin) {
                            // TODO **Implement**
                        } else if (event.message) {
                            processMessage(event.sender.id, event.message)
                        } else if (event.delivery) {
                            // TODO **Implement**
                        } else if (event.postback) {
                            // TODO **Implement**
                        } else {
                            log.warn('Unknown messaging event {} received at webhook', JsonOutput.toJson(body))
                        }
                    }
                }
            }
            new ResponseEntity(OK)
        }
        else {
            new ResponseEntity(FORBIDDEN)
        }
    }

    private boolean isPayloadFromFacebook(byte[] payloadBytes, String xHubSignature) {
        if (xHubSignature?.startsWith('sha1=')) {
            String hashReceived = xHubSignature.substring(5)
            String hashComputed = HmacUtils.hmacSha1Hex(facebookAppSecret.getBytes(StandardCharsets.UTF_8), payloadBytes)
            return hashReceived == hashComputed
        }
        return false
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
        Response febos = getFeboAt(location.coordinates)
        if (!febos.htmlAttributions && febos.status == GooglePlacesResponseStatus.OK) {
            Result nearest = febos.results.first()
            sendTextMessage(sender, "The nearest FEBO is at $nearest.vicinity")
            if (nearest.openingHours?.openNow) {
                sendTextMessage(sender, 'and it is open now!!')
            }
            else {
                sendTextMessage(sender, 'but it is probably closed now 😞')
                Result open = febos.results.find {it.openingHours?.openNow}
                if (open) {
                    sendTextMessage(sender, "The nearest FEBO that is open now is at $open.vicinity")
                }
                else {
                    sendTextMessage(sender, 'Unfortunately there is nothing near that is open now')
                }
            }
        }
    }

    private Response getFeboAt(coordinates) {
        try {
            restTemplate.getForObject(GOOGLE_PLACES_URL, Response, [key: googleKey, lat: coordinates.lat, lon: coordinates.long])
        }
        catch (HttpClientErrorException _4xx) {
            log.error('Could not get FEBO at {} due to {}', coordinates, _4xx.responseBodyAsString)
            null
        }
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
        try {
            restTemplate.postForObject(GRAPH_API_URL, data, SendApiResponse, [access_token: pageAccessToken])
        }
        catch (HttpClientErrorException _4xx) {
            log.error('Could not send {} due to {}', data, _4xx.responseBodyAsString)
            null
        }
    }
}
