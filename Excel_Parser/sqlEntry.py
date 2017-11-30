#!/usr/bin/python

""" mySQL functions to implement Guelph Transit Bus Schedule

This module connects to the mySQL Guelph Transit Database and creates
tables for each of the bus routes.  Information is received such as 
stop name, stop id and added to the tables.  Coordinates from a location
are calculated in this module using google Places API

"""

import MySQLdb as sql
import sys
import getpass

def connect(db):  

  # Connect to the database
  try:

    # Get mySQL information
    username = raw_input("Enter mySQL username: ")
    password = getpass.getpass("Enter mySQL password: ")

    # Database connection
    db = sql.connect(host = 'localhost', user = username, passwd = password, db = 'GuelphTransit')

  # Handle any errors that could occur
  except sql.Error, e:

    print "Error %d: %s" % (e.args[0], e.args[1])
    sys.exit(1)

  else:
    print "Connected to the Database"
    return db
 
def createRoutesTable(db):

  cur = db.cursor()
  print "Creating Routes Table..."

  cur.execute("DROP TABLE IF EXISTS Routes")
  print "Old Routes Table dropped."
   
  cur.execute("CREATE TABLE Routes (route varchar(100) NOT NULL, stopID varchar(5) NOT NULL, stopName VARCHAR(100) NOT NULL, Latitude FLOAT(10, 6), Longitude FLOAT(10, 6), timeList VARCHAR(1000)  NOT NULL, day VARCHAR(10) NOT NULL,  PRIMARY KEY (route, stopID, day))")

def close(db):

  # Close the MySQL connection
  print "Closed the connection"
  db.close()

def coordinates(db):
  # There is a seperate table that stores coordinates, 
  # coordinates must be copied over to main table

  query = db.cursor()
  query.execute("UPDATE Routes, Coordinates SET Routes.Longitude = Coordinates.Longitude WHERE Routes.stopID = Coordinates.stopID")

  query.execute("UPDATE Routes, Coordinates SET Routes.Latitude = Coordinates.Latitude WHERE Routes.stopID = Coordinates.stopID")
  db.commit()
  query.close()

def insert(db, info):

  route = info[0]

  print "Inserting " + route + " into database."
  for x in range(1, len(info)):
    stopName = str(info[x][0])
    stopID = str(info[x][1])
    day = str(info[x][2])
    timeList = ''
    for y in range(3, len(info[x])):
      timeList = timeList + str(info[x][y])+" "
    
    timeList = str(timeList)
    if len(stopID) == 3:
      insertStop(db, route, stopName, "0"+stopID, day, timeList)
    else:  
      insertStop(db, route, stopName, stopID, day, timeList)
    
  coordinates(db)
 
def insertStop(db, route, stopName, stopID, day, timeList):

  if route == "1A_CollegeEdinburgh":
    routeNum = str("1A")
  elif route == "1B_CollegeEdinburgh":
    routeNum = str("1B")
  elif route == "2A_WestLoop-Oct62014" or route == "2A_WestLoop":
    routeNum = str("2A")
  elif route == "2B_WestLoop":
    routeNum = str("2B")
  elif route == "3A_EastLoop":
    routeNum = str("3A")
  elif route == "3B_EastLoop":
    routeNum = str("3B")
  elif route == "4_York":
    routeNum = str("4")
  elif route == "5_Gordon1" or route == "5_Gordon":
    routeNum = str("5")
  elif route == "6_HarvardIronwood":
    routeNum = str("6")
  elif route == "7_KortrightDowney":
    routeNum = str("7")
  elif route == "8_StoneRoadMall":
    routeNum = str("8")
  elif route == "9_Waterloo":
    routeNum = str("9")
  elif route == "10_Imperial":
    routeNum = str("10")
  elif route == "11_WillowWest":
    routeNum = str("11")
  elif route == "12_GeneralHospital":
    routeNum = str("12")
  elif route == "13_VRRC":
    routeNum = str("13")
  elif route == "14_Grange":
    routeNum = str("14")
  elif route == "15_UniversityCollege":
    routeNum = str("15")
  elif route == "16_Southgate-Oct62014" or route == "16_Southgate":
    routeNum = str("16")
  elif route == "20_NorthwestIndustrial":
    routeNum = str("20")
  elif route == "50_StoneRoadExpress":
    routeNum = str("50")
  elif route == "56_VictoriaExpress" or route == "56_VictoriaExpress-30MinServiceSept2014":
    routeNum = str("56")
  elif route == "57_HarvardExpress":
    routeNum = str("57")
  elif route == "58_EdinburghExpress":
    routeNum = str("58")  
  else:
    routeNum = str("")
  Lat = None
  Long = None
  
  if (routeNum != ""):
    insert = ("INSERT IGNORE INTO Routes (route, stopName, stopID, Latitude, Longitude, timeList, day) VALUES (%s, %s, %s, %s, %s, %s, %s)")

    stopInfo = (routeNum, stopName, stopID, None, None, timeList, day)

    query = db.cursor()
    query.execute(insert, stopInfo)
    db.commit()


    query.close()
