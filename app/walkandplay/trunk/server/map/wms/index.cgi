#!/bin/sh
MAPSERV="/usr/lib/cgi-bin/mapserv"
MAPFILE="/var/keyworx/webapps/walkandplay/map/wp.map"
if [ "${REQUEST_METHOD}" = "GET" ]; then
  if [ -z "${QUERY_STRING}" ]; then
    QUERY_STRING="map=${MAPFILE}"
  else
    QUERY_STRING="map=${MAPFILE}&${QUERY_STRING}"
  fi
  exec ${MAPSERV}
else
  echo "Sorry, I only understand GET requests."
fi
exit 1
# End of Script
