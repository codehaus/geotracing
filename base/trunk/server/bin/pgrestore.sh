#!/bin/bash

PGHOST="localhost"
PGUSER="oaseuser"
PGPASSWORD="oase"



function error() {
	echo "Error: $1"
	exit -1;
}

function warn() {
	echo "WARNING: $1"
}

function usage() {
	echo "Usage: pgrestore.sh  <dbname> <dbfile>"
	exit 0;
}

function restore() {
	echo "restoring $1 from $2"
	LWPOSTGIS="/usr/share/postgresql-8.1-postgis/lwpostgis.sql"
	if [ ! -f ${LWPOSTGIS} ]
	then
		LWPOSTGIS="/opt/local/share/postgis/lwpostgis.sql"
	fi

	if [ ! -f ${LWPOSTGIS} ]
	then
		error "cannot find lwpostgis.sql"
	fi

 	if ! (dropdb  $1)
   	then
    	warn "dropdb failed"
    fi

    postgis_restore.pl ${LWPOSTGIS}  $1 $2 -E UTF8 -T postgis
}

DB=$1
DBFILE=$2

if [ "$1" == "" ] || [ "$2" == "" ]; then
	usage;
fi

restore $DB $DBFILE



