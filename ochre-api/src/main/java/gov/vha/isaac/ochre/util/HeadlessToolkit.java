package gov.vha.isaac.ochre.util;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.AccessControlContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.geometry.Dimension2D;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import com.sun.glass.ui.CommonDialogs.FileChooserResult;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.perf.PerformanceTracker;
import com.sun.javafx.runtime.async.AsyncOperation;
import com.sun.javafx.runtime.async.AsyncOperationListener;
import com.sun.javafx.scene.text.HitInfo;
import com.sun.javafx.scene.text.TextLayoutFactory;
import com.sun.javafx.tk.AppletWindow;
import com.sun.javafx.tk.DummyToolkit;
import com.sun.javafx.tk.FileChooserType;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.ImageLoader;
import com.sun.javafx.tk.PlatformImage;
import com.sun.javafx.tk.RenderJob;
import com.sun.javafx.tk.ScreenConfigurationAccessor;
import com.sun.javafx.tk.TKClipboard;
import com.sun.javafx.tk.TKDragGestureListener;
import com.sun.javafx.tk.TKDragSourceListener;
import com.sun.javafx.tk.TKDropTargetListener;
import com.sun.javafx.tk.TKScene;
import com.sun.javafx.tk.TKScreenConfigurationListener;
import com.sun.javafx.tk.TKStage;
import com.sun.javafx.tk.TKSystemMenu;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.tk.quantum.MasterTimer;
import com.sun.scenario.DelayedRunnable;
import com.sun.scenario.animation.AbstractMasterTimer;
import com.sun.scenario.effect.FilterContext;
import com.sun.scenario.effect.Filterable;

/**
 * A terrible hack to attempt to keep basic parts of JavaFX usable (tasks, for example) on a headless environment.
 * 
 * To use, very early in the startup sequence, run something like this:
 * 
 * if (GraphicsEnvironment.isHeadless())
 * {
 *      HeadlessToolkit.installToolkit();
 * }
 * 
 * This class is mostly copied from {@link DummyToolkit} with parts copied from {@link com.sun.javafx.tk.quantum.QuantumToolkit}
 * 
 * @author darmbrust
 */

@SuppressWarnings("restriction")
public class HeadlessToolkit extends Toolkit
{
    private AtomicBoolean toolkitRunning = new AtomicBoolean(false);
    
