package co.tenor.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import groovy.transform.Immutable

@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
class GifsResponse {
    String next
    List<GifResult> results
}
