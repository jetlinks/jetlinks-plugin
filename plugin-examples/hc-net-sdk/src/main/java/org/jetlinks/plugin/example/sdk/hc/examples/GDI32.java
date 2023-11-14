package org.jetlinks.plugin.example.sdk.hc.examples;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

import java.awt.*;

/**
 * 输入描述.
 *
 * @author zhangji 2023/10/27
 */
public interface GDI32 extends W32API {
    GDI32 INSTANCE = (GDI32) Native.loadLibrary("gdi32", GDI32.class, DEFAULT_OPTIONS);
    int RDH_RECTANGLES = 1;
    int RGN_AND = 1;
    int RGN_OR = 2;
    int RGN_XOR = 3;
    int RGN_DIFF = 4;
    int RGN_COPY = 5;
    int ERROR = 0;
    int NULLREGION = 1;
    int SIMPLEREGION = 2;
    int COMPLEXREGION = 3;
    int ALTERNATE = 1;
    int WINDING = 2;
    int BI_RGB = 0;
    int BI_RLE8 = 1;
    int BI_RLE4 = 2;
    int BI_BITFIELDS = 3;
    int BI_JPEG = 4;
    int BI_PNG = 5;
    int DIB_RGB_COLORS = 0;
    int DIB_PAL_COLORS = 1;

    HRGN ExtCreateRegion(Pointer var1, int var2, GDI32.RGNDATA var3);

    int CombineRgn(HRGN var1, HRGN var2, HRGN var3, int var4);

    HRGN CreateRectRgn(int var1, int var2, int var3, int var4);

    HRGN CreateRoundRectRgn(int var1, int var2, int var3, int var4, int var5, int var6);

    HRGN CreatePolyPolygonRgn(User32.POINT[] var1, int[] var2, int var3, int var4);

    boolean SetRectRgn(HRGN var1, int var2, int var3, int var4, int var5);

    int SetPixel(HDC var1, int var2, int var3, int var4);

    HDC CreateCompatibleDC(HDC var1);

    boolean DeleteDC(HDC var1);

    HBITMAP CreateDIBitmap(HDC var1, GDI32.BITMAPINFOHEADER var2, int var3, Pointer var4, GDI32.BITMAPINFO var5, int var6);

    HBITMAP CreateDIBSection(HDC var1, GDI32.BITMAPINFO var2, int var3, PointerByReference var4, Pointer var5, int var6);

    HBITMAP CreateCompatibleBitmap(HDC var1, int var2, int var3);

    HANDLE SelectObject(HDC var1, HANDLE var2);

    boolean DeleteObject(HANDLE var1);

    public static class BITMAPINFO extends Structure {
        public GDI32.BITMAPINFOHEADER bmiHeader;
        int[] bmiColors;

        public BITMAPINFO() {
            this(1);
        }

        public BITMAPINFO(int size) {
            this.bmiHeader = new GDI32.BITMAPINFOHEADER();
            this.bmiColors = new int[1];
            this.bmiColors = new int[size];
            this.allocateMemory();
        }
    }

    public static class RGBQUAD extends Structure {
        public byte rgbBlue;
        public byte rgbGreen;
        public byte rgbRed;
        public byte rgbReserved = 0;

        public RGBQUAD() {
        }
    }

    public static class BITMAPINFOHEADER extends Structure {
        public int biSize = this.size();
        public int biWidth;
        public int biHeight;
        public short biPlanes;
        public short biBitCount;
        public int biCompression;
        public int biSizeImage;
        public int biXPelsPerMeter;
        public int biYPelsPerMeter;
        public int biClrUsed;
        public int biClrImportant;

        public BITMAPINFOHEADER() {
        }
    }

    public static class RGNDATA extends Structure {
        public GDI32.RGNDATAHEADER rdh;
        public byte[] Buffer;

        public RGNDATA(int bufferSize) {
            this.Buffer = new byte[bufferSize];
            this.allocateMemory();
        }
    }

    public static class RGNDATAHEADER extends Structure {
        public int dwSize = this.size();
        public int iType = 1;
        public int nCount;
        public int nRgnSize;
        public GDI32.RECT rcBound;

        public RGNDATAHEADER() {
        }
    }

    public static class RECT extends Structure {
        public int left;
        public int top;
        public int right;
        public int bottom;

        public RECT() {
        }

        public Rectangle toRectangle() {
            return new Rectangle(this.left, this.top, this.right - this.left, this.bottom - this.top);
        }

        public String toString() {
            return "[(" + this.left + "," + this.top + ")(" + this.right + "," + this.bottom + ")]";
        }
    }
}
