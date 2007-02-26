#!/bin/sh
#
# Restore single Oase context from backup file.
#
# usage: restore-context.sh <backup_file>
# <backup_file> must have been created with backup-context.sh
#
# Author: Just
# $Id$
#

# global vars
BACKUP_FILE=$2
SQL_FILE=context.pgd
KEYWORX_DEPLOY_DIR=/var/keyworx
CONTEXT_NAME=$1
CONTEXT_DATA_DIR=
export PGHOST=localhost
export PGUSER=oaseuser

function init() {

	if [ "x${BACKUP_FILE}" =  "x" ]
	then
		usage
	fi

	if [ "x${CONTEXT_NAME}" =  "x" ]
	then
		usage
	fi

	if [ ! -f ${BACKUP_FILE} ]
	then
		error "backup file ${BACKUP_FILE} does not exist"
	fi

	CONTEXT_DATA_DIR=${KEYWORX_DEPLOY_DIR}/data/oase/${CONTEXT_NAME}
	if [ ! -d ${CONTEXT_DATA_DIR} ]
	then
                mkdir ${CONTEXT_DATA_DIR}
#		error "data directory ${CONTEXT_DATA_DIR} for context ${CONTEXT_NAME} does not exist"
	fi

	echo "Directories and vars verified OK"
}


function postgisrestore() {
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
}

function restore() {
	# goto data dir
	if ! cd ${CONTEXT_DATA_DIR}
	then
		error "Cannot change dir to ${CONTEXT_DATA_DIR}"
	fi

	# clear old file cache/incoming stuff
	echo "Restoring files into ${CONTEXT_DATA_DIR}/files"
	emptyDir ${CONTEXT_DATA_DIR}/files
	emptyDir ${CONTEXT_DATA_DIR}/incoming
	emptyDir ${CONTEXT_DATA_DIR}/cache

	# extract backup file
	if ! tar xzvf ${BACKUP_FILE}
	then
		error "Cannot extract backup file in ${CONTEXT_DATA_DIR}"
	fi


	# restore DB
	echo "Restoring PostGIS data"
	if ! (postgisrestore ${CONTEXT_NAME} ${SQL_FILE})
	then
		error "cannot restore PostGIS DB for ${CONTEXT_NAME}"
	fi

	# go back
	cd -


	echo "OK context data restored for ${CONTEXT_NAME}"

}


function emptyDir() {
	if ! /bin/rm -rf $1/*
	then
		error "Cannot empty directory $1"
	fi
}

function error() {
	echo "ERROR: $1"
	exit -1
}

function usage() {
	echo "Usage: restore-context <context-name> <context data tar.gz created with context-backup.sh>"
	exit -1
}

# main
init
restore











