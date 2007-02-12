#!/bin/bash


function error() {
	echo "Error: $1"
	exit -1;
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

	dropdb $1

    postgis_restore.pl ${LWPOSTGIS}  $1 $2 -E UTF8 -T postgis
    # createdb -E UTF8 -T postgis $1
    # psql $1 < $2
    # pg_restore -d $1 $2
}

DB=$1
DBFILE=$2

if [ "$1" == "" ] || [ "$2" == "" ]; then
	usage;
fi

restore $DB $DBFILE



