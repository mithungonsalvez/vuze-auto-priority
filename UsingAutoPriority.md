# Intro #

Numeric priority feature introduced in Vuze provides a way by which the files can be prioritized, some having higher priority than the other. Setting priority individually for each file is a bit of a painful process. Hence the introduction of the plugin Auto-Priority.


# Details #

This Plugin provides a context menu named 'Auto Priority' on
  * Torrent
  * Files in the Torrent (by selecting multiple files)

### Torrent ###
Clicking 'Auto Priority' on the torrent shows a UI containing the list of files present in the torrent.

### Files ###
Clicking 'Auto Priority' on the files shows a UI with only the selected files.

### UI ###
The UI has the following features:
  * Drag and Arrange the file(s) in the required sequence
  * Reverse the Order with a single Button Click
  * Input Field where the priority can be provided
  * OK Button to apply the order
  * Cancel Button to Cancel

Upon clicking OK, descending order of priority is applied for each 'Not-Deleted' and 'Not-Skipped' file.

**Example:**
First file in the list gets priority N. Second file gets priority N-1 and so on


**NOTE:** The order of files is not retained, hence every time Auto Priority is invoked, the files will have to be re-ordered.