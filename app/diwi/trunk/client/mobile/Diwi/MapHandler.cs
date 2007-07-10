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

        public static event CallbackHandler sDownloadCallback;

        static AppController.DownloadCallbackHandler sMapDnl = new AppController.DownloadCallbackHandler(mapReceived);
 
        static MapBounds sHorBounds = new MapBounds();
        static MapBounds sVerBounds = new MapBounds();

        static float sMapRadius = 0.1F;
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
                    new MediaDownloader(mapUrl, @"\horMap.jpg", sMapDnl);
                } else {
                    AppController.sEventLog.WriteLine("Dit not get MapURI");
                }
            } else {
                string mapUrl = AppController.sKwxClient.getBoundsMap(AppController.sActiveRouteID, sMapRadius, false);
                if (mapUrl != null) {
                    new MediaDownloader(mapUrl, @"\verMap.jpg", sMapDnl);
                } else {
                    AppController.sEventLog.WriteLine("Dit not get MapURI");
                }
            }
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
                        new MediaDownloader(mapUrl, @"\verMap.jpg", sMapDnl);
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
                        new MediaDownloader(mapUrl, @"\horMap.jpg", sMapDnl);
                    }
                    return;
                } 
            }

            if (sDoDownload > 0) {
                sDoDownload = 0;
                downLoadMaps();
            }
        }
    }
}
