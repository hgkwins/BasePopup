package razerdp.basepopup;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import razerdp.util.log.PopupLog;

/**
 * Created by 大灯泡 on 2020/9/9.
 * <p>
 * 这是一个BasePopup的后门，该后门允许你做出对BasePopup不安全的动作，请谨慎使用
 * <p>
 * 该类及其所有方法将会标记为过时，但那只是唬你的哈哈，只是想说尽可能不要用到它
 */
@SuppressLint("All")
@SuppressWarnings("all")
@Deprecated
public enum BasePopupUnsafe {
    INSTANCE;

    /**
     * 关闭所有的PopupWindow
     */
    @Deprecated
    public void dismissAllPopup(boolean animateDismiss) {
        HashMap<String, LinkedList<WindowManagerProxy>> allCache = WindowManagerProxy.PopupWindowQueueManager.sQueueMap;
        if (!allCache.isEmpty()) {
            for (LinkedList<WindowManagerProxy> value : allCache.values()) {
                for (WindowManagerProxy proxy : value) {
                    if (proxy.mPopupHelper != null && proxy.mPopupHelper.mPopupWindow != null) {
                        proxy.mPopupHelper.mPopupWindow.dismiss(animateDismiss);
                    }
                }
            }
        }
    }

    /**
     * 记录堆栈
     */
    @Deprecated
    @Nullable
    public StackDumpInfo dump(BasePopupWindow p) {
        if (p == null) return null;
        return StackFetcher.record(p);
    }

    /**
     * 获取用{@link #dump(BasePopupWindow)}记录的堆栈信息
     */
    @Deprecated
    @Nullable
    public StackDumpInfo getDump(BasePopupWindow p) {
        return StackFetcher.get(p);
    }

    //============================================================= helper
    static class StackFetcher {
        private static final Map<String, StackDumpInfo> STACK_MAP = new HashMap<>();

        private static StackTraceElement getCurrentStackTrace() {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            int stackOffset = PopupLog.getStackOffset(trace, BasePopupUnsafe.class);
            if (stackOffset == -1) {
                stackOffset = PopupLog.getStackOffset(trace, StackFetcher.class);
                if (stackOffset == -1) {
                    return null;
                }
            }
            return trace[stackOffset];
        }

        private static StackDumpInfo record(BasePopupWindow p) {
            return STACK_MAP.put(key(p), StackDumpInfo.obtain(getCurrentStackTrace()));
        }

        static void remove(BasePopupWindow p) {
            StackDumpInfo.CACHE = STACK_MAP.remove(key(p));
        }

        private static StackDumpInfo get(BasePopupWindow p) {
            String key = key(p);
            StackDumpInfo info = STACK_MAP.get(key(p));
            if (!TextUtils.isEmpty(key) && info != null) {
                String[] msg = key.split("@");
                if (msg.length == 2) {
                    info.popupClassName = msg[0];
                    info.popupAddress = msg[1];
                }
            }
            return info;
        }

        private static String key(BasePopupWindow p) {
            return String.valueOf(p);
        }
    }

    //============================================================= class
    public static class StackDumpInfo {
        static volatile StackDumpInfo CACHE = null;
        public String className;
        public String methodName;
        public String lineNum;
        public String popupClassName;
        public String popupAddress;

        public StackDumpInfo(StackTraceElement element) {
            record(element);
        }

        void record(StackTraceElement element) {
            if (element != null) {
                className = element.getFileName();
                methodName = element.getMethodName();
                lineNum = String.valueOf(element.getLineNumber());
            }
            popupClassName = null;
            popupAddress = null;
        }

        static StackDumpInfo obtain(StackTraceElement element) {
            if (CACHE != null) {
                CACHE.record(element);
                return CACHE;
            }
            return new StackDumpInfo(element);
        }

        @Override
        public String toString() {
            return "StackDumpInfo{" +
                    "className='" + className + '\'' +
                    ", methodName='" + methodName + '\'' +
                    ", lineNum='" + lineNum + '\'' +
                    ", popupClassName='" + popupClassName + '\'' +
                    ", popupAddress='" + popupAddress + '\'' +
                    '}';
        }
    }
}
