#!/bin/sh
#
#
# start/stop gamebots
#
MEDIA=/var/keyworx/webapps/walkandplay/wptest/media
KWX=../lib/keyworx.jar
PID_FILE=gamebot.pid
PID_FILE2=java.pid

function start() {
  echo $$ > ${PID_FILE}
  while [ 1 ]
  do
    echo "++++++++START BLUE BOT++++++++++`date`"
    cp ${MEDIA}/save/* ${MEDIA}
    java -cp $KWX  org.keyworx.amuse.test.protocol.Main ../cfg/gamebot-blue.properties &
    echo $! > ${PID_FILE2}
    sleep 120
    echo "++++++++START RED  BOT++++++++++`date`"
    java -cp $KWX  org.keyworx.amuse.test.protocol.Main ../cfg/gamebot-red.properties
  done
}

function stop() {
  pid=`cat  ${PID_FILE}`
  pid2=`cat  ${PID_FILE2}`
  echo "stop $pid and $pid2"
  kill -9 $pid
  kill -9 $pid2
  /bin/rm  ${PID_FILE} ${PID_FILE2}
}

case "$1" in
  'start')
 start
 ;;

  'stop')
 stop
 ;;

 *)
    # usage
    echo "Usage: $0 start|stop"
    exit 1
    ;;

esac
