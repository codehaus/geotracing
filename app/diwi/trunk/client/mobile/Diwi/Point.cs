using System;
using System.Collections.Generic;
using System.Text;

namespace Diwi {
    class GeoPoint {
        const double deg2rad = 3.14159/180.0;
        public float lat;
        public float lon;

        public GeoPoint(float la, float lo) {
            lat = la;
            lon = lo;
        }

        public float distance(GeoPoint p) {
            double avLat = (double)((p.lat + lat) / 2.0);
            double dLat = p.lat - lat;
            double dLon = ((p.lon - lon) * Math.Cos(deg2rad * avLat));
            return (float) (111330.0 * Math.Sqrt(dLat * dLat + dLon * dLon));
        }
    }
}
