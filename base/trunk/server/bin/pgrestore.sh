#!/bin/bash


function usage() {
	echo "Usage: pgrestore.sh  <dbname> <dbfile>"
	exit 0;
}

function restore() {
	echo "restoring $1 from $2"
	dropdb $1
    # ./postgis_restore.pl /opt/local/share/postgis/lwpostgis.sql  $1 $2 -E UTF8 -T postgis
    createdb -E UTF8 -T postgis $1
    # psql $1 < $2
    pg_restore -d $1 $2
}

DB=$1
DBFILE=$2

if [ "$1" == "" ] || [ "$2" == "" ]; then
	usage;
fi

restore $DB $DBFILE



