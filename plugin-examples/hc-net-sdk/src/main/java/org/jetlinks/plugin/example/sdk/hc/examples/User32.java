package org.jetlinks.plugin.example.sdk.hc.examples;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import org.jetlinks.plugin.example.sdk.hc.examples.GDI32.RECT;

/**
 * 输入描述.
 *
 * @author zhangji 2023/10/27
 */
public interface User32 extends W32API {
    User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class, DEFAULT_OPTIONS);
    int FLASHW_STOP = 0;
    int FLASHW_CAPTION = 1;
    int FLASHW_TRAY = 2;
    int FLASHW_ALL = 3;
    int FLASHW_TIMER = 4;
    int FLASHW_TIMERNOFG = 12;
    int IMAGE_BITMAP = 0;
    int IMAGE_ICON = 1;
    int IMAGE_CURSOR = 2;
    int IMAGE_ENHMETAFILE = 3;
    int LR_DEFAULTCOLOR = 0;
    int LR_MONOCHROME = 1;
    int LR_COLOR = 2;
    int LR_COPYRETURNORG = 4;
    int LR_COPYDELETEORG = 8;
    int LR_LOADFROMFILE = 16;
    int LR_LOADTRANSPARENT = 32;
    int LR_DEFAULTSIZE = 64;
    int LR_VGACOLOR = 128;
    int LR_LOADMAP3DCOLORS = 4096;
    int LR_CREATEDIBSECTION = 8192;
    int LR_COPYFROMRESOURCE = 16384;
    int LR_SHARED = 32768;
    int GWL_EXSTYLE = -20;
    int GWL_STYLE = -16;
    int GWL_WNDPROC = -4;
    int GWL_HINSTANCE = -6;
    int GWL_ID = -12;
    int GWL_USERDATA = -21;
    int DWL_DLGPROC = 4;
    int DWL_MSGRESULT = 0;
    int DWL_USER = 8;
    int WS_EX_COMPOSITED = 536870912;
    int WS_EX_LAYERED = 524288;
    int WS_EX_TRANSPARENT = 32;
    int LWA_COLORKEY = 1;
    int LWA_ALPHA = 2;
    int ULW_COLORKEY = 1;
    int ULW_ALPHA = 2;
    int ULW_OPAQUE = 4;
    int AC_SRC_OVER = 0;
    int AC_SRC_ALPHA = 1;
    int AC_SRC_NO_PREMULT_ALPHA = 1;
    int AC_SRC_NO_ALPHA = 2;
    int VK_SHIFT = 16;
    int VK_LSHIFT = 160;
    int VK_RSHIFT = 161;
    int VK_CONTROL = 17;
    int VK_LCONTROL = 162;
    int VK_RCONTROL = 163;
    int VK_MENU = 18;
    int VK_LMENU = 164;
    int VK_RMENU = 165;
    int WH_KEYBOARD = 2;
    int WH_MOUSE = 7;
    int WH_KEYBOARD_LL = 13;
    int WH_MOUSE_LL = 14;
    int WM_KEYDOWN = 256;
    int WM_KEYUP = 257;
    int WM_SYSKEYDOWN = 260;
    int WM_SYSKEYUP = 261;

    HDC GetDC(HWND var1);

    int ReleaseDC(HWND var1, HDC var2);

    HWND FindWindow(String var1, String var2);

    int GetClassName(HWND var1, byte[] var2, int var3);

    int GetClassName(HWND var1, char[] var2, int var3);

    boolean GetGUIThreadInfo(int var1, User32.GUITHREADINFO var2);

    boolean GetWindowInfo(HWND var1, User32.WINDOWINFO var2);

    boolean GetWindowRect(HWND var1, RECT var2);

    int GetWindowText(HWND var1, byte[] var2, int var3);

    int GetWindowText(HWND var1, char[] var2, int var3);

    int GetWindowTextLength(HWND var1);

    int GetWindowModuleFileName(HWND var1, byte[] var2, int var3);

    int GetWindowModuleFileName(HWND var1, char[] var2, int var3);

    int GetWindowThreadProcessId(HWND var1, IntByReference var2);

    boolean EnumWindows(User32.WNDENUMPROC var1, Pointer var2);

    boolean EnumChildWindows(HWND var1, User32.WNDENUMPROC var2, Pointer var3);

    boolean EnumThreadWindows(int var1, User32.WNDENUMPROC var2, Pointer var3);

    boolean FlashWindowEx(User32.FLASHWINFO var1);

    HICON LoadIcon(HINSTANCE var1, String var2);

    HANDLE LoadImage(HINSTANCE var1, String var2, int var3, int var4, int var5, int var6);

    boolean DestroyIcon(HICON var1);

    int GetWindowLong(HWND var1, int var2);

    int SetWindowLong(HWND var1, int var2, int var3);

    Pointer SetWindowLong(HWND var1, int var2, Pointer var3);

    LONG_PTR GetWindowLongPtr(HWND var1, int var2);

    LONG_PTR SetWindowLongPtr(HWND var1, int var2, LONG_PTR var3);

    Pointer SetWindowLongPtr(HWND var1, int var2, Pointer var3);

    boolean SetLayeredWindowAttributes(HWND var1, int var2, byte var3, int var4);

    boolean GetLayeredWindowAttributes(HWND var1, IntByReference var2, ByteByReference var3, IntByReference var4);

    boolean UpdateLayeredWindow(HWND var1, HDC var2, User32.POINT var3, User32.SIZE var4, HDC var5, User32.POINT var6, int var7, User32.BLENDFUNCTION var8, int var9);

    int SetWindowRgn(HWND var1, HRGN var2, boolean var3);

    boolean GetKeyboardState(byte[] var1);

    short GetAsyncKeyState(int var1);

    User32.HHOOK SetWindowsHookEx(int var1, User32.HOOKPROC var2, HINSTANCE var3, int var4);

    LRESULT CallNextHookEx(User32.HHOOK var1, int var2, WPARAM var3, LPARAM var4);

    LRESULT CallNextHookEx(User32.HHOOK var1, int var2, WPARAM var3, Pointer var4);

    boolean UnhookWindowsHookEx(User32.HHOOK var1);

    int GetMessage(User32.MSG var1, HWND var2, int var3, int var4);

    boolean PeekMessage(User32.MSG var1, HWND var2, int var3, int var4, int var5);

    boolean TranslateMessage(User32.MSG var1);

    LRESULT DispatchMessage(User32.MSG var1);

    void PostMessage(HWND var1, int var2, WPARAM var3, LPARAM var4);

    void PostQuitMessage(int var1);

    public static class MSG extends Structure {
        public HWND hWnd;
        public int message;
        public WPARAM wParam;
        public LPARAM lParam;
        public int time;
        public User32.POINT pt;

        public MSG() {
        }
    }

    public interface LowLevelKeyboardProc extends User32.HOOKPROC {
        LRESULT callback(int var1, WPARAM var2, User32.KBDLLHOOKSTRUCT var3);
    }

    public static class KBDLLHOOKSTRUCT extends Structure {
        public int vkCode;
        public int scanCode;
        public int flags;
        public int time;
        public ULONG_PTR dwExtraInfo;

        public KBDLLHOOKSTRUCT() {
        }
    }

    public interface HOOKPROC extends StdCallCallback {
    }

    public static class HHOOK extends HANDLE {
        public HHOOK() {
        }
    }

    public static class BLENDFUNCTION extends Structure {
        public byte BlendOp = 0;
        public byte BlendFlags = 0;
        public byte SourceConstantAlpha;
        public byte AlphaFormat;

        public BLENDFUNCTION() {
        }
    }

    public static class SIZE extends Structure {
        public int cx;
        public int cy;

        public SIZE() {
        }

        public SIZE(int w, int h) {
            this.cx = w;
            this.cy = h;
        }
    }

    public static class POINT extends Structure {
        public int x;
        public int y;

        public POINT() {
        }

        public POINT(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public interface WNDENUMPROC extends StdCallCallback {
        boolean callback(HWND var1, Pointer var2);
    }

    public static class WINDOWINFO extends Structure {
        public int cbSize = this.size();
        public RECT rcWindow;
        public RECT rcClient;
        public int dwStyle;
        public int dwExStyle;
        public int dwWindowStatus;
        public int cxWindowBorders;
        public int cyWindowBorders;
        public short atomWindowType;
        public short wCreatorVersion;

        public WINDOWINFO() {
        }
    }

    public static class GUITHREADINFO extends Structure {
        public int cbSize = this.size();
        public int flags;
        public HWND hwndActive;
        public HWND hwndFocus;
        public HWND hwndCapture;
        public HWND hwndMenuOwner;
        public HWND hwndMoveSize;
        public HWND hwndCaret;
        public RECT rcCaret;

        public GUITHREADINFO() {
        }
    }

    public static class FLASHWINFO extends Structure {
        public int cbSize;
        public HANDLE hWnd;
        public int dwFlags;
        public int uCount;
        public int dwTimeout;

        public FLASHWINFO() {
        }
    }
}

