# twitter-prime-time

Tool for estimating the best time to post on Twitter based on followers' activity. The tool retrieves public timelines of followers for a given Twitter account and aggregates the activities into hour-long groups during a week. Due to the limitations of the Twitter API, only 200 items per follower are retrieved. 

## Usage

Go to <https://apps.twitter.com/>, create a new application, generate access credentials for it, and save them in the environment (using [environ](https://github.com/weavejester/environ)). For example, in `.lein-env`:

```clojure
{
  :twitter-prime-time {
    :app-consumer-key "Consumer Key (API Key)"
    :app-consumer-secret "Consumer Secret (API Secret)"
    :user-access-token "Access Token"
    :user-access-token-secret "Access Token Secret"
  }
}
```

Once the Twitter API credentials are set, you can run the tool either using [Leiningen](http://leiningen.org/):

```bash
lein run - -h
```

Or via a compiled JAR-file:

```bash
lein clean; lein uberjar
java -jar target/twitter-prime-time-0.1.0-SNAPSHOT-standalone.jar -h 
```

Please bear in mind that retrieval of a lot of followers data will typically take long time.

## License

Copyright © 2014 Jindřich Mynarz

Distributed under the Eclipse Public License version 1.0. 
