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


## License

Copyright © 2018, Hexagram30

Apache License, Version 2.0


<!-- Named page links below: /-->

[logo]: https://raw.githubusercontent.com/hexagram30/resources/master/branding/logo/h30-logo-1-long-with-text-x688.png
[logo-large]: https://raw.githubusercontent.com/hexagram30/resources/master/branding/logo/h30-logo-1-long-with-text-x3440.png
[comp-term]: https://github.com/hexagram30/hexagramMUSH/blob/master/src/hexagram30/mush/components/terminal.clj
