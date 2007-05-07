using System;
using System.Collections.Generic;
using System.Text;

namespace Diwi {


    class MapBounds {
        float urLat, urLon;
        float blLat, blLon;

        public void setBounds(float ura, float uro, float bla, float blo) {
            urLat = ura;
            urLon = uro;
            blLat = bla;
            blLon = blo;
        }

        public int pixX(int scale) {
            float lon = GpsReader.lon;
            return (int)((float)scale * ((lon - blLon) / (urLon - blLon)));
        }

        public int pixY(int scale) {
            float lat = GpsReader.lat;
            return scale - (int)((float)scale * ((lat - blLat) / (urLat - blLat)));
        }

    }



    class MapHandler {
        public delegate void CallbackHandler();
        public delegate void DownloadCallbackHandler(string path);
        public static event CallbackHandler sDownloadCallback;
        static MapBounds sHorBounds = new MapBounds();
        static MapBounds sVerBounds = new MapBounds();

        static float sMapRadius = 0.5F;
        static bool sActive = false;

        static int sDoDownload = 0;

        static void downLoadMaps() {
            AppController.sActiveRouteMapPathHor = null;
            AppController.sActiveRouteMapPathVer = null;
            string mapUrl = AppController.sKwxClient.getBoundsMap(AppController.sActiveRouteID, sMapRadius, true);
            if (mapUrl != null) {
                new MediaDownloader(mapUrl, @"\horMap.jpg", new DownloadCallbackHandler(mapReceived));
            }
        }

        public static int currentXpixel(bool hor) {
            int x;
            if (hor) {
                x = sHorBounds.pixX(320);
                if (x < 30 || x > 270) {
                    downLoadMaps();
                }
            } else {
                x = sVerBounds.pixX(240);
                if (x < 30 || x > 190) {
                    downLoadMaps();
                }
            }
            return x;
        }

        public static int currentYpixel(bool hor) {
            int y;
            if (hor) {
                y = sHorBounds.pixY(240);
                if (y < 30 || y > 210) {
                    downLoadMaps();
                }
            } else {
                y = sVerBounds.pixY(320);
                if (y < 30 || y > 290) {
                    downLoadMaps();
                }
            }
            return y;
        }

        public static void setBounds(bool hor, float urLat, float urLon, float blLat, float blLon) {
            if (hor) {
                sHorBounds.setBounds(urLat, urLon, blLat, blLon);
            } else {
                sVerBounds.setBounds(urLat, urLon, blLat, blLon);
            }
        }

        public static float mapRadius {
            get { return sMapRadius; }
            set {
                sMapRadius = value;
                if (sActive) {
                    sDoDownload++;
                    if (sDoDownload == 1)
                        downLoadMaps();
                }
            }
        }

        public static bool active {
            get { return sActive; }
            set {
                sActive = value;
                if (sActive == true) {
                    sDoDownload++;
                    if( sDoDownload == 1 )
                        downLoadMaps();
                }
            }
        }

        static void mapReceived(string path) {
            if (AppController.sActiveRouteMapPathHor == null) {
                AppController.sActiveRouteMapPathHor = path;
                if (sDownloadCallback != null) {
                    sDownloadCallback();
                }
                string mapUrl = AppController.sKwxClient.getBoundsMap(AppController.sActiveRouteID, sMapRadius, false);
                if (mapUrl != null) {
                    new MediaDownloader(mapUrl, @"\verMap.jpg", new DownloadCallbackHandler(mapReceived));
                }
            } else {
                AppController.sActiveRouteMapPathVer = path;
                if (sDownloadCallback != null) {
                    sDownloadCallback();
                }
                sDoDownload--;
                if (sDoDownload > 0) {
                    sDoDownload = 1;
                    downLoadMaps();
                }
            }
        }
    }
}
