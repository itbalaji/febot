package com.herokuapp.febotnl.google.places.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.Immutable

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
class Response {
    @JsonProperty('html_attributions') List<String> htmlAttributions
    List<Result> results
    ResponseStatus status
}
