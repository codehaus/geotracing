/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.walkandplay.server.engine;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Log stuff to rotating dayfiles.
 *
 * @author Just van den Broecke
 * @version $Id: DayLogger.java,v 1.1.1.1 2006/04/03 09:21:35 rlenz Exp $
 */
public class DayLogger implements GameProtocol {
    private Log log;
    private String dirPath;
    private String rootFileName;
    private BufferedWriter fileWriter;
    private static final SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyMMdd");

    public DayLogger(String aDirPath, String aRootFileName, Log aLog) {
        dirPath = aDirPath;
        log = aLog;
        rootFileName = aRootFileName;
    }

    public void log(JXElement anElement) {
        log(anElement.toString());
    }

    public void log(String aString) {
        try {
            init();
            if (fileWriter == null) {
                return;
            }
            fileWriter.write(aString + "\n");
            fileWriter.flush();
        } catch (Throwable t) {
            log.warn("Error in DayLogger.log(), not logging", t);
            fileWriter = null;
        }
    }


    private void init() {
        String todayFilePath = dirPath + File.separator + rootFileName + fileNameFormat.format(new Date()) + ".txt";
        try {
            File todayFile = new File(todayFilePath);

            // Today's file does not exist
            if (!todayFile.exists()) {

                // Close old file if open
                if (fileWriter != null) {
                    try {
                        fileWriter.close();
                    } finally {
                        fileWriter = null;
                    }
                }
            }

            // Open current file path if not open
            if (fileWriter == null) {
                // Create or append to log file if none present/open
                File dataDirFile = new File(dirPath);

                if (!dataDirFile.exists()) {
                    dataDirFile.mkdirs();
                }

                fileWriter = new BufferedWriter(new FileWriter(todayFile, true));
            }

        } catch (Throwable t) {
            log.warn("Error in DayLogger.init(), file=" + todayFilePath, t);
            fileWriter = null;
        }
    }
}
