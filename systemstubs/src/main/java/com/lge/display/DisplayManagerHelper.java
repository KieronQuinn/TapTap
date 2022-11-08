package com.lge.display;

import android.content.Context;

public class DisplayManagerHelper {

  public static final int COVER_TYPE_DISPLAY_COVER = 15;
  
  public static final int COVER_TYPE_NONE = 5;
  
  public static final int STATE_COVER_CLOSED = 1;
  
  public static final int STATE_COVER_FLIPPED_OVER = 5;
  
  public static final int STATE_COVER_OPENED = 0;
  
  public static final int STATE_DISABLED = 2;
  
  public static final int STATE_ENABLED = 3;
  
  public static final int STATE_UNMOUNT = 1;
  
  public static abstract class CoverDisplayCallback {
    public CoverDisplayCallback() {
      throw new RuntimeException("Stub!");
    }
    
    public void onCoverDisplayEnabledChangedCallback(int state) {
      throw new RuntimeException("Stub!");
    }
  }
  
  public static abstract class SmartCoverCallback {
    public SmartCoverCallback() {
      throw new RuntimeException("Stub!");
    }
    
    public void onTypeChanged(int type) {
      throw new RuntimeException("Stub!");
    }
    
    public void onStateChanged(int state) {
      throw new RuntimeException("Stub!");
    }
  }
  
  public DisplayManagerHelper(Context context) {
    throw new RuntimeException("Stub!");
  }
  
  public int getCoverDisplayState() {
    throw new RuntimeException("Stub!");
  }
  
  public void registerCoverDisplayEnabledCallback(String key, CoverDisplayCallback clbk) {
    throw new RuntimeException("Stub!");
  }
  
  public void unregisterCoverDisplayEnabledCallback(String key) {
    throw new RuntimeException("Stub!");
  }
  
  public void registerSmartCoverCallback(SmartCoverCallback clbk) {
    throw new RuntimeException("Stub!");
  }
  
  public void unregisterSmartCoverCallback(SmartCoverCallback clbk) {
    throw new RuntimeException("Stub!");
  }
  
  public int getCoverState() {
    throw new RuntimeException("Stub!");
  }
  
  public int getCoverType() {
    throw new RuntimeException("Stub!");
  }
  
  public int getCoverDisplayId() {
    throw new RuntimeException("Stub!");
  }

}
