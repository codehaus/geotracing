package org.walkandplay.client.phone;

import org.walkandplay.client.external.CameraHandler;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.*;
import java.io.DataInputStream;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class TestDisplay extends Form implements CommandListener, ProgressListener {
    private Command BACK_CMD = new Command("Back", Command.BACK, 1);
    private Command START_CMD = new Command("Start", Command.OK, 1);
    private Command STOP_CMD = new Command("Stop", Command.OK, 1);
    protected WPMidlet midlet;
    private Display display;
    protected Displayable prevScreen;
    private Gauge gaugeInf = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
    private Gauge gauge = new Gauge(null, false, Gauge.INCREMENTAL_UPDATING, Gauge.CONTINUOUS_RUNNING);

    private Gauge progressBar = new Gauge("Download Progress", false, 100, 0);
    private int progressCounter;
    private int progressMax = 100;

    private int w = -1, h = -1;
    private Font f, fb;

    public TestDisplay(WPMidlet aMidlet) {
        //#style defaultscreen
        super("Test");
        midlet = aMidlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();
        display = Display.getDisplay(midlet);

        System.out.println("constructor TestDisplay");

        addCommand(BACK_CMD);
        //addCommand(START_CMD);

        /*//#style formbox
        append(gaugeInf);
        append(progressBar);
*/
        setCommandListener(this);

        camera();
        //append(new Gauge( null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING ));
        //append(new ClockItem(""));

    }

    public void camera() {
        try {
            CameraHandler.takeSnapshot(display);
        } catch (Exception e) {
            setTitle("Error:" + e.getMessage());
        }

        //Create a new thread here to get notified when the photo is taken
        new Thread() {
            public void run() {
                while (!CameraHandler.isFinished) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ie) {
                    }
                }
                //addPhoto();
                CameraHandler.end();
            }
        }.start();
    }

    public void prStart() {
        progressBar.setMaxValue(progressMax);
    }

    public void prProgress(int anAmount) {
        progressBar.setValue(progressCounter);
        if (progressCounter == progressMax - 1) {
            progressCounter = 0;
        }
        progressCounter++;
    }

    public void prStop() {
        progressBar.setLabel("Download finished!");
    }

    public void prError(String aMessage) {
        show(aMessage);
    }

    public void prSetContentLength(int aContentLength) {
        progressBar.setLabel("Downloading " + aContentLength + " bytes");
    }

    private void show(String aMsg) {
        System.out.println(aMsg);
    }

    private void download() {
        System.out.println("download");
        try {
            removeCommand(START_CMD);
            addCommand(STOP_CMD);

            Downloader dl = new Downloader();
            dl.download(this);
        } catch (Throwable t) {
            show("Exception in download:" + t.getMessage());
        }
    }


    private class Downloader {
        public int state = 0;

        private void download(ProgressListener aListener) {
            final ProgressListener listener = aListener;
            try {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            DataInputStream dis = null;
                            HttpConnection c = null;
                            String result = null;
                            try {
                                //image =  Util.getImage("http://farm2.static.flickr.com/1182/874505187_b12f8039bd_o_d.jpg");
                                c = (HttpConnection) Connector.open("http://test.walkandplay.com/command.txt");
                                dis = new DataInputStream(c.openInputStream());
                                listener.prStart();

                                // Read until the connection is closed.
                                StringBuffer b = new StringBuffer();
                                int ch;
                                while ((ch = dis.read()) != -1) {
                                    b.append((char) ch);
                                    listener.prProgress(-1);
                                }
                                result = b.toString();
                            } finally {
                                if (dis != null) {
                                    dis.close();
                                }
                                if (c != null) {
                                    c.close();
                                }
                                listener.prStop();
                            }
                        } catch (Throwable t) {
                            show(t.getMessage());
                            listener.prError(t.getMessage());
                        }
                    }
                }).start();
            } catch (Throwable t) {
                show("Exception in Downloader:" + t.getMessage());
            }
        }
    }


    public void commandAction(Command command, Displayable screen) {
        if (command == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (command == START_CMD) {
            download();
        } else if (command == STOP_CMD) {
            removeCommand(STOP_CMD);
            addCommand(START_CMD);
            prStop();
        }
    }

}
