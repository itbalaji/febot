package com.herokuapp.febotnl.google.maps.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.Immutable

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
class TimeZone {
    int dstOffset, rawOffset
    String timeZoneId, timeZoneName, status, error_message
}
