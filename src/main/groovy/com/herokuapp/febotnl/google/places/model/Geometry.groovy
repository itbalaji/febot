package com.herokuapp.febotnl.google.places.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.Immutable

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
class Geometry {
    Location location
}
