# hexagram30/terminal

*Telnet, SSL Telnet, SSH, and secure REPLs for use by hexagram30 projects*

[![][logo]][logo-large]


## Usage

Stand-alone telnet server:
```
$ lein start-telnet
```

If you wish to run the server with SSL, update the configuration to include
SSL data, e.g:

```edn
{:telnet {:port 1130
          :ssl {:enabled? true
                :fqdn "hexagram30.mush"
                :pkey-bits 4096}}
 ...}
```

Once stated with SSL enabled, use an SSL telnet client to connect, e.g.:

```
telnet-ssl -z ssl localhost 1130
```

For use as part of a component-based system, see
[hxgm30.mush.components.terminal][comp-term].


## License

Copyright © 2018, Hexagram30

Apache License, Version 2.0


<!-- Named page links below: /-->

[logo]: https://raw.githubusercontent.com/hexagram30/resources/master/branding/logo/h30-logo-1-long-with-text-x688.png
[logo-large]: https://raw.githubusercontent.com/hexagram30/resources/master/branding/logo/h30-logo-1-long-with-text-x3440.png
[comp-term]: https://github.com/hexagram30/hexagramMUSH/blob/master/src/hexagram30/mush/components/terminal.clj
