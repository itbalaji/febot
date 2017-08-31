package com.herokuapp.febotnl.messenger

interface Constants {
    long STICKER_THUMBS_UP = 369239263222822
    String GRAPH_API_URL = 'https://graph.facebook.com/v2.8'
    String MESSENGER_PLATFORM_URI = '/me/messages?access_token={access_token}'
    String GOOGLE_PLACES_URL = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?key={key}&location={lat},{lon}&rankby=distance&name=febo'
    String GOOGLE_TIMEZONE_URL = 'https://maps.googleapis.com/maps/api/timezone/json?key={key}&location={lat},{lon}&timestamp={timestamp}'
    String GOOGLE_MAPS_DIRECTION_URL = 'https://maps.google.com?saddr={fromLat},{fromLon}&daddr={toLat},{toLon}'
    String FEBO_NL_URL = 'https://www.febo.nl/wp-admin/admin-ajax.php?action=store_search&lat={lat}&lng={lon}&max_results=30&radius=5'
    String TENOR_GIF_URL = 'https://api.tenor.co/v1/search?tag={query}&key=LIVDSRZULELA&safesearch=strict'
}
