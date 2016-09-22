package com.herokuapp.febotnl.messenger.webhook

import com.herokuapp.febotnl.febo.model.Febo
import com.herokuapp.febotnl.messenger.model.SendApiResponse
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static com.herokuapp.febotnl.messenger.Constants.FEBO_NL_URL
import static com.herokuapp.febotnl.messenger.Constants.GRAPH_API_URL
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.OK

/**
 * Test for MessengerWebhookController
 */
class MessengerWebhookControllerTest extends Specification {
    Environment env
    MessengerWebhookController controller
    RestTemplate template
    MockHttpServletRequest request

    def setup() {
        env = Mock(Environment)
        env.getRequiredProperty('facebook-webhook-token') >> 't0k3n'
        env.getRequiredProperty('google-key') >> 'k3y'
        env.getRequiredProperty('facebook-app-secret') >> 's3cr3t'
        template = Mock(RestTemplate)
        request = new MockHttpServletRequest()
        controller = new MessengerWebhookController(env, template, new MappingJackson2HttpMessageConverter())
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

    def 'Webhook payload not Facebook'() {
        given:
        request.setContent('{"object":"page","entry":[{"id":"999999999999999","time":1474035686272,"messaging":[{"sender":{"id":"9999999999999999"},"recipient":{"id":"999999999999999"},"timestamp":1474035663272,"message":{"mid":"mid.9999999999999:a1a1a1a1a1a1a1a1a1","seq":19,"sticker_id":369239263222822,"attachments":[{"type":"image","payload":{"url":"https://scontent.xx.fbcdn.net/t39.1997-6/851557_369239266556155_759568595_n.png?_nc_ad=z-m"}}]}}]}]}'.bytes)

        when:
        ResponseEntity<String> result = controller.webhook(request, 'sha1=0aadf6c6435ac95d1c22e200434981888122a867')

        then:
        result
        result.statusCode == FORBIDDEN

        when:
        result = controller.webhook(request, '122a867')

        then:
        result
        result.statusCode == FORBIDDEN
    }

    def 'Webhook sticker'() {
        given:
        request.setContent('{"object":"page","entry":[{"id":"999999999999999","time":1474035686272,"messaging":[{"sender":{"id":"9999999999999999"},"recipient":{"id":"999999999999999"},"timestamp":1474035663272,"message":{"mid":"mid.9999999999999:a1a1a1a1a1a1a1a1a1","seq":19,"sticker_id":369239263222822,"attachments":[{"type":"image","payload":{"url":"https://scontent.xx.fbcdn.net/t39.1997-6/851557_369239266556155_759568595_n.png?_nc_ad=z-m"}}]}}]}]}'.bytes)

        when:
        ResponseEntity<String> result = controller.webhook(request, 'sha1=cab07c4e8a665e77e08d4bc2683d2637af1030ef')

        then:
        result
        result.statusCode.'2xxSuccessful'
        1 * template.postForObject(GRAPH_API_URL, {it.message.text == 'you are welcome 😊'}, SendApiResponse, _)
    }

    def 'Webhook location'() {
        given:
        request.setContent('{"object":"page","entry":[{"id":"123456789012345","time":1474193856227,"messaging":[{"sender":{"id":"123456789012345"},"recipient":{"id":"123456789012345"},"timestamp":1474193856068,"message":{"mid":"mid.1234567890123:123456789012345asd","seq":30,"attachments":[{"title":"My Location","url":"https://www.facebook.com/l.php?u=https","type":"location","payload":{"coordinates":{"lat":52,"long":4}}}]}}]}]}'.bytes)
        1 * template.exchange(FEBO_NL_URL, HttpMethod.GET, null, *_) >> new ResponseEntity<>(new Febo(phone: '020-6912593', hours: '<table class="wpsl-opening-hours"><tr><td>Maandag</td><td><time>10:00 - 20:00</time></td></tr><tr><td>Dinsdag</td><td><time>10:00 - 20:00</time></td></tr><tr><td>Woensdag</td><td><time>10:00 - 20:00</time></td></tr><tr><td>Donderdag</td><td><time>10:00 - 20:00</time></td></tr><tr><td>Vrijdag</td><td><time>10:00 - 20:00</time></td></tr><tr><td>Zaterdag</td><td><time>10:00 - 20:00</time></td></tr><tr><td>Zondag</td><td><time>10:00 - 20:00</time></td></tr></table>'), OK)

        when:
        ResponseEntity<String> result = controller.webhook(request, 'sha1=0aadf6c6435ac95d1c22e200434981888122a867')

        then:
        result
        result.statusCode.'2xxSuccessful'
        1 * template.postForObject(GRAPH_API_URL, {
            it.message.attachment.type == 'template' &&
                    it.message.attachment.payload.template_type == 'generic' &&
                    it.message.attachment.payload.elements.size() == 1 &&
                    it.message.attachment.payload.elements[0].buttons.size() == 3
        }, SendApiResponse, _)
    }

    def 'Webhook text'() {
        given:
        request.setContent('{"object":"page","entry":[{"id":"123456789012345","time":1474193856227,"messaging":[{"sender":{"id":"123456789012345"},"recipient":{"id":"123456789012345"},"timestamp":1474193856068,"message":{"mid":"mid.1234567890123:123456789012345asd","seq":30,"text":"COUP D\'ŒIL"}}]}]}'.bytes)

        when:
        ResponseEntity<String> result = controller.webhook(request, 'sha1=90dd6de3e11b69ac777b8b32d3da7ebf848b942d')

        then:
        result
        result.statusCode.'2xxSuccessful'
        1 * template.postForObject(GRAPH_API_URL, {it.message.text == 'I wish I understood what you say 😞. Why don\'t you try sending your location?' && it.message.quick_replies.size() == 1 && it.message.quick_replies[0].content_type == 'location'}, SendApiResponse, _)
    }
}
