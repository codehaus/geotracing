using System;
using System.Collections.Generic;
using System.Text;

namespace Diwi {


    class MapBounds {
        public float urLat, urLon;
        public float blLat, blLon;

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

        public float pix2lat(int y, int h) {
            int y_on_map = h - y;
            float a = ( urLat - blLat) * y_on_map / h + blLat;
            return a;
        }

        public float pix2lon(int x, int w) {
            float a = ( ( urLon - blLon) * x ) / w + blLon;
            return a;
        }

    }


    class MapHandler {
        public delegate void CallbackHandler();

        public static event CallbackHandler sDownloadCallback;

        static AppController.DownloadCallbackHandler sMapDnl = new AppController.DownloadCallbackHandler(mapReceived);

        static MapBounds sHorBounds = new MapBounds();
        static MapBounds sVerBounds = new MapBounds();

        static MapBounds sHorTempBounds = new MapBounds();
        static MapBounds sVerTempBounds = new MapBounds();

        static float sMapRadius = 0.25F;
        static bool sActive = false;
        static int sDownloadingVersion = 0;

        static int sDoDownload = 0;

        static public void downLoadMaps() {
            AppController.sActiveRouteMapPathHor = null;
            AppController.sActiveRouteMapPathVer = null;
            sDownloadingVersion = 0;

            if(DiwiPageBase.sCurrentPage.horizontal) {
                string mapUrl = AppController.sKwxClient.getBoundsMap(AppController.sActiveRouteID, sMapRadius, true);
                if (mapUrl != null) {
                    new MediaDownloader(mapUrl, AppController.sAppDir + @"\horMap.jpg", sMapDnl);
                } else {
                    AppController.sEventLog.WriteLine("Dit not get MapURI");
                }
            } else {
                string mapUrl = AppController.sKwxClient.getBoundsMap(AppController.sActiveRouteID, sMapRadius, false);
                if (mapUrl != null) {
                    new MediaDownloader(mapUrl, AppController.sAppDir + @"\verMap.jpg", sMapDnl);
                } else {
                    AppController.sEventLog.WriteLine("Dit not get MapURI");
                }
            }
        }

        public static void copyBounds() {
            sHorBounds.setBounds(sHorTempBounds.urLat, sHorTempBounds.urLon, sHorTempBounds.blLat, sHorTempBounds.blLon);
            sVerBounds.setBounds(sVerTempBounds.urLat, sVerTempBounds.urLon, sVerTempBounds.blLat, sVerTempBounds.blLon);
        }

        public static void mouseClick(int y, int x) {
            float lat, lon;
            if (DiwiPageBase.sCurrentPage.horizontal) {
                lon = sHorBounds.pix2lon(x,320);
                lat = sHorBounds.pix2lat(y,240);
            } else {
                lon = sVerBounds.pix2lon(x,240);
                lat = sVerBounds.pix2lat(y,320);
            }
            AppController.sGpsReader.insertLocation(lat, lon);
        }

        public static int currentXpixel(bool hor) {
            int x;
            if (hor) {
                x = sHorBounds.pixX(320);
                if (x < 50 || x > 250) {
                    downLoadMaps();
                }
            } else {
                x = sVerBounds.pixX(240);
                if (x < 50 || x > 170) {
                    downLoadMaps();
                }
            }
            return x;
        }

        public static int currentYpixel(bool hor) {
            int y;
            if (hor) {
                y = sHorBounds.pixY(240);
                if (y < 50 || y > 190) {
                    downLoadMaps();
                }
            } else {
                y = sVerBounds.pixY(320);
                if (y < 50 || y > 250) {
                    downLoadMaps();
                }
            }
            return y;
        }

        public static void setTempBounds(bool hor, float urLat, float urLon, float blLat, float blLon) {
            if (hor) {
                sHorTempBounds.setBounds(urLat, urLon, blLat, blLon);
            } else {
                sVerTempBounds.setBounds(urLat, urLon, blLat, blLon);
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

        static void mapReceived(string path, bool local) {
            int n = path.IndexOf("horMap");
            if (n >= 0) {
                AppController.sActiveRouteMapPathHor = path;
                if (sDownloadCallback != null) {
                    sDownloadCallback();
                }
                if (sDownloadingVersion == 0) {
                    sDownloadingVersion = 1;
                    string mapUrl = AppController.sKwxClient.getBoundsMap(AppController.sActiveRouteID, sMapRadius, false);
                    if (mapUrl != null) {
                        new MediaDownloader(mapUrl, AppController.sAppDir + @"\verMap.jpg", sMapDnl);
                    }
                    return;
                }
            } else {
                AppController.sActiveRouteMapPathVer = path;
                if (sDownloadCallback != null) {
                    sDownloadCallback();
                }
                if (sDownloadingVersion == 0) {
                    sDownloadingVersion = 1;
                    string mapUrl = AppController.sKwxClient.getBoundsMap(AppController.sActiveRouteID, sMapRadius, true);
                    if (mapUrl != null) {
                        new MediaDownloader(mapUrl, AppController.sAppDir + @"\horMap.jpg", sMapDnl);
                    }
                } 
            }

            if (sDoDownload > 0) sDoDownload--;

            if (sDoDownload > 0) {
                sDoDownload = 0;
                downLoadMaps();
            }
        }
    }
}
