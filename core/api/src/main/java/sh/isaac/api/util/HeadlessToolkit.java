/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.InputStream;

import java.lang.reflect.Field;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

//~--- non-JDK imports --------------------------------------------------------

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.glass.ui.GlassRobot;

//~--- JDK imports ------------------------------------------------------------

import com.sun.glass.ui.CommonDialogs.FileChooserResult;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.perf.PerformanceTracker;
import com.sun.javafx.runtime.VersionInfo;
import com.sun.javafx.runtime.async.AsyncOperation;
import com.sun.javafx.runtime.async.AsyncOperationListener;
//import com.sun.javafx.scene.text.HitInfo;
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

//~--- classes ----------------------------------------------------------------

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
public class HeadlessToolkit
        extends Toolkit {
   /** The Constant log. */
   private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   /** The toolkit running. */
   private final AtomicBoolean toolkitRunning = new AtomicBoolean(false);

   /** The tasks. */
   LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

   /** The context map. */
   private final Map<Object, Object> contextMap = Collections.synchronizedMap(new HashMap<>());

   //~--- methods -------------------------------------------------------------

   /**
    * Accumulate stroke bounds.
    *
    * @param shape the shape
    * @param bbox the bbox
    * @param type the type
    * @param strokewidth the strokewidth
    * @param cap the cap
    * @param join the join
    * @param miterLimit the miter limit
    * @param tx the tx
    */
   @Override
   public void accumulateStrokeBounds(Shape shape,
                                      float[] bbox,
                                      StrokeType type,
                                      double strokewidth,
                                      StrokeLineCap cap,
                                      StrokeLineJoin join,
                                      float miterLimit,
                                      BaseTransform tx) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Adds the render job.
    *
    * @param rj the rj
    * @return the future
    */
   @Override
   public Future<?> addRenderJob(RenderJob rj) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Can start nested event loop.
    *
    * @return true, if successful
    */
   @Override
   public boolean canStartNestedEventLoop() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   /**
    * Close applet window.
    */
   @Override
   public void closeAppletWindow() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

//   /**
//    * Convert hit info to FX.
//    *
//    * @param hit the hit
//    * @return the hit info
//    */
//   @Override
//   public HitInfo convertHitInfoToFX(Object hit) {
//      throw new UnsupportedOperationException("Not supported yet.");
//   }

   /**
    * Convert shape to FX path.
    *
    * @param shape the shape
    * @return the path element[]
    */
   @Override
   public PathElement[] convertShapeToFXPath(Object shape) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the applet window.
    *
    * @param parent the parent
    * @param serverName the server name
    * @return the applet window
    */
   @Override
   public AppletWindow createAppletWindow(long parent, String serverName) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the performance tracker.
    *
    * @return the performance tracker
    */
   @Override
   public PerformanceTracker createPerformanceTracker() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the platform image.
    *
    * @param w the w
    * @param h the h
    * @return the platform image
    */
   @Override
   public PlatformImage createPlatformImage(int w, int h) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the SVG path 2 D.
    *
    * @param svgpath the svgpath
    * @return the path 2 D
    */
   @Override
   public Path2D createSVGPath2D(SVGPath svgpath) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the SVG path object.
    *
    * @param svgpath the svgpath
    * @return the object
    */
   @Override
   public Object createSVGPathObject(SVGPath svgpath) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the stroked shape.
    *
    * @param shape the shape
    * @param pgtype the pgtype
    * @param strokewidth the strokewidth
    * @param pgcap the pgcap
    * @param pgjoin the pgjoin
    * @param miterLimit the miter limit
    * @param dashArray the dash array
    * @param dashOffset the dash offset
    * @return the shape
    */
   @Override
   public Shape createStrokedShape(Shape shape,
                                   StrokeType pgtype,
                                   double strokewidth,
                                   StrokeLineCap pgcap,
                                   StrokeLineJoin pgjoin,
                                   float miterLimit,
                                   float[] dashArray,
                                   float dashOffset) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the TK embedded stage.
    *
    * @param host the host
    * @param acc the acc
    * @return the TK stage
    */
   @Override
   public TKStage createTKEmbeddedStage(HostInterface host, AccessControlContext acc) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the TK popup stage.
    *
    * @param peerWindow the peer window
    * @param popupStyle the popup style
    * @param owner the owner
    * @param acc the acc
    * @return the TK stage
    */
   @Override
   public TKStage createTKPopupStage(Window peerWindow,
                                     StageStyle popupStyle,
                                     TKStage owner,
                                     AccessControlContext acc) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the TK stage.
    *
    * @param peerWindow the peer window
    * @param securityDialog the security dialog
    * @param stageStyle the stage style
    * @param primary the primary
    * @param modality the modality
    * @param owner the owner
    * @param rtl the rtl
    * @param acc the acc
    * @return the TK stage
    */
   @Override
   public TKStage createTKStage(Window peerWindow,
                                boolean securityDialog,
                                StageStyle stageStyle,
                                boolean primary,
                                Modality modality,
                                TKStage owner,
                                boolean rtl,
                                AccessControlContext acc) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Defer.
    *
    * @param runnable the runnable
    */
   @Override
   public void defer(Runnable runnable) {
      this.tasks.add(runnable);
   }

   /**
    * Enable drop.
    *
    * @param s the s
    * @param l the l
    */
   @Override
   public void enableDrop(TKScene s, TKDropTargetListener l) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Enter nested event loop.
    *
    * @param key the key
    * @return the object
    */
   @Override
   public Object enterNestedEventLoop(Object key) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Exit nested event loop.
    *
    * @param key the key
    * @param rval the rval
    */
   @Override
   public void exitNestedEventLoop(Object key, Object rval) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Image contains.
    *
    * @param image the image
    * @param x the x
    * @param y the y
    * @return true, if successful
    */
   @Override
   public boolean imageContains(Object image, float x, float y) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Inits the.
    *
    * @return true, if successful
    */
   @Override
   public boolean init() {
      return true;
   }

   /**
    * Install input method requests.
    *
    * @param scene the scene
    * @param requests the requests
    */
   @Override
   public void installInputMethodRequests(TKScene scene, InputMethodRequests requests) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * For running on a headless system only - hack this toolkit into place.
    */
   public static void installToolkit() {
      log.debug("installHeadlessToolkit begins");

      try {
         final Field f = Toolkit.class.getDeclaredField("TOOLKIT");

         f.setAccessible(true);

         final Object currentToolkit = f.get(null);

         if (currentToolkit == null) {
            log.debug("Installing the headless toolkit via reflection.");
            f.set(null, new HeadlessToolkit());
         } else if (currentToolkit.getClass()
                                  .getCanonicalName()
                                  .equals(HeadlessToolkit.class.getCanonicalName())) {
            // Just do nothing, if this code gets called twice
            log.debug("The headless toolkit already appears to be installed, doing nothing.");
            return;
         } else {
            throw new RuntimeException("A real Toolkit is already configured");
         }

         AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            // Get the javafx.version and javafx.runtime.version from a preconstructed
            // java class, VersionInfo, created at build time.
                  VersionInfo.setupSystemProperties();
                  return null;
               });
      } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
         throw new RuntimeException("Failed trying to hack JavaFX for Headless!", e);
      }
   }
   
   @Override
   public ImageLoader loadImage(String url, double width, double height, boolean preserveRatio, boolean smooth) {
       throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public ImageLoader loadImage(InputStream stream, double width, double height, boolean preserveRatio, boolean smooth) {
       throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Load platform image.
    *
    * @param platformImage the platform image
    * @return the image loader
    */
   @Override
   public ImageLoader loadPlatformImage(Object platformImage) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Register drag gesture listener.
    *
    * @param s the s
    * @param tms the tms
    * @param l the l
    */
   @Override
   public void registerDragGestureListener(TKScene s, Set<TransferMode> tms, TKDragGestureListener l) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Render to image.
    *
    * @param context the context
    * @return the object
    */
   @Override
   public Object renderToImage(ImageRenderingContext context) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Request next pulse.
    */
   @Override
   public void requestNextPulse() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Show directory chooser.
    *
    * @param ownerWindow the owner window
    * @param title the title
    * @param initialDirectory the initial directory
    * @return the file
    */
   @Override
   public File showDirectoryChooser(TKStage ownerWindow, String title, File initialDirectory) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Show file chooser.
    *
    * @param ownerWindow the owner window
    * @param title the title
    * @param initialDirectory the initial directory
    * @param initialFileName the initial file name
    * @param fileChooserType the file chooser type
    * @param extensionFilters the extension filters
    * @param selectedFilter the selected filter
    * @return the file chooser result
    */
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

   /**
    * Start drag.
    *
    * @param scene the scene
    * @param tms the tms
    * @param l the l
    * @param dragboard the dragboard
    */
   @Override
   public void startDrag(TKScene scene, Set<TransferMode> tms, TKDragSourceListener l, Dragboard dragboard) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Startup.
    *
    * @param runnable the runnable
    */
   @Override
   public void startup(Runnable runnable) {
      log.info("HeadlessTookit startup method called");

      if (!this.toolkitRunning.getAndSet(true)) {
         log.info("Starting a stand-in JavaFX Application Thread");

         final Thread t = new Thread(() -> {
                                        final Thread user = Thread.currentThread();

                                        user.setName("JavaFX Application Thread");

                                        // Set context class loader to the same as the thread that called startup
                                        user.setContextClassLoader(user.getContextClassLoader());
                                        setFxUserThread(user);

                                        while (true) {
                                           try {
                                              this.tasks.take()
                                                    .run();
                                           } catch (final Exception e) {
                                              // don't care
                                           }
                                        }
                                     });

         t.setDaemon(true);
         t.start();
      }

      this.tasks.add(runnable);
   }

   /**
    * Stroke contains.
    *
    * @param shape the shape
    * @param x the x
    * @param y the y
    * @param type the type
    * @param strokewidth the strokewidth
    * @param cap the cap
    * @param join the join
    * @param miterLimit the miter limit
    * @return true, if successful
    */
   @Override
   public boolean strokeContains(Shape shape,
                                 double x,
                                 double y,
                                 StrokeType type,
                                 double strokewidth,
                                 StrokeLineCap cap,
                                 StrokeLineJoin join,
                                 float miterLimit) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * To filterable.
    *
    * @param img the img
    * @return the filterable
    */
   @Override
   public Filterable toFilterable(Image img) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Wait for.
    *
    * @param t the t
    */
   @Override
   public void waitFor(Task t) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the color paint.
    *
    * @param paint the paint
    * @return the object
    */
   @Override
   protected Object createColorPaint(Color paint) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the image pattern paint.
    *
    * @param paint the paint
    * @return the object
    */
   @Override
   protected Object createImagePatternPaint(ImagePattern paint) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the linear gradient paint.
    *
    * @param paint the paint
    * @return the object
    */
   @Override
   protected Object createLinearGradientPaint(LinearGradient paint) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Creates the radial gradient paint.
    *
    * @param paint the paint
    * @return the object
    */
   @Override
   protected Object createRadialGradientPaint(RadialGradient paint) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Sets the animation runnable.
    *
    * @param animationRunnable the new animation runnable
    */
   @Override
   public void setAnimationRunnable(DelayedRunnable animationRunnable) {
      if (animationRunnable != null) {
         this.tasks.add(animationRunnable);
      }
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Checks if backward traversal key.
    *
    * @param e the e
    * @return true, if backward traversal key
    */
   @Override
   public boolean isBackwardTraversalKey(KeyEvent e) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the best cursor size.
    *
    * @param preferredWidth the preferred width
    * @param preferredHeight the preferred height
    * @return the best cursor size
    */
   @Override
   public Dimension2D getBestCursorSize(int preferredWidth, int preferredHeight) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the context map.
    *
    * @return the context map
    */
   @Override
   public Map<Object, Object> getContextMap() {
      return this.contextMap;
   }

   /**
    * Gets the filter context.
    *
    * @param config the config
    * @return the filter context
    */
   @Override
   public FilterContext getFilterContext(Object config) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the font loader.
    *
    * @return the font loader
    */
   @Override
   public FontLoader getFontLoader() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Checks if forward traversal key.
    *
    * @param e the e
    * @return true, if forward traversal key
    */
   @Override
   public boolean isForwardTraversalKey(KeyEvent e) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the key code for char.
    *
    * @param character the character
    * @return the key code for char
    */
   @Override
   public int getKeyCodeForChar(String character) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the master timer.
    *
    * @return the master timer
    */
   @Override
   public AbstractMasterTimer getMasterTimer() {
      return MasterTimer.getInstance();
   }

   /**
    * Gets the maximum cursor colors.
    *
    * @return the maximum cursor colors
    */
   @Override
   public int getMaximumCursorColors() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the multi click max X.
    *
    * @return the multi click max X
    */
   @Override
   public int getMultiClickMaxX() {
      return 0;
   }

   /**
    * Gets the multi click max Y.
    *
    * @return the multi click max Y
    */
   @Override
   public int getMultiClickMaxY() {
      return 0;
   }

   /**
    * Gets the multi click time.
    *
    * @return the multi click time
    */
   @Override
   public long getMultiClickTime() {
      return 0L;
   }

   /**
    * Gets the named clipboard.
    *
    * @param name the name
    * @return the named clipboard
    */
   @Override
   public TKClipboard getNamedClipboard(String name) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Checks if nested loop running.
    *
    * @return true, if nested loop running
    */
   @Override
   public boolean isNestedLoopRunning() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the performance tracker.
    *
    * @return the performance tracker
    */
   @Override
   public PerformanceTracker getPerformanceTracker() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the platform shortcut key.
    *
    * @return the platform shortcut key
    */
   @Override
   public KeyCode getPlatformShortcutKey() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the primary screen.
    *
    * @return the primary screen
    */
   @Override
   public Object getPrimaryScreen() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the refresh rate.
    *
    * @return the refresh rate
    */
   @Override
   public int getRefreshRate() {
      return 60;
   }

   /**
    * Gets the screen configuration accessor.
    *
    * @return the screen configuration accessor
    */
   @Override
   public ScreenConfigurationAccessor getScreenConfigurationAccessor() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   //~--- set methods ---------------------------------------------------------

   /**
    * Set screen configuration listener.
    *
    * @param listener the listener
    * @return the screen configuration accessor
    */
   @Override
   public ScreenConfigurationAccessor setScreenConfigurationListener(TKScreenConfigurationListener listener) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Gets the screens.
    *
    * @return the screens
    */
   @Override
   public List<?> getScreens() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the system clipboard.
    *
    * @return the system clipboard
    */
   @Override
   public com.sun.javafx.tk.TKClipboard getSystemClipboard() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the system menu.
    *
    * @return the system menu
    */
   @Override
   public TKSystemMenu getSystemMenu() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   /**
    * Gets the text layout factory.
    *
    * @return the text layout factory
    */
   @Override
   public TextLayoutFactory getTextLayoutFactory() {
      throw new UnsupportedOperationException("Not supported yet.");
   }
   
   @Override
   public GlassRobot createRobot() {
       throw new UnsupportedOperationException("not implemented");
   }
   
   @Override
   public AsyncOperation loadImageAsync(AsyncOperationListener<? extends ImageLoader> listener, String url, double width, double height, boolean preserveRatio, boolean smooth) {
       throw new UnsupportedOperationException("Not supported yet.");
   }
   
   @Override
   public void exitAllNestedEventLoops() {
       throw new UnsupportedOperationException("Not supported yet.");
   }

}

