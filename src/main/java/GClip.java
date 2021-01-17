import java.awt.*;
import java.awt.datatransfer.*;
 
public class GClip implements ClipboardOwner {
 
  /**
   * Static reference to enforce singleton pattern
   */
  private static GClip gclip = null;
 
  /**
   * Class attribute to reference the programs clipboard
   */
  private Clipboard clipboard = null;
 
 
  /**
   * Copy a string to the clipboard
   * <a href="/two/profile/param">@param</a> chars
   */
  public static boolean copy(String chars) {
    if (gclip == null)
      gclip = new GClip();
    return gclip.copyString(chars);
  }
 
  /**
   * Get a string from the clipboard
   * <a href="/two/profile/return">@return</a> the string on the clipboard
   */
  public static String paste() {
    if (gclip == null)
      gclip = new GClip();
    return gclip.pasteString();
  }
 
  /**
   * Ctor is private so clipboard is only created when a copy or paste is 
   * attempted and one does not exist already.
   */
  private GClip() {
    if (clipboard == null) {
      makeClipboardObject();
    }
  }
 
  /**
   * If security permits use the system clipboard otherwise create 
   * our own application clipboard.
   */
  private void makeClipboardObject() {
    SecurityManager security = System.getSecurityManager();
    if (security != null) {
      try {
        security.checkSystemClipboardAccess();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      } 
      catch (SecurityException e) {
        clipboard = new Clipboard("Application Clipboard");
      }
    } else {
      try {
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      } 
      catch (Exception e) {
      }
    }
  }
 
  /**
   * Copy a string to the clipboard. If the Clipboard has not been created
   * then create it.
   * <a href="/two/profile/return">@return</a> true for a successful copy to clipboard
   */
  private boolean copyString(String chars) {
    if (clipboard == null)
      makeClipboardObject();
    if (clipboard != null) {
      StringSelection fieldContent = new StringSelection (chars);
      clipboard.setContents (fieldContent, this);
      return true;
    }
    return false;
  }
 
  /**
   * Gets a string from the clipboard. If there is no Clipboard
   * then create it.
   * <a href="/two/profile/return">@return</a> if possible the string on the clipboard else an empty string
   */
  private String pasteString() {
    // If there is no clipboard then there is nothing to paste
    if (clipboard == null) {
      makeClipboardObject();
      return "";
    }
    // We have a clipboard so get the string if we can
    Transferable clipboardContent = clipboard.getContents(this);
 
    if ((clipboardContent != null) &&
      (clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))) {
      try {
        String tempString;
        tempString = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
        return tempString;
      }
      catch (Exception e) {
        e.printStackTrace ();
      }
    }
    return "";
  }
 
  /**
   * Reqd by ClipboardOwner interface
   */
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
  }
}