# draw location and resize
# convert -stroke yellow -fill yellow -draw 'circle 220,189 223,192' kh-220-189.jpg kh-220-189-res.jpg
# usage example: drawloc.sh 220 189 223 192 176x208 kh-220-189.jpg kh-220-189-res.jpg

tmp=$7.jpg
convert -stroke white -fill red -draw "circle $1,$2 $3,$4" $6 $tmp
convert -resize $5\! $tmp  $7
/bin/rm $tmp



