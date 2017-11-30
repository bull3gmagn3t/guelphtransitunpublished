##################################################
#xlrdParse.py                                    #
#Written by William (Aidan) Maher                #
##################################################
#Parse the City of Guelph Excel files with xlrd  #
#And populate a python tuple with information    #
#and send to the database                        #
##################################################
#Base example from
#http://www.youlikeprogramming.com/2012/03/examples-reading-excel-xls-documents-using-pythons-xlrd/

#!/usr/bin/python

import xlrd,datetime
from datetime import time
import math
import ctypes
import subprocess
import os.path
from sqlEntry import insert
from sqlEntry import connect, close, createRoutesTable, insert

#global sheetCounter Var, workaround 
#instead of passing workbook to sub-functions
#that the parser calls
sheetCounter = 0

#Instantiate routeList tuple
routeList = ()
#tuple containing 2 tuple of stopID : stopName
#which will be converted to a dictionary
totalStopList = []
uniqueStopList = []
overLapStopList = []
######################readFile####################
#Read a file
#Accepts
#Workbook xlrd spreadsheet type
#And the name of the bus, (String type)
##################################################
def readFile(workbook,busName):
	route=() #ROUTE tuple we will store all data
	route+=(busName,)
	for count in range(0,workbook.nsheets):
		global sheetCounter
		sheetCounter = count
		print 'READING SHEET ',sheetCounter, fileN

		#read by sheet
		#sheetCounter = count
		worksheet = workbook.sheet_by_index(count)	
		stopList = readSheet(worksheet)
		#print stopList
		route += stopList
	return route
######################readSheet####################
#Read a worksheet
#Accepts 
#worksheet (xlrd worksheet type)
#Returns 
#A list of stops
###################################################
def readSheet(worksheet):
	num_rows = worksheet.nrows - 1
	num_cells = worksheet.ncols - 1
	curr_row = -1

	timeList = ""
	#stopName, stopID, times.
	stopList = ()

	while curr_row < num_rows:
		#clears timeList var, makes sure it's empty
		stopTimeList=()
		curr_row += 1
		row = worksheet.row(curr_row)
		busStop = readRow(row,num_cells,worksheet,curr_row)
		if(busStop is not None):
			#print busStop
			global totalStopList
			stopTup=()
			stopTup+=(str(busStop[1]),)
			stopTup+=(str(busStop[0]),)
			totalStopList.append(stopTup)
			stopList += (busStop,)
	return stopList
######################readRow######################
#Read the row from xlrd sheet type
#
#Accepts 
#row (xlrd row type)
#num_cells (int)
###################################################
def readRow(row,num_cells,worksheet,curr_row):
	busStop = ()
	stopTimeList = ()
	curr_cell = -1

	#print 'Row:', curr_row
	numTimes = 0 # number of times at a stop
	#print worksheet.ncols
	while curr_cell < num_cells:
		#iterate cell, A1 -> A2 -> A3 ----> B1 -> B2 -> B3
		#print curr_cell
		stopTime = ()
		stopID = ()
		curr_cell += 1
		# Cell Types: 0=Empty, 1=Text, 2=Number, 3=Date, 4=Boolean, 5=Error, 6=Blank
		#Get type and value
		cell_type = worksheet.cell_type(curr_row, curr_cell)
		cell_value = worksheet.cell_value(curr_row, curr_cell)
		#print cell_type
		#print cell_value
		#print len(str(cell_value))

		#times
		if cell_type == 3:
			x = cell_value# a float
			x = int(x * 24 * 3600) # convert to number of seconds
			hour = x//3600
			#if(hour>23):
			#	hour-=1
			minute = ((x%3600)/60.) #period after number to divide by signifies float division
			if minute < 59:
				minute = int(math.ceil(minute))
			else:
				minute= int(minute)

			#This fixes output to make sure there is a full string
			# i.e 12:07 as opposed to 12:7
			if(minute !=0 and minute >= 10):
				stopTime = str(str(hour)+":"+str(minute))
			elif(minute == 0):
				stopTime=str(str(hour)+":"+str(minute)+'0')
			elif(minute<10):
				stopTime=str(str(hour)+":"+'0'+str(minute))
			#print stopTime;
			#print " at "+stopName
			stopTime = (stopTime,)
			stopTimeList += stopTime
			numTimes = numTimes+1
			#end of file case
			if(curr_cell == worksheet.ncols-1):
				#print busStop
				busStop += stopTimeList
				return busStop

				#print busStop
				#print 'numTimes',numTimes
			

			#stopName
		elif ((cell_type == 1)and '-' not in(str(cell_value)) and len(str(cell_value))>4 

			and str(cell_value)!='Stop Name' and str(cell_value)!='Stop ID' 
			and str(cell_value)!='Stop ID' and str(cell_value) and str(cell_value)!='Schedule' and (str(cell_value)!='NexBus ID')):
			stopName=str(cell_value) #cast to str
			###################################################
			#print statements below for debugging
			###################################################
			#print('Stop Name' +stopName);
			#print 'Cell type'+str(cell_type)
			#print cell_type
			#print 'Cell value'+str(cell_value)+'\n'
			#print cell_value
			#print 'bus stop '+busStop
			###################################################
			busStop += (stopName,) #cast to str-tuple


			#stopID
		elif (cell_type == 1 or cell_type == 2) and ((len(str(cell_value)) == 4) or len(str(cell_value)) == 6) and '-' not in (str(cell_value)):
			#code for stopID
			stopID=int(cell_value)
			#print 'stopID: ',stopID
			stopID =(stopID,)
			busStop += stopID
			#print stopID
			if(workbook.nsheets!=1):
				global sheetCounter 
				sheetcounter=sheetCounter
				if(sheetcounter==0):
					busStop+= ('Week',)

				elif(sheetcounter==1):
					busStop+=('Sat',)

				elif(sheetcounter==2):
					busStop+=('Sun',)
			else:
				busStop+=('Express',)
			###################################################
			#Debugging print statements
			#print 'stopID : ',cell_value
			###################################################
			#end of row/bus stop
		elif (cell_type ==1 and '-' in cell_value and curr_cell==worksheet.ncols-1):
			busStop+= stopTimeList
			#print busStop
			return busStop


