/*
 * SortingFrame.java
 *
 * Created on Oct 23, 2011, 4:36:44 AM
 */

package in.mijosago.vuze.autopriority.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.activation.ActivationDataFlavor;
import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;

/**
 *
 * @author MJ
 */
public class SortingFrame extends javax.swing.JFrame {

    private class CustomListModel extends AbstractListModel<File> {

        private File[] files = {};

        @Override
        public int getSize() {
            return files.length;
        }

        @Override
        public File getElementAt(int index) {
            return files[index];
        }

        public File[] getFiles() {
            return files;
        }

        private void setFiles(File[] files) {
            int size = files.length;
            SortingFrame.this.jTFStartingPriority.setText(String.valueOf(size));
            this.files = files;
            this.fireContentsChanged(this, 0, size);
        }

        private void moveItems(List<File> data, int index) {

            int fLen = files.length;
            int dLen = data.size();
            File[] nFiles = new File[fLen];
            File[] mFiles = new File[dLen];
            data.toArray(mFiles);

            int nIdx = ((index + dLen) > fLen) ? index - dLen : index;
            System.arraycopy(mFiles, 0, nFiles, nIdx, dLen);

            Set<File> usedFiles = new HashSet<File>(data);
            for (int i = 0, j = 0; i < fLen; i++) {
                if (nFiles[i] == null) {
                    while (j < fLen) {
                        File file = this.files[j++];
                        if (!usedFiles.contains(file)) {
                            nFiles[i] = file;
                            break;
                        }
                    }
                }
            }

            setFiles(nFiles);

            int[] selections = new int[dLen];
            for (int i = 0; i < dLen; i++) {
                selections[i] = nIdx++;
            }

            jLList.setSelectedIndices(selections);
        }

    }

    private class CustomTransferHandler extends TransferHandler {

        private final JList<File> list;

        public CustomTransferHandler(JList<File> list) {
            this.list = list;
        }

        @Override
        public boolean canImport(TransferSupport ts) {
            if (!ts.isDataFlavorSupported(ADF)) {
                return false;
            }

            JList.DropLocation dl = (JList.DropLocation) ts.getDropLocation();
            if (dl.getIndex() == -1) {
                return false;
            }
            return true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean importData(TransferHandler.TransferSupport info) {
            if (!info.isDrop()) {
                return false;
            }

            try {
                Transferable t = info.getTransferable();
                JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
                List<File> data = (List<File>) t.getTransferData(ADF);
                int index = dl.getIndex();
                CustomListModel listModel = (CustomListModel) list.getModel();
                listModel.moveItems(data, index);
            } catch (Exception ex) { // ignore all exceptions :)
                return false;
            }

            return true;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            @SuppressWarnings("unchecked")
            JList<File> lst = (JList<File>) c;
            final List<File> values = lst.getSelectedValuesList();

            return new Transferable() {

                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return SUPPORTED_TYPES;
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return flavor.equals(ADF);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                    return values;
                }

            };
        }

    }

    /** Creates new form SortingFrame */
    public SortingFrame(File[] files, Map<File, DiskManagerFileInfo> filesMap) {
        if (!lnfSet) {
            lnfSet = true;
            try {
                for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                // Ignore exceptions
            }
        }

        this.filesMap = filesMap;
        initComponents();

        CustomListModel clm = (CustomListModel) this.jLList.getModel();
        clm.setFiles(files);
    }

