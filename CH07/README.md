= Chapter 7

== Configuration

- make sure to install postgreSQL and to create the `chapter7` database with the correct credentials
- put your twitter credentials in `conf/twitter.conf`

== Usage

- login via http://localhost:9000/login (user: bob@marley.org, password: secret)
- connect via telnet: `telnet localhost 6666`
- register: `+123 register twitterHandle`
- subscribe mentions: `+123 subscribe mentions`