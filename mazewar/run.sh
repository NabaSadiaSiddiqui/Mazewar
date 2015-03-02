#!/bin/bash
JAVA_HOME=/cad2/ece419s/java/jdk1.6.0/

# arguments to Mazewar client
# $1 = hostname of where MazewarServer is located
# $2 = port # where MazewarServer is listening
# $3 = hostname of where Client is located
# $4 = port # where Client is listening

${JAVA_HOME}/bin/java Mazewar $1 $2 $3 $4
