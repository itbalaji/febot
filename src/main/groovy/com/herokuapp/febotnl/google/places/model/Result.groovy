package com.herokuapp.febotnl.google.places.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.Immutable

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
class Result {
    Geometry geometry
    String icon, id, name
    @JsonProperty('opening_hours') OpeningHours openingHours
    List<Photo> photos
    @JsonProperty('place_id') String placeId
    Scope scope
    String reference
    List<String> types
    String vicinity
}
