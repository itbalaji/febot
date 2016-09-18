package com.herokuapp.febotnl.messenger.webhook

import com.herokuapp.febotnl.google.places.model.OpeningHours
import com.herokuapp.febotnl.google.places.model.Response
import com.herokuapp.febotnl.google.places.model.ResponseStatus
import com.herokuapp.febotnl.google.places.model.Result
import com.herokuapp.febotnl.messenger.model.SendApiResponse
import groovy.json.JsonSlurper
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static com.herokuapp.febotnl.messenger.Constants.GOOGLE_PLACES_URL
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
        env.getRequiredProperty('google-key') >> 'k3y'
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

    def 'Webhook sticker'() {
        given:
        def body = new JsonSlurper().parseText('{"object":"page","entry":[{"id":"999999999999999","time":1474035686272,"messaging":[{"sender":{"id":"9999999999999999"},"recipient":{"id":"999999999999999"},"timestamp":1474035663272,"message":{"mid":"mid.9999999999999:a1a1a1a1a1a1a1a1a1","seq":19,"sticker_id":369239263222822,"attachments":[{"type":"image","payload":{"url":"https://scontent.xx.fbcdn.net/t39.1997-6/851557_369239266556155_759568595_n.png?_nc_ad=z-m"}}]}}]}]}')

        when:
        ResponseEntity<String> result = controller.webhook(body)

        then:
        result
        result.statusCode.'2xxSuccessful'
        1 * template.postForObject(GRAPH_API_URL, {it.message.text == 'you are welcome 😊'}, SendApiResponse, _)
    }

    def 'Webhook location - nothing open'() {
        given:
        def body = new JsonSlurper().parseText('{"object":"page","entry":[{"id":"123456789012345","time":1474193856227,"messaging":[{"sender":{"id":"123456789012345"},"recipient":{"id":"123456789012345"},"timestamp":1474193856068,"message":{"mid":"mid.1234567890123:123456789012345asd","seq":30,"attachments":[{"title":"My Location","url":"https://www.facebook.com/l.php?u=https","type":"location","payload":{"coordinates":{"lat":52,"long":4}}}]}}]}]}')
        1 * template.getForObject(GOOGLE_PLACES_URL, Response, [key: 'k3y', lat: 52, lon: 4]) >> new Response([], [new Result(vicinity: '1 Green Road, Greenland')], ResponseStatus.OK)

        when:
        ResponseEntity<String> result = controller.webhook(body)

        then:
        result
        result.statusCode.'2xxSuccessful'
        1 * template.postForObject(GRAPH_API_URL, {it.message.text == 'The nearest FEBO is at 1 Green Road, Greenland'}, SendApiResponse, _)
        1 * template.postForObject(GRAPH_API_URL, {it.message.text == 'but it is probably closed now 😞'}, SendApiResponse, _)
        1 * template.postForObject(GRAPH_API_URL, {it.message.text == 'Unfortunately there is nothing near that is open now'}, SendApiResponse, _)
    }

    def 'Webhook location - nearest is open'() {
        given:
        def body = new JsonSlurper().parseText('{"object":"page","entry":[{"id":"123456789012345","time":1474193856227,"messaging":[{"sender":{"id":"123456789012345"},"recipient":{"id":"123456789012345"},"timestamp":1474193856068,"message":{"mid":"mid.1234567890123:123456789012345asd","seq":30,"attachments":[{"title":"My Location","url":"https://www.facebook.com/l.php?u=https","type":"location","payload":{"coordinates":{"lat":52,"long":4}}}]}}]}]}')
        1 * template.getForObject(GOOGLE_PLACES_URL, Response, [key: 'k3y', lat: 52, lon: 4]) >> new Response([], [new Result(openingHours: new OpeningHours(true), vicinity: '1 Green Road, Greenland')], ResponseStatus.OK)

        when:
        ResponseEntity<String> result = controller.webhook(body)

        then:
        result
        result.statusCode.'2xxSuccessful'
        1 * template.postForObject(GRAPH_API_URL, {it.message.text == 'The nearest FEBO is at 1 Green Road, Greenland'}, SendApiResponse, _)
        1 * template.postForObject(GRAPH_API_URL, {it.message.text == 'and it is open now!!'}, SendApiResponse, _)
    }

    def 'Webhook location - nearest not open but another open'() {
        given:
        def body = new JsonSlurper().parseText('{"object":"page","entry":[{"id":"123456789012345","time":1474193856227,"messaging":[{"sender":{"id":"123456789012345"},"recipient":{"id":"123456789012345"},"timestamp":1474193856068,"message":{"mid":"mid.1234567890123:123456789012345asd","seq":30,"attachments":[{"title":"My Location","url":"https://www.facebook.com/l.php?u=https","type":"location","payload":{"coordinates":{"lat":52,"long":4}}}]}}]}]}')
        1 * template.getForObject(GOOGLE_PLACES_URL, Response, [key: 'k3y', lat: 52, lon: 4]) >> new Response([], [new Result(openingHours: new OpeningHours(false), vicinity: '1 Green Road, Greenland'), new Result(openingHours: new OpeningHours(true), vicinity: '2 Green Road, Greenland')], ResponseStatus.OK)

        when:
        ResponseEntity<String> result = controller.webhook(body)

        then:
        result
        result.statusCode.'2xxSuccessful'
        1 * template.postForObject(GRAPH_API_URL, {it.message.text == 'The nearest FEBO is at 1 Green Road, Greenland'}, SendApiResponse, _)
        1 * template.postForObject(GRAPH_API_URL, {it.message.text == 'but it is probably closed now 😞'}, SendApiResponse, _)
        1 * template.postForObject(GRAPH_API_URL, {it.message.text == 'The nearest FEBO that is open now is at 2 Green Road, Greenland'}, SendApiResponse, _)
    }
}
