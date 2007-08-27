DIR=keyworx@berlin:/var/keyworx/webapps/mlgk
scp ${DIR}/stable/index.php  dev.php
scp -r ${DIR}/mob ${DIR}/info ${DIR}/stable/media  ${DIR}/stable/script  ${DIR}/stable/style .