    /**
     * For running on a headless system only - hack this toolkit into place.
     */
    public static void installToolkit()
    {
        try
        {
            Field f = Toolkit.class.getDeclaredField("TOOLKIT");
            f.setAccessible(true);
            if (f.get(null) == null)
            {
            	f.set(null, new HeadlessToolkit());
            }
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
        {
            System.err.println("Failed trying to hack JavaFX for Headless!");
            e.printStackTrace();
        }
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public Object enterNestedEventLoop(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exitNestedEventLoop(Object key, Object rval) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TKStage createTKStage(Window peerWindow, boolean securityDialog, StageStyle stageStyle, boolean primary, Modality modality, TKStage owner, boolean rtl, AccessControlContext acc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TKStage createTKPopupStage(Window peerWindow, StageStyle popupStyle, TKStage owner, AccessControlContext acc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TKStage createTKEmbeddedStage(HostInterface host, AccessControlContext acc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AppletWindow createAppletWindow(long parent, String serverName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void closeAppletWindow() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TKSystemMenu getSystemMenu() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageLoader loadImage(String url, int width, int height, boolean preserveRatio, boolean smooth) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageLoader loadImage(InputStream stream, int width, int height, boolean preserveRatio, boolean smooth) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AsyncOperation loadImageAsync(AsyncOperationListener<? extends ImageLoader> listener, String url, int width, int height, boolean preserveRatio, boolean smooth) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageLoader loadPlatformImage(Object platformImage) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PlatformImage createPlatformImage(int w, int h) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    
    @Override
    public void startup(Runnable runnable) {
        
        if (!toolkitRunning.getAndSet(true)) 
        {
            Thread t = new Thread(() -> 
            {
                Thread user = Thread.currentThread();
                user.setName("JavaFX Application Thread");
                // Set context class loader to the same as the thread that called startup
                user.setContextClassLoader(user.getContextClassLoader());
                setFxUserThread(user);
                while(true)
                {
                    try
                    {
                        tasks.take().run();
                    }
                    catch (Exception e)
                    {
                        //don't care
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        }
        tasks.add(runnable);
    }

    @Override
    public void defer(Runnable runnable) {
        tasks.add(runnable);
    }

    @Override
    public Future addRenderJob(RenderJob rj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Map<Object, Object> contextMap = Collections.synchronizedMap(new HashMap<>());
    @Override public Map<Object, Object> getContextMap() {
        return contextMap;
    }

    @Override
    public int getRefreshRate() {
        return 60;
    }

    @Override
    public void setAnimationRunnable(DelayedRunnable animationRunnable) {
        //don't care
    }

    @Override
    public PerformanceTracker getPerformanceTracker() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public PerformanceTracker createPerformanceTracker() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void waitFor(Task t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Object createColorPaint(Color paint) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Object createLinearGradientPaint(LinearGradient paint) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Object createRadialGradientPaint(RadialGradient paint) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Object createImagePatternPaint(ImagePattern paint) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void accumulateStrokeBounds(Shape shape, float[] bbox, StrokeType type, double strokewidth, StrokeLineCap cap, StrokeLineJoin join, float miterLimit, BaseTransform tx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean strokeContains(Shape shape, double x, double y, StrokeType type, double strokewidth, StrokeLineCap cap, StrokeLineJoin join, float miterLimit) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Shape createStrokedShape(Shape shape, StrokeType pgtype, double strokewidth, StrokeLineCap pgcap, StrokeLineJoin pgjoin, float miterLimit, float[] dashArray, float dashOffset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getKeyCodeForChar(String character) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Dimension2D getBestCursorSize(int preferredWidth, int preferredHeight) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMaximumCursorColors() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public PathElement[] convertShapeToFXPath(Object shape) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HitInfo convertHitInfoToFX(Object hit) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Filterable toFilterable(Image img) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FilterContext getFilterContext(Object config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isForwardTraversalKey(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isBackwardTraversalKey(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isNestedLoopRunning() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AbstractMasterTimer getMasterTimer() {
        return MasterTimer.getInstance();
    }

    @Override
    public FontLoader getFontLoader() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TextLayoutFactory getTextLayoutFactory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object createSVGPathObject(SVGPath svgpath) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Path2D createSVGPath2D(SVGPath svgpath) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean imageContains(Object image, float x, float y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public com.sun.javafx.tk.TKClipboard getSystemClipboard() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TKClipboard getNamedClipboard(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ScreenConfigurationAccessor setScreenConfigurationListener(TKScreenConfigurationListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getPrimaryScreen() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<?> getScreens() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void registerDragGestureListener(TKScene s, Set<TransferMode> tms, TKDragGestureListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startDrag(TKScene scene, Set<TransferMode> tms, TKDragSourceListener l, Dragboard dragboard) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void enableDrop(TKScene s, TKDropTargetListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void installInputMethodRequests(TKScene scene, InputMethodRequests requests) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object renderToImage(ImageRenderingContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KeyCode getPlatformShortcutKey() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public FileChooserResult showFileChooser(TKStage ownerWindow,
                                      String title,
                                      File initialDirectory,
                                      String initialFileName,
                                      FileChooserType fileChooserType,
                                      List<ExtensionFilter> extensionFilters,
                                      ExtensionFilter selectedFilter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public File showDirectoryChooser(TKStage ownerWindow,
                                     String title,
                                     File initialDirectory) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getMultiClickTime() {
    return 0L;
    }

    @Override
    public int getMultiClickMaxX() {
    return 0;
    }

    @Override
    public int getMultiClickMaxY() {
        return 0;
    }

    @Override
    public void requestNextPulse() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
