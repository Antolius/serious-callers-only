this register commandProtocol("/pick-a-book-declarative".cmd) {
    replyPublicly {
        +section("Looking for fiction or a technical book?".txt) {
            accessory = select("Pick one!".txt) {
                +option("Technical".txt) {
                    replyPublicly {
                        +section("_The Pragmatic Programmer_ is a classic.".md)
                        +context { +"It's written by Andrew Hunt".txt }
                    }
                }
                +option("Fiction".txt) {
                    replyPublicly {
                        +section("Have particular genre in mind?".txt) {
                            accessory = select("Pick one!".txt) {
                                +option("Science Fiction".txt) {
                                    replyPublicly {
                                        +section("I particularly enjoy _Excession_.".md)
                                        +context { +"It's written by Iain M. Banks".txt }
                                    }
                                }
                                +option("Fantasy".txt) {
                                    replyPublicly {
                                        +section("_Foundryside_ is an absolute treasure!".md)
                                        +context { +"It's written by Robert Jackson Bennett".txt }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
