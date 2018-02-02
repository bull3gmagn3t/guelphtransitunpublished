# README #
### What is this repository for? ###
* Quick summary
  There is an Amazon ec2 server up, but it will only be running until September 2018
  This is an Android app which shows bus route data, which has been parsed from an excel spreadsheet via python and put into a database.
Requires:  
	-LAMP server with Python2.7   
	-Selenium web-scraper (requires Java) (http://selenium-release.storage.googleapis.com/2.45/selenium-server-standalone-2.45.0.jar),   
	-mySQL set up with a database filled with bus routes which can be generated via Excel_Parser folder's code (See Database Configuration in this README for the Database schema).  


__Excel_Parser__
This python code which is executed from xlrdParse.py parses Excel spreadsheets using the pythong xlrd library and loads the parsed data into the database specified in sqlEntry.py
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###
* Projects
Open 
* Database configuration
mySQL 

CREATE TABLE Routes (
	`_id`	varchar ( 100 ) NOT NULL,
	`stopID`	varchar ( 5 ) NOT NULL,
	`stopName`	varchar ( 100 ) NOT NULL,
	`latitude`	float ( 10 , 6 ) DEFAULT NULL,
	`longitude`	float ( 10 , 6 ) DEFAULT NULL,
	`weekTimes`	varchar ( 1000 ) DEFAULT NULL,
	`satTimes`	varchar ( 1000 ) DEFAULT NULL,
	`sunTimes`	varchar ( 1000 ) DEFAULT NULL
);

* How to run tests
* Deployment instructions

* Detailed overview
.

├── Excel_Parser  
│   ├── androidSqlConnect  
│   │   ├── db_config.php  
│   │   ├── db_connect.php  
│   │   ├── getNextBus.php  
│   │   ├── getRoutes.php  
│   │   └── getUpdateTime.php  
│   ├── data  
│   │   ├── coordinates.txt  
│   │   ├── express.txt  
│   │   ├── grabFiles.py  
│   │   ├── linkscopy.txt  
│   │   ├── links.txt  
│   │   └── sheets  
│   │       ├── 10_Imperial.xlsx  
│   │       ├── 11_WillowWest.xlsx  
│   │       ... 20 more entries  
│   ├── sqlEntry.py  
│   └── xlrdParse.py  
├── GuelphTransit 	    __GuelphTransit (With Amazon Server support)__  
│   ├── app  
│   │   │     
│   │   └── src	  
│   │       └── main  
│   │  
│   │           ├── java  
│   │           │   └── velocityraptor  
│   │           │       └── guelphtransit  
|   |           |	    |└──1 class  
│   │           │           ├── interfaces  
│   │           │           │   └── ScrapeRespCallback.java  
│   │           │           ├── listeners  
│   │           │           │   ├── 2 Classes  
│   │           │           ├── main  
│   │           │           │   ├── 8 classes  
│   │           │           │   ├── MainActivity.java  
│   │           │           │   │   ├── 2 classes  
│   │           │           │   ├── fragmentRouteBar  
│   │           │           │   │   ├── 2 classes  
│   │           │           │   ├── loadStops  
│   │           │           │   │   ├── 4 classes  
│   │           │           │     
│   │           │           ├── out.txt  
│   │           │           └── Stop.class  
├── README.md  
└── serverSideStuff  
    ├── php_shell.sh  
    └── wrapper.c  
  
589 directories, 1489 files  


### Who do I talk to? ###

* Repo owner or admin
maher.aidan1@gmail.com
* Other community or team contact