#1 LINE BELOW REQUIRED FOR SERVER Written by Anthony Mazzawi
#os.system("data/grabFiles.py")
####################getFiles#######################
#This function checks for all the files in data folder 
#by comparing it to the fileNames in links.txt
#if any of the files are missing, it will redownload them all
#with the grabFiles python script
###################################################
#def getFile(a):
def getFiles():
	filePointer=open('data/links.txt','r')
	for link  in filePointer:
		link = link.rstrip('\r|\n')
		t = link.partition(guelphLink)
		t2 = ('data/'+t[2])
		#if missing a file, re download them all in the list
		if(not(os.path.isfile(t2))):
			subprocess.call("python data/grabFiles.py", shell=True)
			break
	filePointer.close()

#3 LINES BELOW REQUIRED FOR SERVER Written by Anthony Mazzawi
#db = None
#db = connect(db)
#createRoutesTable(db)

#"MAIN" FUNCTION HERE

filePointer=open('data/links.txt','r')
guelphLink='http://guelph.ca/wp-content/uploads/Route'
fileNameList=list()
filesRead=0
nameList=list()


##################################################
#get filenames from URLS file for reading
##################################################
for link in filePointer:
	#remove newlines for urlOpener
	link = link.rstrip('\r|\n')
	t = link.partition(guelphLink)
	t2 = 'data/'+t[2]
	t3=t[2].partition('.xlsx')
	nameList.insert(0,t3[0])
	fileNameList.insert(0,t2)
filePointer.close()

currFile=0
print 'Files to read'
for t in fileNameList:
	print t
for fileN in fileNameList:
	#getFiles(1)
	getFiles()
	print 'READING FILE ', fileN
	workbook = xlrd.open_workbook(fileN)
	busName=str(nameList[currFile])	
	route=readFile(workbook,busName)
	#insert(db, route)
	currFile=currFile+1
	routeList+=(route,)
	filesRead=filesRead+1


		#print filesRead,' files read:'

#1 LINE BELOW REQUIRED FOR SERVER Written by Anthony Mazzawi
#close(db)
		###################################################
		#LINES BELOW FOR PRINT DEV/DEBUGGING OUTPUT INFO 
		###################################################
		#routeList
		#is the final output tuple.
		# ('1A_CollegeEdinburgh', ('University Centre', 100, 'Week', '6:0', ... ,timeN)),

		#  (busNameStr, (stopNameStr,stopIDstr,schedTypeStr,time1,time2, ... ,timeN))

		###########################################
		#print routeList
		##########################################
		#print route
			#for x in range(len(routeList)):
			#	print routeList[x]
			#print '\n'
			#for x in range(len(route)):
			#	print route[x]
			#	print 'x',x
# currStop=""
# stopName=""
# myInt = len(totalStopList)
# nonUniqueIndices=[]
# #print myInt
# for x in range(0,myInt):
# 	for y in range(x+1,myInt):
# 		if(totalStopList[x]==totalStopList[y]):
# 			totalStopList[y] = ()
# #filter(None,totalStopList)
# totalStopList = [x for x in totalStopList if x != ()]
# #for x in range(len(totalStopList)):
# 	print totalStopList[x]


# dictCount=dict()

# for x in range(len(totalStopList)):
# 	currStop = totalStopList[x][1]
# 	if(currStop not in dictCount):
# 		#dictCount.append({currStop:0})
# 		dictCount[currStop]=0
# 	else:
# 		dictCount[currStop]=dictCount[currStop]+1

# 	for y in range(x+1,len(totalStopList)):
# 		stopNameStr = totalStopList[y][1]
# 		if(stopNameStr == currStop ):
# 			nonUniqueIndices.append(y)

# for x in range (len(totalStopList)):
# 	if (x in nonUniqueIndices):
# 		overLapStopList.append(totalStopList[x])


# overLapStopList.sort(key=lambda a : a[1])
# print 'OVERLAPPING LIST'
# for x in range(len(overLapStopList)):
# 	print overLapStopList[x]

# for x in dictCount:
# 	print str(x)+':'+str(dictCount[x])

# uniqueStopListNames=[]

# for x in dictCount:
# 	if(dictCount[x]==0):
# 		uniqueStopListNames.append(x)

# #for x in range(len(uniqueStopList)):
# #	print uniqueStopList[x]

# for x in totalStopList:
# 	if (x[1] in uniqueStopListNames):
# 		uniqueStopList.append(x)
# #print '\n'
# #print 'UNIQUE STOP LIST'
# #for x in range(len(uniqueStopList)):
# #	print uniqueStopList[x]
