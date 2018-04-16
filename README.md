# hexagram30/terminal

*Telnet, SSL Telnet, SSH, and secure REPLs for use by hexagram30 projects*

[![][logo]][logo-large]


## Usage

Start up the terminal project's components (including an SSL/encrypted Telnet
server and a regular, unencrypted Telnet server):
```
$ lein start
```


To connect to the regular server, you may use any MUSH/MUD client or `telnet`
itself, e.g.:

```
telnet localhost 1123
```

To connect to the encrypted Telnet server on Debian-based Linux, you can use
the SSL-enabled `telnet` program:

```
telnet-ssl -z ssl localhost 1122
```

For other operating systems, you can use netcat:

```
ncat --telnet --ssl -n 127.0.0.1 1122
```

For use as part of a component-based system, see `src/hxgm30/terminal/components/`.

Note: SSH is not yet supported.


## Flow

The telnet server instances (regular and SSL-encrypted) in this project both
utilize the [Netty][netty] Java library. In addition, a Clojure `server`
namespace is provided that offers a straight-forward means of initializing,
starting, and stopping a telnet server with the respective public functions:
`init`, `start`, and `stop`.

High-level (Clojure API) initialization is responsible for setting up the
connection groups: one group for the "boss" thread that will be responsible for
accepting an incoming connection, and one group for the "worker" thread(s) that
will handle the traffic for the connection. Though it is possible to call `init`
manually, passing in options for the thread counts, by default `init` is called
by start and uses configuration to set the thread counts.

While calling `init` is optional, calling `start` is required. This function is
responsible for calling `init` if you haven't already, and for bootstrapping the
given telnet server. `start` returns the event loops from the call to `init`
(or the ones that were provided manually), and it is these event loops that are
passed to `stop` when it is time to shutdown the server.

The bootstrapping process is the code that's responsible for setting up the
telnet initializer. One initializer is created for each telnet server, but its
methods are called on a connection-by-connection basis: most important of these
is the method that initializes the Netty channel.

In turn, the most important thing the channel initialization does is set up
the handler that will respond to new lines sent over the wire.

The handler is responsible for several key actions:
* responding to when the channel first becomes active (this is usually where a
  banner/welcome screen is sent to the user)
* responding when a message is received
* flushing the context
* handling exceptions in communication

This is the part of the terminal code that ties into the shell implementations.


## License

Copyright © 2018, Hexagram30

Apache License, Version 2.0


<!-- Named page links below: /-->

[logo]: https://raw.githubusercontent.com/hexagram30/resources/master/branding/logo/h30-logo-1-long-with-text-x688.png
[logo-large]: https://raw.githubusercontent.com/hexagram30/resources/master/branding/logo/h30-logo-1-long-with-text-x3440.png
[comp-term]: https://github.com/hexagram30/hexagramMUSH/blob/master/src/hexagram30/mush/components/terminal.clj
[netty]: https://netty.io
