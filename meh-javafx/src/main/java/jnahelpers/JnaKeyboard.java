package jnahelpers;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class JnaKeyboard {

    private static final Logger LOG = LoggerFactory.getLogger(JnaKeyboard.class);

    public interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);

        int GetTickCount();
    }

    ;

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

        class LASTINPUTINFO extends Structure {
            public int cbSize = 8;
            public int dwTime;

            @Override
            protected List<String> getFieldOrder() {
                return Arrays.asList(new String[]{"cbSize", "dwTime"});
            }
        }

        boolean GetLastInputInfo(LASTINPUTINFO result);

        int GetKeyState(int virtKey);
    }

    ;

    public static int getIdleTimeMillisWin32() {
        User32.LASTINPUTINFO lastInputInfo = new User32.LASTINPUTINFO();
        User32.INSTANCE.GetLastInputInfo(lastInputInfo);
        return Kernel32.INSTANCE.GetTickCount() - lastInputInfo.dwTime;
    }


    public static boolean isCapsLockActiveWin32() {

        int VK_CAPS_LOCK = java.awt.event.KeyEvent.VK_CAPS_LOCK;
        int keyState = User32.INSTANCE.GetKeyState(VK_CAPS_LOCK);
        boolean capsLockActive = (keyState & 0x0001) != 0;
        return capsLockActive;
    }


}
