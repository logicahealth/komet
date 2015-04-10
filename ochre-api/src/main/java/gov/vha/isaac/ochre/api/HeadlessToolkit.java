package gov.vha.isaac.ochre.api;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.CommonDialogs;
import com.sun.glass.ui.Screen;
import com.sun.glass.ui.Timer;
import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.logging.PulseLogger;
import com.sun.javafx.perf.PerformanceTracker;
import com.sun.javafx.runtime.VersionInfo;
import com.sun.javafx.runtime.async.AsyncOperation;
import com.sun.javafx.runtime.async.AsyncOperationListener;
import com.sun.javafx.scene.text.HitInfo;
import com.sun.javafx.scene.text.TextLayoutFactory;
import com.sun.javafx.tk.*;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.tk.quantum.MasterTimer;
import com.sun.scenario.DelayedRunnable;
import com.sun.scenario.animation.AbstractMasterTimer;
import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.Filterable;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.shape.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.sun.javafx.logging.PulseLogger.PULSE_LOGGING_ENABLED;

/**
 * Created by kec on 4/8/15.
 */
public class HeadlessToolkit extends Toolkit {
    private ClassLoader             ccl;
    private Runnable                userRunnable;
    private AtomicBoolean toolkitRunning = new AtomicBoolean(false);
    private Map<Object, Object> contextMap = new HashMap<>();
    private AtomicBoolean           animationRunning = new AtomicBoolean(false);
    private AtomicBoolean           nextPulseRequested = new AtomicBoolean(false);
    private AtomicBoolean           pulseRunning = new AtomicBoolean(false);
    private boolean                 inPulse = false;
    private Runnable                pulseRunnable, timerRunnable;
    private Timer pulseTimer = null;
    final int                       PULSE_INTERVAL = (int)(TimeUnit.SECONDS.toMillis(1L) / getRefreshRate());

