/*
 * SortingFrame.java
 * Created on Oct 23, 2011, 4:36:44 AM
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

package in.mijosago.vuze.autopriority.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.activation.ActivationDataFlavor;
import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;

/**
 * Displays the list of files to the user<br/>
 * @author Mithun Gonsalvez
 */
public class SortingFrame extends javax.swing.JFrame {

    /** List Model that contains the actual data */
    private class CustomListModel extends AbstractListModel {

        /** Files that are to be sorted */
        private File[] files;

        /** Size of the number of files being displayed */
        private int size;

        /** Reading order in the files list */
        private int[] readIdx;

        /** Whether the order being served is reversed */
        private boolean reverse; /* false by default */


        /** {@inheritDoc} */
        @Override
        public int getSize() {
            return this.size;
        }

        /** {@inheritDoc} */
        @Override
        public File getElementAt(int index) {
            int nIdx = reverse ? (this.size - index - 1) : index;
            return files[readIdx[nIdx]];
        }

        /**
         * Sets the files that is the data source for the list
         * @param files Files that act as the data for the list
         */
        private void setFiles(File[] files) {
            this.size = files.length;
            SortingFrame.this.jTFStartingPriority.setText(String.valueOf(this.size));
            this.files = files;
            int[] nReadIdx = new int[this.size];
            for (int i = 0; i < this.size; i++) {
                nReadIdx[i] = i;
            }
            this.setOrder(nReadIdx);
        }

        /**
         * Reverse the order being displayed
         */
        private void reverse() {
            this.reverse = !reverse;
            this.fireContentsChanged(this, 0, this.size);
        }

        /**
         * Sets the order in which the items in the files array should be read
         * @param readIdx reading order for the list of files
         */
        private void setOrder(int[] readIdx) {
            this.readIdx = readIdx;
            this.fireContentsChanged(this, 0, this.size);
        }

        /**
         * Moves the list items in the list
         * @param inIdx the items that are being moved
         * @param index New position in the list where the items have to be moved
         */
        private void moveItems(int[] inIdx, int index) {

            int dLen = inIdx.length;
            int nIdx = ((index + dLen) > this.size) ? index - dLen : index;
            int[] nReadIdx = new int[this.size];
            int[] nInIdx = new int[dLen];
            int[] selections = new int[dLen];

            for (int i = 0; i < dLen; i++) {
                nInIdx[i] = this.readIdx[inIdx[i]];
                selections[i] = nIdx + i;
            }

            System.arraycopy(nInIdx, 0, nReadIdx, nIdx, dLen);

            for (int i = 0, j = 0, k = 0; i < this.size; i++) {
                if (i == nIdx) {
                    i += dLen - 1;
                } else {
                    int nxtVal = this.readIdx[j++];
                    while (k < dLen && nInIdx[k] == nxtVal) {
                        nxtVal = this.readIdx[j++];
                        k++;
                    }
                    nReadIdx[i] = nxtVal;
                }
            }

            setOrder(nReadIdx);

            SortingFrame.this.jLList.setSelectedIndices(selections);
        }

    }

    /** Custom Transfer Handler */
    private class CustomTransferHandler extends TransferHandler {

        /** List that is backing up the data */
        private final JList list;

        public CustomTransferHandler(JList list) {
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
                int[] data = (int[]) t.getTransferData(ADF);
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
            final int[] selectedValues = ((JList) c).getSelectedIndices();

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
                    return selectedValues;
                }

            };
        }

    }

    /**
     * Creates new form SortingFrame
     * @param files Files to be sorted
     * @param filesMap Mapping between File and its DiskManagerFileInfo
     */
    public SortingFrame(File[] files, Map<File, DiskManagerFileInfo> filesMap) {
        this.filesMap = filesMap;
        initComponents();

        CustomListModel clm = (CustomListModel) this.jLList.getModel();
        clm.setFiles(files);

        // Bring up the UI in the centre of the screen
        setLocationRelativeTo(null);
    }

    /** WARNING: Do NOT modify this code. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JScrollPane jSPList = new javax.swing.JScrollPane();
        jLList = new javax.swing.JList();
        javax.swing.JPanel jPGroup = new javax.swing.JPanel();
        javax.swing.JLabel jLStartingPriority = new javax.swing.JLabel();
        javax.swing.JButton jBOk = new javax.swing.JButton();
        javax.swing.JButton jBCancel = new javax.swing.JButton();
        javax.swing.JButton jBReverse = new javax.swing.JButton();
        jTFStartingPriority = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLList.setModel(new CustomListModel());
        jLList.setDragEnabled(true);
        jLList.setDropMode(javax.swing.DropMode.INSERT);
        jLList.setTransferHandler(new CustomTransferHandler(jLList));
        jSPList.setViewportView(jLList);

        jPGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        jLStartingPriority.setText("Starting Priority: ");

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
                .addComponent(jLStartingPriority)
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
                    .addComponent(jLStartingPriority)
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

    /**
     * Method invoked when Cancel button is pressed
     * @param evt Event associated
     */
    private void jBCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBCancelActionPerformed
        setCompleted(false);
    }//GEN-LAST:event_jBCancelActionPerformed

    /**
     * Method invoked when Ok button is pressed
     * @param evt Event associated
     */
    private void jBOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBOkActionPerformed
        setCompleted(true);
    }//GEN-LAST:event_jBOkActionPerformed

    /**
     * Method invoked when the Reverse button is pressed
     * @param evt Event associated
     */
    private void jBReverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBReverseActionPerformed
        CustomListModel model = (CustomListModel) this.jLList.getModel();
        model.reverse();
    }//GEN-LAST:event_jBReverseActionPerformed

    /**
     * Completes the use of this class (if the priority is valid and if ok is pressed)
     * @param ordered 'true' if the user pressed 'OK'<br/>
     * 'false' if the user pressed 'Cancel'
     */
    private void setCompleted(boolean ordered) {
        if (ordered) {
            String txt = jTFStartingPriority.getText();
            try {
                int priority = Integer.parseInt(txt);
                prioritize(this.getFilesList(), priority);
                this.dispose();
            } catch (NumberFormatException ex) {
                String msg = "Invalid Priority";
                JOptionPane.showMessageDialog(this, msg, msg, JOptionPane.WARNING_MESSAGE);
            }
        } else {
            this.dispose();
        }
    }

    /**
     * Sets the priority for the files
     * @param files Files for which the priority should be set
     * @param priority Priority to be set
     * (this will be set to 0th file, priority minus 1 will be set to 1st file and so on)
     */
    private void prioritize(File[] files, int priority) {
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

    /**
     * Returns the list of files
     * @return Files that
     */
    private File[] getFilesList() {
        @SuppressWarnings("unchecked")
        CustomListModel model = (CustomListModel) this.jLList.getModel();
        int size = model.getSize();
        File[] files = new File[size];
        for (int i = 0; i < size; i++) {
            files[i] = model.getElementAt(i);
        }

        return files;
    }

    private static final ActivationDataFlavor ADF = new ActivationDataFlavor(int[].class,
            DataFlavor.javaJVMLocalObjectMimeType, "Downloadables");

    private static final DataFlavor[] SUPPORTED_TYPES = new DataFlavor[]{ADF};

    private final Map<File, DiskManagerFileInfo> filesMap;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jLList;
    private javax.swing.JTextField jTFStartingPriority;
    // End of variables declaration//GEN-END:variables
}
