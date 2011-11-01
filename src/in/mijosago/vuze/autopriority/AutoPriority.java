/*
 * AutoPriority.java
 * Created on ‎Oct ‎23, ‎2011, ‏‎12:54:01 AM
 * Copyright (C) 2011 Mithun Gonsalvez
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package in.mijosago.vuze.autopriority;

import static org.gudy.azureus2.plugins.ui.tables.TableManager.*;

import in.mijosago.vuze.autopriority.ui.SortingFrame;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gudy.azureus2.plugins.Plugin;
import org.gudy.azureus2.plugins.PluginException;
import org.gudy.azureus2.plugins.PluginInterface;
import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;
import org.gudy.azureus2.plugins.download.Download;
import org.gudy.azureus2.plugins.download.DownloadException;
import org.gudy.azureus2.plugins.ui.menus.MenuItem;
import org.gudy.azureus2.plugins.ui.menus.MenuItemListener;
import org.gudy.azureus2.plugins.ui.tables.TableManager;
import org.gudy.azureus2.plugins.ui.tables.TableRow;

/**
 * Main Plugin Class
 * @author Mithun Gonsalvez
 */
public class AutoPriority implements Plugin {

    /** Menu Item Listener */
    private class DefaultMenuItemListener implements MenuItemListener {

        /** {@inheritDoc} */
        @Override
        public void selected(MenuItem mi, Object o) {

            if (o instanceof TableRow) {
                TorrentHandlerThread ct = new TorrentHandlerThread((TableRow) o);
                ct.setDaemon(true);
                ct.start();
            } else if (o instanceof TableRow[]) {
                FilesHandlerThread ft = new FilesHandlerThread((TableRow[]) o);
                ft.setDaemon(true);
                ft.start();
            }
        }

    };

    /** Thread that handles the case where the user clicks "Auto Priority" on File(s) */
    private static class FilesHandlerThread extends DoodleThread {

        /** Table Rows */
        private final TableRow[] trs;

        /**
         * Constructor
         * @param trs Table Rows
         */
        public FilesHandlerThread(TableRow[] trs) {
            this.trs = trs;
        }

        /** {@inheritDoc} */
        @Override
        public void run() {
            int len = trs.length;
            Map<File, DiskManagerFileInfo> filesM = new HashMap<File, DiskManagerFileInfo>(len);
            List<File> fnL = new ArrayList<File>(len);
            String name = null;
            for (int i = 0; i < len; i++) {
                TableRow tr = trs[i];
                Object ds = tr.getDataSource();
                if (ds instanceof DiskManagerFileInfo) {
                    DiskManagerFileInfo dmfi = (DiskManagerFileInfo) ds;
                    processFileInfo(dmfi, fnL, filesM);
                    try {
                        name = name == null ? dmfi.getDownload().getName() : "Files";
                    } catch (DownloadException ex) {
                        name = "Files";
                    }
                }
            }

            int size = fnL.size();
            if (size > 0) {
                name = name == null ? "Files" : name;
                callSortingFrame(name, fnL.toArray(new File[size]), filesM);
            }
        }

    }

    /** Thread that handles the case where the user clicks "Auto Priority" on a Torrent */
    private static class TorrentHandlerThread extends DoodleThread {

        /** Table Row */
        private final TableRow tr;

        /**
         * Constructor
         * @param tr Table Row
         */
        public TorrentHandlerThread(TableRow tr) {
            this.tr = tr;
        }

        /** {@inheritDoc} */
        @Override
        public void run() {
            Object ds = tr.getDataSource();
            if (ds instanceof Download) {
                Download d = (Download) ds;
                DiskManagerFileInfo[] dmfis = d.getDiskManagerFileInfo();
                int len = dmfis.length;
                Map<File, DiskManagerFileInfo> filesM = new HashMap<File, DiskManagerFileInfo>(len);
                List<File> fnL = new ArrayList<File>(len);
                for (int i = 0; i < dmfis.length; i++) {
                    processFileInfo(dmfis[i], fnL, filesM);
                }

                callSortingFrame(d.getName(), fnL.toArray(new File[fnL.size()]), filesM);
            }
        }

    }

    /** Extension of Thread class that provides some basic functions */
    private static abstract class DoodleThread extends Thread {

        /**
         * Each {@link DiskManagerFileInfo} object is checked for skip or delete option
         * If it is a file that is being downloaded, then it is considered for Prioritization
         * @param dmfi instance of {@link DiskManagerFileInfo} that is to be checked and added
         * @param fnL List where the valid Files are added
         * @param filesM Contains the mapping between the file and {@link DiskManagerFileInfo}
         */
        public void processFileInfo(DiskManagerFileInfo dmfi, List<File> fnL, Map<File, DiskManagerFileInfo> filesM) {
            if (!dmfi.isSkipped() && !dmfi.isDeleted()) {
                File f = dmfi.getFile();
                fnL.add(f);
                filesM.put(f, dmfi);
            }
        }

        /**
         * Initializes the {@link SortingFrame} and displays it
         * @param name Title of the Sorting Frame
         * @param fn Files Array that has to be displayed
         * @param filesM Contains the mapping between the file and {@link DiskManagerFileInfo}
         */
        public void callSortingFrame(String name, File[] fn, Map<File, DiskManagerFileInfo> filesM) {
            SortingFrame njf = new SortingFrame(fn, filesM);
            njf.setTitle(name);
            njf.setAlwaysOnTop(true);
            njf.setVisible(true);
        }

    }

    /** {@inheritDoc} */
    @Override
    public void initialize(PluginInterface pi) throws PluginException {
        TableManager tm = pi.getUIManager().getTableManager();
        DefaultMenuItemListener dml = new DefaultMenuItemListener();

        tm.addContextMenuItem(TABLE_MYTORRENTS_INCOMPLETE, "auto.priority").addListener(dml);     // Library - Adv View
        tm.addContextMenuItem(TABLE_MYTORRENTS_ALL_BIG, "auto.priority").addListener(dml);        // Library - Simple View
        tm.addContextMenuItem(TABLE_MYTORRENTS_INCOMPLETE_BIG, "auto.priority").addListener(dml); // Downloading View
        tm.addContextMenuItem(TABLE_TORRENT_FILES, "auto.priority").addMultiListener(dml);        // On Individual Files

    }

}
