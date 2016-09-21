package com.herokuapp.febotnl.messenger

interface Constants {
    long STICKER_THUMBS_UP = 369239263222822
    String GRAPH_API_URL = 'https://graph.facebook.com/v2.7/me/messages?access_token={access_token}'
    String GOOGLE_PLACES_URL = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?key={key}&location={lat},{lon}&rankby=distance&name=febo'
    String GOOGLE_MAPS_DIRECTION_URL = 'https://www.google.com/maps?saddr=My+Location&daddr={lat},{lon}'
    String FEBO_NL_URL = 'http://www.febo.nl/wp-admin/admin-ajax.php?action=store_search&lat={lat}&lng={lon}&max_results=30&radius=5'
}
