#!/bin/bash

BACKUP_DIR="/var/keyworx/backup"

PGHOST="localhost"
# PGUSER="postgres"

function pg_backup_database {
  DB=$1
  pg_dump -obv -Fc -f $BACKUP_DIR/$DB.pgd $DB
}

if [ -n "$1" ]; then
  pg_backup_database $1
else
  DB_LIST=`psql -l -t |/bin/cut -d'|' -f1 |/bin/sed -e 's/ //g'`
  for DB in $DB_LIST
  do
    if [ "$DB" != "template0" ] && [ "$DB" != "template1" ]; then
      pg_backup_database $DB
    fi
  done
fi
