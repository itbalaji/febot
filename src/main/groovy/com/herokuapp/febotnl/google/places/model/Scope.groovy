package com.herokuapp.febotnl.google.places.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
enum Scope {
    GOOGLE, APP
}