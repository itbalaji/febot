package com.herokuapp.febotnl.messenger.webhook

import co.tenor.model.GifResult
import co.tenor.model.GifsResponse
import com.herokuapp.febotnl.data.ReceivedFromMessengerMongoCollection
import com.herokuapp.febotnl.febo.model.Febo
import com.herokuapp.febotnl.google.maps.model.TimeZone
import com.herokuapp.febotnl.messenger.model.SendApiResponse
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import org.apache.commons.codec.digest.HmacUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriTemplate

import javax.servlet.http.HttpServletRequest
import java.nio.charset.StandardCharsets
import java.time.LocalTime
import java.time.ZoneId

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
    final String febotPageId
    final MappingJackson2HttpMessageConverter jackson2HttpMessageConverter
    final ReceivedFromMessengerMongoCollection rfmmCollection

    @Autowired
    MessengerWebhookController(Environment environment, @Qualifier('messengerRestTemplate') RestTemplate restTemplate,
                               MappingJackson2HttpMessageConverter converter, ReceivedFromMessengerMongoCollection col) {
        verifyToken = environment.getRequiredProperty('facebook-webhook-token')
        pageAccessToken = environment.getRequiredProperty('facebook-page-access-token')
        googleKey = environment.getRequiredProperty('google-key')
        facebookAppSecret = environment.getRequiredProperty('facebook-app-secret')
        febotPageId = environment.getRequiredProperty('febot-page-id')
        this.restTemplate = restTemplate
        this.jackson2HttpMessageConverter = converter
        this.rfmmCollection = col
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
    ResponseEntity webhook(HttpServletRequest request, @RequestHeader('X-Hub-Signature') String xHubSignature) {
        try {
            byte[] bodyBytes = request.inputStream.text.getBytes(StandardCharsets.UTF_8)
            if (isPayloadFromFacebook(bodyBytes, xHubSignature)) {
                def body = jackson2HttpMessageConverter.objectMapper.readValue(bodyBytes, Map)
                log.info('Received {} body from facebook', JsonOutput.toJson(body))
                if (body.object == 'page') {
                    body.entry.each {
                        it.messaging.each { event ->
                            if (event.recipient.id == febotPageId) {
                                if (event.optin) {
                                    // TODO **Implement**
                                } else if (event.message && isNew(event.message)) {
                                    //TODO rfmmCollection.save(new ReceivedFromMessenger(data: ))
                                    processMessage(event.sender.id, event.message)
                                } else if (event.delivery) {
                                    // TODO **Implement**
                                } else if (event.postback) {
                                    String payload = event.postback.payload
                                    log.info('Payload received {}', payload)
                                    if (payload == 'what-can-febot-do') {
                                        sendLocationQuickReply(event.sender.id, 'I repeat...')
                                    }
                                    sendLocationQuickReply(event.sender.id, 'Febot can find you the nearest Febo')
                                } else {
                                    log.warn('Unknown messaging event {} received at webhook', JsonOutput.toJson(body))
                                }
                            }
                        }
                    }
                }
                new ResponseEntity(OK)
            } else {
                new ResponseEntity(FORBIDDEN)
            }
        }
        catch (e) {
            log.error('Unexpected', e)
            new ResponseEntity(OK)
        }
    }

    private boolean isNew(message) {
        //TODO rfmmCollection.findByMessageIdAndSeq(message.mid, message.seq)
        true
    }

    private boolean isPayloadFromFacebook(byte[] payloadBytes, String xHubSignature) {
        if (xHubSignature?.startsWith('sha1=')) {
            String hashReceived = xHubSignature.substring(5)
            String hashComputed = HmacUtils.hmacSha1Hex(facebookAppSecret.getBytes(StandardCharsets.UTF_8), payloadBytes)
            log.info('Received {} computed {}', hashReceived, hashComputed)
            return hashReceived == hashComputed
        }
        false
    }

    private void processMessage(sender, message) {
        if (message.sticker_id) {
            processSticker(sender, message.sticker_id)
        }
        else if (message.text) {
            sendLocationQuickReply(sender, "I wish I understood what you say 😞. Why don't you try sending your location?")
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
        findFoodTime(sender, location)
        findFebo(sender, location)
    }

    private void findFoodTime(sender, location) {
        TimeZone googleTimeZone = getLocalTimeAt(location.coordinates)
        if (googleTimeZone) {
            log.info('Received {} for {}', googleTimeZone, location)
            LocalTime localTime = LocalTime.now(ZoneId.of(googleTimeZone.timeZoneId))
            if (localTime.isAfter(LocalTime.of(6, 30)) && localTime.isBefore(LocalTime.of(9, 0))) {
                sendLocationQuickReply(sender, 'Breakfast time')
                sendGif(sender, 'breakfast')
            }
            else if (localTime.isAfter(LocalTime.of(11, 30)) && localTime.isBefore(LocalTime.of(14, 0))) {
                sendLocationQuickReply(sender, 'Lunch time')
                sendGif(sender, 'lunch')
            }
            else if (localTime.isAfter(LocalTime.of(18, 30)) && localTime.isBefore(LocalTime.of(21, 0))) {
                sendLocationQuickReply(sender, 'Dinner time')
                sendGif(sender, 'dinner')
            }
            else {
                sendLocationQuickReply(sender, 'Hungry at this time! Are you pregnant?')
                sendGif(sender, 'pregnant')
            }
        }
    }

    private GifResult getGifFor(query) {
        try {
            def results = restTemplate.exchange(TENOR_GIF_URL, HttpMethod.GET, null, new ParameterizedTypeReference<GifsResponse>() {}, [query: query])?.body?.results
            results ? results[new Random().nextInt(results?.size())] : null
        }
        catch (HttpClientErrorException _4xx) {
            log.error('Could not get GIFs for {}', query, _4xx.responseBodyAsString)
            null
        }
    }

    private void sendGif(sender, query) {
        GifResult result = getGifFor(query)
        if (result) {
            sendDataToMessenger([
                    recipient: [
                            id: sender
                    ],
                    message  : [
                            attachment   : [
                                    type   : 'image',
                                    payload: [
                                            url: "${result.media[0].tinygif.url}".toString()
                                    ]
                            ],
                            quick_replies: [[content_type: 'location']]
                    ]
            ])
        }
    }

    private TimeZone getLocalTimeAt(coordinates) {
        try {
            restTemplate.exchange(GOOGLE_TIMEZONE_URL, HttpMethod.GET, null, new ParameterizedTypeReference<TimeZone>() {}, [key: googleKey,lat: coordinates.lat, lon: coordinates.long, timestamp: Math.round(System.currentTimeMillis() / 1000)])?.body
        }
        catch (HttpClientErrorException _4xx) {
            log.error('Could not get TimeZone at {} due to {}', coordinates, _4xx.responseBodyAsString)
            null
        }
    }

    private void findFebo(sender, location) {
        List<Febo> febos = getFeboAt(location.coordinates)
        if (febos) {
            Febo nearest = febos.first()
            sendFeboInGenericTemplate(sender, location.coordinates, nearest)
            sendLocationQuickReply(sender, 'Share your location to find another Febo')
        }
        else {
            sendLocationQuickReply(sender, 'This damn place has no Febo. See you in Netherlands soon.')
        }
    }

    private Febo[] getFeboAt(coordinates) {
        try {
            restTemplate.exchange(FEBO_NL_URL, HttpMethod.GET, null, new ParameterizedTypeReference<List<Febo>>() {}, [lat: coordinates.lat, lon: coordinates.long]).body
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

    private void sendLocationQuickReply(sender, String message) {
        def data = getTextMessage(sender, message)
        data.message.quick_replies = [[content_type: 'location']]
        sendDataToMessenger(data)
    }

    private void sendFeboInGenericTemplate(sender, from, Febo febo) {
        sendDataToMessenger([
                recipient: [
                        id: sender
                ],
                message: [
                        attachment: [
                                type: 'template',
                                payload: [
                                        template_type: 'generic',
                                        elements: [
                                                [
                                                        title: "$febo.address, $febo.zip $febo.city" as String,
                                                        item_url: febo.permalink,
                                                        subtitle: new XmlSlurper().parseText(febo.hours).tr.find {Calendar.getInstance(new Locale('nl')).getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, new Locale('nl')).equalsIgnoreCase(it.td[0].text())}.td[1].text(),
                                                        buttons: [
                                                                [
                                                                        type: 'element_share'
                                                                ],
                                                                [
                                                                        type: 'phone_number',
                                                                        title: 'Call',
                                                                        payload: "+31${febo.phone.substring(1) - '-'}" as String
                                                                ],
                                                                [
                                                                        type: 'web_url',
                                                                        title: 'Navigate',
                                                                        url: new UriTemplate(GOOGLE_MAPS_DIRECTION_URL).expand([fromLat: from.lat, fromLon: from.long, toLat: febo.lat, toLon: febo.lng]).toString()
                                                                ]
                                                        ]
                                                ]
                                        ]
                                ]
                        ],
                        quick_replies: [[content_type: 'location']]
                ]
        ])
    }

    private void sendTextMessage(sender, String message) {
        sendDataToMessenger(getTextMessage(sender, message))
    }

    private static def getTextMessage(sender, String message) {
        return [
                recipient: [
                        id: sender
                ],
                message: [
                        text: message
                ]
        ]
    }


    private SendApiResponse sendDataToMessenger(data) {
        try {
            restTemplate.postForObject(GRAPH_API_URL + MESSENGER_PLATFORM_URI, data, SendApiResponse, [access_token: pageAccessToken])
        }
        catch (HttpClientErrorException _4xx) {
            log.error('Could not send {} due to {}', JsonOutput.toJson(data), _4xx.responseBodyAsString, _4xx)
            null
        }
    }
}
