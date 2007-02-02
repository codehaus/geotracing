#!/bin/sh

TOP=
setDirs() {
        # find out the home dir of the project
        if [ -z "$PROJECT_HOME" ] ; then
                ## resolve links - $0 may be a link to  home
                PRG=$0
                progname=`basename $0`

                while [ -h "$PRG" ] ; do
                        ls=`ls -ld "$PRG"`
                        link=`expr "$ls" : '.*-> \(.*\)$'`
                        if expr "$link" : '.*/.*' > /dev/null; then
                                PRG="$link"
                        else
                                PRG="`dirname $PRG`/$link"
                        fi
                done

                PROJECT_HOME=`dirname "$PRG"`
        fi

        TOP=${PROJECT_HOME}/..
        TOP2=${PROJECT_HOME}/../build/war/WEB-INF

        echo "[OK] Using PROJECT_HOME dir $PROJECT_HOME "
        echo "[OK] Using TOP dir $TOP "
}

draw() {
  CP=${TOP}/lib/geotracing.jar:${TOP}/lib/keyworx.jar:${TOP2}/lib/geotracing.jar:${TOP2}/lib/keyworx.jar

 java -cp ${CP} org.geotracing.daemon.MapDrawer ${CFG_FILE}
}

draw_pja() {
PJAHOME=${TOP}/../external/pja-2.5
CP=${TOP}/build/lib/geotracing.jar:${PJAHOME}/lib/pjatools.jar

 java -Xbootclasspath/a:${PJAHOME}/lib/pja.jar \
  -Djava.awt.graphicsenv=com.eteks.java2d.PJAGraphicsEnvironment \
  -Dawt.toolkit=com.eteks.awt.PJAToolkit -Duser.home=${PJAHOME} \
  -cp ${CP} nl.justobjects.geotracing.map.MapDrawer ${CFG_FILE}
}

setDirs

CFG_FILE=$1

draw



