package com.herokuapp.febotnl.messenger.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.Immutable

@JsonIgnoreProperties(ignoreUnknown = true)
@Immutable
class SendApiResponse {
    String recipient_id
    String message_id
}
