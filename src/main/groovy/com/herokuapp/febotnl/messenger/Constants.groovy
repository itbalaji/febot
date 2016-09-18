package com.herokuapp.febotnl.messenger

interface Constants {
    long STICKER_THUMBS_UP = 369239263222822
    String GRAPH_API_URL = 'https://graph.facebook.com/v2.7/me/messages?access_token={access_token}'
    String GOOGLE_PLACES_URL = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?key={key}&location={lat},{lon}&rankby=distance&name=febo'
}
