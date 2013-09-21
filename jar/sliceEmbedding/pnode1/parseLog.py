#!/usr/bin/python
import sys


# Numpy is a library for handling arrays (like data points)
#import numpy as np

# Pyplot is a module within the matplotlib library for plotting
#import matplotlib.pyplot as plt



def parseLineWithWord(KEYWORDS):
    print KEYWORDS
    fo = open(logfile+"Parsed.txt", "w")
    global time
    log = open(logfile, "r").readlines()
    # print log
    time = []
    counter = 0
    for line in log:
        for word in line.split():
            #print word
            if word in KEYWORDS:
        #print line
               words = line.split()
               t= words[0]
               print t
               fo.write("%s\n" % t)
    fo.close() 
        
 

def parse():  
  fo = open(logfile+"Parsed.txt", "w")  
  global time
  log = open(logfile, "r").readlines()
 # print log
  time = []
  counter = 0
  for line in log:
    #print line
    words = line.split()
    t= words[0]
    print t
    fo.write("%s\n" % t)
  fo.close() 

def plotData(logfile):

 # Create an array of 100 linearly-spaced points from 0 to 2*pi
 # x = np.linspace(0,2*np.pi,100)
 # y = np.sin(x)

 # Create the plot
 plt.plot(time)

 # Save the figure in a separate file
 plt.savefig(logfile+'.png')

 # Draw the plot to the screen
 plt.show()


if __name__ == "__main__":

    logfile=sys.argv[1]
    #KEYWORDS = sys.argv[2]
#    parseLineWithWord(KEYWORDS)
    parse()
    #plotData(logfile)
