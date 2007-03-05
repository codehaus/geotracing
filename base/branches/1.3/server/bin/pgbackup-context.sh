#!/bin/sh
#
# Backup single Oase context into backup file (PostGIS-based only!!).
#
# usage: pgbackup-context.sh <context_name>
# <context_name> must a valid Oase context (=postgis db)
#
# Author: Just
# $Id$
#

# single arg
CONTEXT_NAME=$1

# global vars
KEYWORX_DEPLOY_DIR=/var/keyworx
KEYWORX_BACKUP_DIR=${KEYWORX_DEPLOY_DIR}/backup
KEYWORX_OASE_DIR=${KEYWORX_DEPLOY_DIR}/data/oase

PGHOST="localhost"
PGUSER="oaseuser"
PGPASSWORD="oase"

# to be calculated
BACKUP_FILE=
SQL_FILE=context.pgd
CONTEXT_DATA_DIR=

function init() {
    if [ "${CONTEXT_NAME}" =  "" ]
	then
		usage
	fi

	CONTEXT_DATA_DIR=${KEYWORX_OASE_DIR}/${CONTEXT_NAME}
	if [ ! -d ${CONTEXT_DATA_DIR} ]
	then
		error "data directory ${CONTEXT_DATA_DIR} for context ${CONTEXT_NAME} does not exist"
	fi

	if [ ! -d ${KEYWORX_BACKUP_DIR} ]
	then
		mkdir ${KEYWORX_BACKUP_DIR} > /dev/null 2>&1
	fi

	if [ ! -d ${KEYWORX_BACKUP_DIR} ]
	then
		error "backup directory ${KEYWORX_BACKUP_DIR} for context ${CONTEXT_NAME} does not exist or couold not be created"
	fi

	BACKUP_FILE=${KEYWORX_BACKUP_DIR}/context-${CONTEXT_NAME}-data-`date +%y%m%d-%H%M`.tar.gz

	echo "Directories and vars verified OK, backup file is ${BACKUP_FILE}"
}

function backup() {

	# backup DB
	if ! cd ${CONTEXT_DATA_DIR}
	then
		error "Cannot change dir to ${CONTEXT_DATA_DIR}"
	fi

	# dump PostGIS data
	echo "Dumping PostGIS data"
 	if ! (pg_dump -obv -Fc -O -f ${SQL_FILE} ${CONTEXT_NAME})
	then
		error "pg_dump failed"
	fi

	echo "Building ${BACKUP_FILE}"
	if ! (tar -cvzf ${BACKUP_FILE} files ${SQL_FILE})
	then
		error "error creating tar.gz archive ${BACKUP_FILE}"
	fi

	/bin/rm ${SQL_FILE} > /dev/null 2>&1
	cd -

	echo "context data for context ${CONTEXT_NAME} saved as ${BACKUP_FILE}"
}

function error() {
	echo "ERROR: $1"
	exit -1
}

function usage() {
	echo "Usage: backup-context <context name>"
	exit -1
}

# main
init
backup

