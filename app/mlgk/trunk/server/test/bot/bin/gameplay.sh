#!/bin/sh
#
#
# do single gameplay
#
MEDIA=/var/keyworx/webapps/walkandplay/wptest/media
KWX=../lib/keyworx.jar

cp ${MEDIA}/save/* ${MEDIA}
java -cp $KWX  org.keyworx.amuse.test.protocol.Main $1