    /** WARNING: Do NOT modify this code. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSPList = new javax.swing.JScrollPane();
        jLList = new javax.swing.JList();
        jPGroup = new javax.swing.JPanel();
        jLStartingPriority1 = new javax.swing.JLabel();
        jBOk = new javax.swing.JButton();
        jBCancel = new javax.swing.JButton();
        jTFStartingPriority = new javax.swing.JTextField();
        jBReverse = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLList.setModel(new CustomListModel());
        jLList.setDragEnabled(true);
        jLList.setDropMode(javax.swing.DropMode.INSERT);
        jLList.setTransferHandler(new CustomTransferHandler(jLList));
        jSPList.setViewportView(jLList);

        jPGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLStartingPriority1.setText("Starting Priority: ");

        jBOk.setText("Ok");
        jBOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBOkActionPerformed(evt);
            }
        });

        jBCancel.setText("Cancel");
        jBCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBCancelActionPerformed(evt);
            }
        });

        jBReverse.setText("Reverse");
        jBReverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBReverseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPGroupLayout = new javax.swing.GroupLayout(jPGroup);
        jPGroup.setLayout(jPGroupLayout);
        jPGroupLayout.setHorizontalGroup(
            jPGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLStartingPriority1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTFStartingPriority, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jBReverse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 206, Short.MAX_VALUE)
                .addComponent(jBOk)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBCancel)
                .addContainerGap())
        );
        jPGroupLayout.setVerticalGroup(
            jPGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPGroupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLStartingPriority1)
                    .addComponent(jTFStartingPriority, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jBOk)
                    .addComponent(jBCancel)
                    .addComponent(jBReverse))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSPList, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
                    .addComponent(jPGroup, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSPList, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jBCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCancelActionPerformed
        setCompleted(false);
    }//GEN-LAST:event_jBCancelActionPerformed

    private void jBOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBOkActionPerformed
        setCompleted(true);
    }//GEN-LAST:event_jBOkActionPerformed

    private void jBReverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBReverseActionPerformed
        CustomListModel model = (CustomListModel) this.jLList.getModel();
        File[] files = model.getFiles();
        int len = files.length;
        File[] nFiles = new File[len];
        for (int i = len - 1, j = 0; i > -1; i--, j++) {
            nFiles[j] = files[i];
        }

        model.setFiles(nFiles);

    }//GEN-LAST:event_jBReverseActionPerformed

    private void setCompleted(boolean ordered) {
        this.ordered = ordered;
        if (ordered) {
            String txt = jTFStartingPriority.getText();
            try {
                this.priority = Integer.parseInt(txt);
                prioritize(this.getFilesList());
                this.dispose();
            } catch (NumberFormatException ex) {
                String msg = "Invalid Priority";
                JOptionPane.showMessageDialog(this, msg, msg, JOptionPane.WARNING_MESSAGE);
            }
        } else {
            this.dispose();
        }
    }

    private void prioritize(File[] files) {
        int np = priority;
        for (int i = 0; i < files.length; i++) {
            File fk = files[i];
            DiskManagerFileInfo dmfi = this.filesMap.get(fk);
            if (np > 0) {
                dmfi.setNumericPriority(np--);
            } else {
                dmfi.setNumericPriority(0);
            }
        }
    }

    private File[] getFilesList() {
        @SuppressWarnings("unchecked")
        CustomListModel model = (CustomListModel) this.jLList.getModel();
        return model.getFiles();
    }

    public boolean isOrdered() {
        return ordered;
    }

    public int getPriority() {
        return priority;
    }

    private static final ActivationDataFlavor ADF = new ActivationDataFlavor(List.class,
            DataFlavor.javaJVMLocalObjectMimeType, "Downloadables");

    private static final DataFlavor[] SUPPORTED_TYPES = new DataFlavor[]{ADF};

    private int priority;

    private boolean ordered;

    private final Map<File, DiskManagerFileInfo> filesMap;

    private static boolean lnfSet = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBCancel;
    private javax.swing.JButton jBOk;
    private javax.swing.JButton jBReverse;
    private javax.swing.JList jLList;
    private javax.swing.JLabel jLStartingPriority1;
    private javax.swing.JPanel jPGroup;
    private javax.swing.JScrollPane jSPList;
    private javax.swing.JTextField jTFStartingPriority;
    // End of variables declaration//GEN-END:variables
}
