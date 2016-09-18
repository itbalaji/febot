package com.herokuapp.febotnl.google.places.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.Immutable

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
class Photo {
    int height, width
    @JsonProperty('html_attributions') List<String> htmlAttributions
    @JsonProperty('photo_reference') String photoReference
}