    public static synchronized Toolkit setupToolkit() {
        HeadlessToolkit headlessToolkit = new HeadlessToolkit();
        try {
            Field field = com.sun.javafx.tk.Toolkit.class.getDeclaredField("TOOLKIT");
            field.setAccessible(true);
            field.set(null, headlessToolkit);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        final boolean verbose = AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> Boolean.getBoolean("javafx.verbose"));
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            // Get the javafx.version and javafx.runtime.version from a preconstructed
            // java class, VersionInfo, created at build time.
            VersionInfo.setupSystemProperties();
            return null;
        });


        try {
            if (headlessToolkit.init()) {
                    System.err.println("JavaFX: using " + headlessToolkit);
               return headlessToolkit;
            }
        } catch (Exception any) {
            any.printStackTrace();
        }

        throw new RuntimeException("No toolkit found");
    }

    @Override
    public boolean init() {
        Application.setDeviceDetails(null);
        return true;
    }
    @Override
    public AbstractMasterTimer getMasterTimer() {
        return MasterTimer.getInstance();
    }


    @Override
    public void startup(Runnable userStartupRunnable) {
        // Save the context class loader of the launcher thread
        ccl = Thread.currentThread().getContextClassLoader();

        try {
            this.userRunnable = userStartupRunnable;

            // Ensure that the toolkit can only be started here
            Application.run(() -> runToolkit());
        } catch (RuntimeException ex) {
             ex.printStackTrace();
            throw ex;
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }
    void runToolkit() {
        Thread user = Thread.currentThread();

        if (!toolkitRunning.getAndSet(true)) {
            user.setName("JavaFX Headless Application Thread");
            // Set context class loader to the same as the thread that called startup
            user.setContextClassLoader(ccl);
            setFxUserThread(user);
            pulseRunnable = () -> HeadlessToolkit.this.pulse();
            timerRunnable = () -> {
                try {
                    HeadlessToolkit.this.postPulse();
                } catch (Throwable th) {
                    th.printStackTrace(System.err);
                }
            };
            pulseTimer = Application.GetApplication().createTimer(timerRunnable);
            pulseTimer.start(PULSE_INTERVAL);
        }
        try {
            Application.invokeAndWait(this.userRunnable);

        } catch (Throwable th) {
            th.printStackTrace(System.err);
        }
    }

    void postPulse() {
        if (toolkitRunning.get() &&
                (animationRunning.get() || nextPulseRequested.get()) &&
                !setPulseRunning()) {

            Application.invokeLater(pulseRunnable);

        }
    }
    private boolean setPulseRunning() {
        return (pulseRunning.getAndSet(true));
    }

    @Override
    public Map<Object, Object> getContextMap() {
        return contextMap;
    }

    @Override
    public void defer(Runnable runnable) {
        if (!toolkitRunning.get()) return;
        Application.invokeLater(runnable);
    }
    private DelayedRunnable animationRunnable;
    @Override public void setAnimationRunnable(DelayedRunnable animationRunnable) {
        if (animationRunnable != null) {
            animationRunning.set(true);
        }
        this.animationRunnable = animationRunnable;
    }


    @Override
    public int getRefreshRate() {
        return 60;
    }

    protected void pulse() {
        pulse(true);
    }

    void pulse(boolean collect) {
        try {
            if (PULSE_LOGGING_ENABLED) {
                PulseLogger.pulseStart();
            }

            if (!toolkitRunning.get()) {
                return;
            }
            nextPulseRequested.set(false);
            inPulse = true;
            if (animationRunnable != null) {
                animationRunning.set(true);
                animationRunnable.run();
            } else {
                animationRunning.set(false);
            }
            firePulse();
        } finally {
            inPulse = false;
            endPulseRunning();
            if (PULSE_LOGGING_ENABLED) {
                PulseLogger.pulseEnd();
            }
        }
    }
    private void endPulseRunning() {
        pulseRunning.set(false);
    }
    @Override
    public void accumulateStrokeBounds(Shape shape, float[] bbox, StrokeType type, double strokewidth, StrokeLineCap cap, StrokeLineJoin join, float miterLimit, BaseTransform tx) {
        throw new HeadlessException("Using HeadlessToolkit");
    }


    @Override
    public Object enterNestedEventLoop(Object key) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public void exitNestedEventLoop(Object key, Object rval) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public boolean isNestedLoopRunning() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public TKStage createTKStage(Window peerWindow, boolean securityDialog, StageStyle stageStyle, boolean primary, Modality modality, TKStage owner, boolean rtl, AccessControlContext acc) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public TKStage createTKPopupStage(Window peerWindow, StageStyle popupStyle, TKStage owner, AccessControlContext acc) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public TKStage createTKEmbeddedStage(HostInterface host, AccessControlContext acc) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public AppletWindow createAppletWindow(long parent, String serverName) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public void closeAppletWindow() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public void requestNextPulse() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public Future addRenderJob(RenderJob rj) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public ImageLoader loadImage(String url, int width, int height, boolean preserveRatio, boolean smooth) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public ImageLoader loadImage(InputStream stream, int width, int height, boolean preserveRatio, boolean smooth) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public AsyncOperation loadImageAsync(AsyncOperationListener<? extends ImageLoader> listener, String url, int width, int height, boolean preserveRatio, boolean smooth) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public ImageLoader loadPlatformImage(Object platformImage) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public PlatformImage createPlatformImage(int w, int h) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public PerformanceTracker getPerformanceTracker() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public PerformanceTracker createPerformanceTracker() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public void waitFor(Task t) {
        throw new HeadlessException("Using HeadlessToolkit");

    }

    @Override
    protected Object createColorPaint(Color paint) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    protected Object createLinearGradientPaint(LinearGradient paint) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    protected Object createRadialGradientPaint(RadialGradient paint) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    protected Object createImagePatternPaint(ImagePattern paint) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public boolean strokeContains(Shape shape, double x, double y, StrokeType type, double strokewidth, StrokeLineCap cap, StrokeLineJoin join, float miterLimit) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public Shape createStrokedShape(Shape shape, StrokeType type, double strokewidth, StrokeLineCap cap, StrokeLineJoin join, float miterLimit, float[] dashArray, float dashOffset) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public int getKeyCodeForChar(String character) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public Dimension2D getBestCursorSize(int preferredWidth, int preferredHeight) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public int getMaximumCursorColors() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public PathElement[] convertShapeToFXPath(Object shape) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public HitInfo convertHitInfoToFX(Object hit) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public Filterable toFilterable(Image img) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public FilterContext getFilterContext(Object config) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public boolean isForwardTraversalKey(KeyEvent e) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public boolean isBackwardTraversalKey(KeyEvent e) {
        throw new HeadlessException("Using HeadlessToolkit");
    }


    @Override
    public FontLoader getFontLoader() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public TextLayoutFactory getTextLayoutFactory() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public Object createSVGPathObject(SVGPath svgpath) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public Path2D createSVGPath2D(SVGPath svgpath) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public boolean imageContains(Object image, float x, float y) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public TKClipboard getSystemClipboard() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public TKSystemMenu getSystemMenu() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public TKClipboard getNamedClipboard(String name) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public ScreenConfigurationAccessor setScreenConfigurationListener(TKScreenConfigurationListener listener) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public Object getPrimaryScreen() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public List<?> getScreens() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public void registerDragGestureListener(TKScene s, Set<TransferMode> tm, TKDragGestureListener l) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public void startDrag(TKScene scene, Set<TransferMode> tm, TKDragSourceListener l, Dragboard dragboard) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public void enableDrop(TKScene s, TKDropTargetListener l) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public void installInputMethodRequests(TKScene scene, InputMethodRequests requests) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public Object renderToImage(ImageRenderingContext context) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public CommonDialogs.FileChooserResult showFileChooser(TKStage ownerWindow, String title, File initialDirectory, String initialFileName, FileChooserType fileChooserType, List<FileChooser.ExtensionFilter> extensionFilters, FileChooser.ExtensionFilter selectedFilter) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public File showDirectoryChooser(TKStage ownerWindow, String title, File initialDirectory) {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public long getMultiClickTime() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public int getMultiClickMaxX() {
        throw new HeadlessException("Using HeadlessToolkit");
    }

    @Override
    public int getMultiClickMaxY() {
        throw new HeadlessException("Using HeadlessToolkit");
    }
}
