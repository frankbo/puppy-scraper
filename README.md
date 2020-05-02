# Puppy Scraper

Puppy scraper is a script that runs from time to time, to scape/crawl animal shelter web pages.
It collects all data of new Dogs and sends them to a telegram Bot. This bot is in a group and posts
the Dogs with their names, their shelter and a picture of the dog (if available).

## Supported Shelters
Currently only some shelters are supported. They are scraped with `scala-scraper` and the list
could be extended easily. Supported shelters are:
* Iserlohn (http://www.tierheim-iserlohn.de/)
* Olpe (https://www.tierheim-olpe.de)

### No Server
For now the script only runs locally and not on a server.
To run it locally an `application.conf` file needs to be created
with the credentials of the Bot. The relevant config params can be found
under `Model.ServiceConf`.