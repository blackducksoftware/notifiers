# notifiers
## Overview

The notifiers project contains two utilities for alerting users to conditions within
a Protex project that indicate the need to take action. 

The conditions checked by the notifier utility are configurable. The available
conditions are: 

 1. Components that have pending IDs. 
 1. Non-approved components. 
 1. License conflicts.

The mechanism used to alert users of the existence of the configured condition(s) depends on the
notifier utility used:

 1. EmailNotifier sends email.
 1. JiraConnector creates or updates Issues in JIRA.

## Where can I get the latest release?

You can download the latest source from GitHub: https://github.com/blackducksoftware/notifiers.

## Documentation

Please see wiki for more information: https://github.com/blackducksoftware/notifiers/wiki

You can also find sample configuration files in the samples directory within the deliverable zip
file (src/main/resources/info in the source project).

## License

GNU General Public License v2.0 only.