package com.herokuapp.febotnl.messenger

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

import java.time.LocalDateTime

/**
 * Try Messenger Extension and plugin
 */
@Slf4j
@Controller
@RequestMapping('/messenger/web')
class MessengerWeb {
    final String facebookAppId
    final String febotPageId
    final String ROOT_URL
    final String PING_PATH
    LocalDateTime lastSuccessfulPing

    @Autowired
    MessengerWeb(Environment environment) {
        facebookAppId = environment.getRequiredProperty('facebook-app-id')
        febotPageId = environment.getRequiredProperty('febot-page-id')
        ROOT_URL = environment.getRequiredProperty('ROOT_URL')
        PING_PATH = environment.getRequiredProperty('PING_PATH')
        lastSuccessfulPing = LocalDateTime.MIN
    }


    @RequestMapping(method = RequestMethod.GET)
    String web(Model model) {
        model.addAttribute('app_id', facebookAppId)
        model.addAttribute('page_id', febotPageId)
        model.addAttribute('user_ref', UUID.randomUUID().toString())
        return 'index'
    }

    @Scheduled(fixedDelay = 300_000L)
    void ping() {
        try {
            if (lastSuccessfulPing < LocalDateTime.now().minusMinutes(55)) {
                HttpURLConnection connection = new URL("$ROOT_URL$PING_PATH").openConnection() as HttpURLConnection
                connection.connectTimeout = 2000
                connection.readTimeout = 2000
                connection.requestMethod = 'HEAD'
                def responseCode = connection.responseCode
                log.info('Received {} for ping', responseCode)
                lastSuccessfulPing = LocalDateTime.now()
            }
        } catch (IOException exception) {
            log.error('Scheduled ping failed', exception)
        }
    }
}
