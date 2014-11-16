# Osiris


## Overview
Osiris responds to Aker notifications, sending a Webhook request to Iris for each webhook of each changed document.

## Webhook documents

Webhook documents in the Underworld DB should have the form:

    {
        "type": "webhook",
        "db": "<databse name>",
        "trigger_type": "<document type>"
    }

We have not implemented conditional triggers.

<!---
Should we be using Regex or EDN-matching?
-->


## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## License

[Eclipse Public License](https://www.eclipse.org/legal/epl-v10.html)

Copyright © 2014 Physion LLC
