#!/bin/bash

lein clean &> /dev/null
lein with-profile +precompile deploy clojars
