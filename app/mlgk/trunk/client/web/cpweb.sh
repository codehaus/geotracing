DIR=keyworx@berlin:/var/keyworx/webapps/walkandplay
scp ${DIR}/dev/index.php  dev.php
scp -r ${DIR}/mob ${DIR}/dev/media  ${DIR}/dev/script  ${DIR}/dev/style .



