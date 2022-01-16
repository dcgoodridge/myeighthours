import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


public class JNATests {

    private static final Logger LOG = LoggerFactory.getLogger(JNATests.class);


    public interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);

        public int GetTickCount();
    };

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

        class LASTINPUTINFO extends Structure {
            public int cbSize = 8;
            /// Tick count of when the last input event was received.
            public int dwTime;

            @Override
            protected List<String> getFieldOrder() {
                return Arrays.asList(new String[] { "cbSize", "dwTime" });
            }
        }

        public boolean GetLastInputInfo(LASTINPUTINFO result);

        /*
        SHORT WINAPI GetKeyState(
            _In_ int nVirtKey
            );
         */

        int GetKeyState(int virtKey);

    };

    public static int getIdleTimeMillisWin32() {
        User32.LASTINPUTINFO lastInputInfo = new User32.LASTINPUTINFO();
        User32.INSTANCE.GetLastInputInfo(lastInputInfo);
        return Kernel32.INSTANCE.GetTickCount() - lastInputInfo.dwTime;
    }
    enum State {
        UNKNOWN, ONLINE, IDLE, AWAY
    };

    @Test
    @Ignore
    public void sdfsdf() throws Exception {

        for (int i = 0; i < 10; i++) {
            int idleSec = getIdleTimeMillisWin32() / 1000;
            LOG.debug("idleSec=" + idleSec);
            Thread.sleep(500);
        }
    }


    @Test
    public void sdfsdasdasdf() throws Exception {

        User32.LASTINPUTINFO lastInputInfo = new User32.LASTINPUTINFO();
        int VK_CAPS_LOCK = java.awt.event.KeyEvent.VK_CAPS_LOCK;
        int keyState = User32.INSTANCE.GetKeyState(VK_CAPS_LOCK);
        boolean capsLockActive = (keyState & 0x0001)!=0;

        LOG.debug("keyState="+capsLockActive);
    }

}
