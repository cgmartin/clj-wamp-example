# clj-wamp-example

An example Clojure WebSockets project using [clj-wamp](https://github.com/cgmartin/clj-wamp).
Powers the [cljwamp.us](http://cljwamp.us) website.

Visit [cljwamp.us](http://cljwamp.us) for live demos and additional information.

For information on the WAMP specification, visit [wamp.ws](http://wamp.ws).

## Examples

For the [Chat Demo](http://cljwamp.us/chat), take a look at the following files:

 * [src/clj_wamp_example/views/chat.html](https://github.com/cgmartin/clj-wamp-example/blob/master/src/clj_wamp_example/views/chat.html)
 * [src/clj_wamp_example/websocket.clj](https://github.com/cgmartin/clj-wamp-example/blob/master/src/clj_wamp_example/websocket.clj)
 * [resources/public/js/chat.js](https://github.com/cgmartin/clj-wamp-example/blob/master/resources/public/js/chat.js)

For the [RPC Demo](http://cljwamp.us/rpc):

 * TODO

## Technologies used

Server side:

 * [clj-wamp](https://github.com/cgmartin/clj-wamp) - Clojure implementation of the WebSocket Application Messaging Protocol
 * [HTTP Kit](http://http-kit.org/) - Ring-compatible HTTP server for Clojure
 * [Compojure](https://github.com/weavejester/compojure) - A concise routing DSL for Ring/Clojure
 * [Clabango](https://github.com/danlarkin/clabango) - Templating language for Clojure

Client side:

 * [AutobahnJS](http://autobahn.ws/) - WAMP over WebSocket JavaScript client
 * [jQuery](http://jquery.com/) - Versatile JavaScript library
 * [Twitter Bootstrap](http://twitter.github.io/bootstrap/) - CSS framework

## License

Copyright © 2013 Christopher Martin

Distributed under the Eclipse Public License, the same as Clojure.
