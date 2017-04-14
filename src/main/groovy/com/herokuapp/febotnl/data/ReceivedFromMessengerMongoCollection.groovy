package com.herokuapp.febotnl.data

import com.herokuapp.febotnl.messenger.model.ReceivedFromMessenger
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface ReceivedFromMessengerMongoCollection extends MongoRepository<ReceivedFromMessenger, String> {
    /* TODO
    @Query('''
        {
            "data.entry.messaging.message": {
                "mid": ?0,
                "seq: ?1
            }
        }
    ''')
    ReceivedFromMessenger findByMessageIdAndSeq(String messageId, long seq)
    */
}