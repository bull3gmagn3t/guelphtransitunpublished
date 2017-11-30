##################################################
#grabFiles.py                                    #
#Written by William (Aidan) Maher                #
##################################################
#open links text file                            #
#read list of links  							 #
#download recent files						     #
##################################################
import copy
import time
import urllib

filePointer=open('data/links.txt','r')
linkList=list()
guelphLink='http://guelph.ca/wp-content/uploads/Route'
routeNameList=list()

for link in filePointer:
	#remove newlines for urlOpener
	link = link.rstrip('\r|\n')
	linkList.insert(0,link)

for link in linkList:
	print 'Downloading', link

	linkCopy = copy.deepcopy(link)
	tempString = linkCopy.partition('.xlsx')
	temp=tempString[0].partition(guelphLink)
	filename= linkCopy.partition(guelphLink)
	routeName=temp[2]
	filename = linkCopy.partition(guelphLink)

	routeNameList.insert(0,routeName)

	urlOpener=urllib.URLopener()
	urlOpener.retrieve(link,('data/sheets/'+filename[2]))
	print 'Done downloading schedule for ',routeName
filePointer.close()

 
