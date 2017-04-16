package com.herokuapp.febotnl.messenger

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

/**
 * Try Messenger Extension and plugin
 */
@Slf4j
@Controller
@RequestMapping('/messenger/web')
class MessengerWeb {
    final String facebookAppId
    final String febotPageId

    @Autowired
    MessengerWeb(Environment environment) {
        facebookAppId = environment.getRequiredProperty('facebook-app-id')
        febotPageId = environment.getRequiredProperty('febot-page-id')
    }


    @RequestMapping(method = RequestMethod.GET)
    String web(Model model) {
        model.addAttribute('app_id', facebookAppId)
        model.addAttribute('page_id', febotPageId)
        model.addAttribute('user_ref', UUID.randomUUID().toString())
        return 'index'
    }

}
