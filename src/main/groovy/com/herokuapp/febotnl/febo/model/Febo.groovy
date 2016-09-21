package com.herokuapp.febotnl.febo.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.Immutable

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
class Febo {
    String address, store, thumb, id, permalink, address2, city, state, zip, country, lat, lng, phone, fax, email, hours, url, since
    double distance
}
