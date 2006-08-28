
# convert -stroke yellow -fill yellow -draw 'circle 220,189 223,192' kh-220-189.jpg kh-220-189-res.jpg
# usage example: drawloc.sh 220 189 223 192 kh-220-189.jpg kh-220-189-res.jpg

tmp=/tmp/gmap.jpg
convert -stroke white -fill red -draw "circle $1,$2 $3,$4" $5 $tmp
convert -resize 176x208 $tmp  $6
/bin/rm $tmp



