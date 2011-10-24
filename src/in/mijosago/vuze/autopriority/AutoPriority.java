/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package in.mijosago.vuze.autopriority;

import in.mijosago.vuze.autopriority.ui.SortingFrame;
import org.gudy.azureus2.plugins.download.DownloadException;
import static org.gudy.azureus2.plugins.ui.tables.TableManager.*;
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
import org.gudy.azureus2.plugins.ui.menus.MenuItem;
import org.gudy.azureus2.plugins.ui.menus.MenuItemListener;
import org.gudy.azureus2.plugins.ui.tables.TableContextMenuItem;
import org.gudy.azureus2.plugins.ui.tables.TableManager;
import org.gudy.azureus2.plugins.ui.tables.TableRow;

/**
 *
 * @author MJ
 */
public class AutoPriority implements Plugin {

    private class MyMenuItemListener implements MenuItemListener {

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

    private static class FilesHandlerThread extends DoodleThread {

        private final TableRow[] trs;

        public FilesHandlerThread(TableRow[] trs) {
            this.trs = trs;
        }

        @Override
        public void run() {
            int len = trs.length;
            Map<File, DiskManagerFileInfo> filesM = new HashMap<File, DiskManagerFileInfo>(len);
            List<File> fnL = new ArrayList<File>(len);
            String name = "Files";
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
                callSortingFrame(name, fnL.toArray(new File[size]), filesM);
            }
        }

    }

    private static class TorrentHandlerThread extends DoodleThread {

        private final TableRow tr;

        public TorrentHandlerThread(TableRow tr) {
            this.tr = tr;
        }

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

    private static abstract class DoodleThread extends Thread {

        public void processFileInfo(DiskManagerFileInfo dmfi, List<File> fnL, Map<File, DiskManagerFileInfo> filesM) {
            if (!dmfi.isSkipped() && !dmfi.isDeleted()) {
                File f = dmfi.getFile();
                fnL.add(f);
                filesM.put(f, dmfi);
            }
        }

        public void callSortingFrame(String name, File[] fn, Map<File, DiskManagerFileInfo> filesM) {
            SortingFrame njf = new SortingFrame(fn, filesM);
            njf.setTitle(name);
            njf.setAlwaysOnTop(true);
            njf.setVisible(true);
        }

    }

    @Override
    public void initialize(PluginInterface pi) throws PluginException {
        TableManager tm = pi.getUIManager().getTableManager();

        TableContextMenuItem incl = tm.addContextMenuItem(TABLE_MYTORRENTS_INCOMPLETE, "auto.priority"); // Library - Adv View
        TableContextMenuItem ongoing = tm.addContextMenuItem(TABLE_MYTORRENTS_ALL_BIG, "auto.priority"); // Library - Simple View
        TableContextMenuItem c = tm.addContextMenuItem(TABLE_TORRENT_FILES, "auto.priority"); // On Individual Files
        TableContextMenuItem d = tm.addContextMenuItem(TABLE_MYTORRENTS_INCOMPLETE_BIG, "auto.priority"); // Downloading

        incl.addListener(new MyMenuItemListener());
        ongoing.addListener(new MyMenuItemListener());
        d.addListener(new MyMenuItemListener());
        c.addMultiListener(new MyMenuItemListener());
    }

}
