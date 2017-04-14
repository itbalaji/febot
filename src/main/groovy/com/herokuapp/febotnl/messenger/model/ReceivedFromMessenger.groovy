package com.herokuapp.febotnl.messenger.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.Immutable
import org.springframework.data.annotation.Id

/**
 * The model for the Webhook from Messenger
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
class ReceivedFromMessenger {
    @Id
    String id
    def data
}
