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
   private static final Logger log = LogManager.getLogger();

   //~--- fields --------------------------------------------------------------

   private final AtomicBoolean         toolkitRunning = new AtomicBoolean(false);
   LinkedBlockingQueue<Runnable> tasks          = new LinkedBlockingQueue<>();
   private final Map<Object, Object>   contextMap     = Collections.synchronizedMap(new HashMap<>());

   //~--- methods -------------------------------------------------------------

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

   @Override
   public Future<?> addRenderJob(RenderJob rj) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
public boolean canStartNestedEventLoop() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   @Override
   public void closeAppletWindow() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public HitInfo convertHitInfoToFX(Object hit) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public PathElement[] convertShapeToFXPath(Object shape) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public AppletWindow createAppletWindow(long parent, String serverName) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public PerformanceTracker createPerformanceTracker() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public PlatformImage createPlatformImage(int w, int h) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Path2D createSVGPath2D(SVGPath svgpath) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Object createSVGPathObject(SVGPath svgpath) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

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

   @Override
   public TKStage createTKEmbeddedStage(HostInterface host, AccessControlContext acc) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public TKStage createTKPopupStage(Window peerWindow,
                                     StageStyle popupStyle,
                                     TKStage owner,
                                     AccessControlContext acc) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

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

   @Override
   public void defer(Runnable runnable) {
      this.tasks.add(runnable);
   }

   @Override
   public void enableDrop(TKScene s, TKDropTargetListener l) {
      throw new UnsupportedOperationException("Not supported yet.");
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
   public boolean imageContains(Object image, float x, float y) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean init() {
      return true;
   }

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

         FortifyFun.fixAccessible(f);  // f.setAccessible(true);

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
   public ImageLoader loadImage(InputStream stream, int width, int height, boolean preserveRatio, boolean smooth) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public ImageLoader loadImage(String url, int width, int height, boolean preserveRatio, boolean smooth) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public AsyncOperation loadImageAsync(AsyncOperationListener<? extends ImageLoader> listener,
         String url,
         int width,
         int height,
         boolean preserveRatio,
         boolean smooth) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public ImageLoader loadPlatformImage(Object platformImage) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void registerDragGestureListener(TKScene s, Set<TransferMode> tms, TKDragGestureListener l) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Object renderToImage(ImageRenderingContext context) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public void requestNextPulse() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public File showDirectoryChooser(TKStage ownerWindow, String title, File initialDirectory) {
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
   public void startDrag(TKScene scene, Set<TransferMode> tms, TKDragSourceListener l, Dragboard dragboard) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

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

   @Override
   public Filterable toFilterable(Image img) {
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
   protected Object createImagePatternPaint(ImagePattern paint) {
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

   //~--- set methods ---------------------------------------------------------

   @Override
   public void setAnimationRunnable(DelayedRunnable animationRunnable) {
      if (animationRunnable != null) {
         this.tasks.add(animationRunnable);
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public boolean isBackwardTraversalKey(KeyEvent e) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Dimension2D getBestCursorSize(int preferredWidth, int preferredHeight) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Map<Object, Object> getContextMap() {
      return this.contextMap;
   }

   @Override
   public FilterContext getFilterContext(Object config) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public FontLoader getFontLoader() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean isForwardTraversalKey(KeyEvent e) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public int getKeyCodeForChar(String character) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public AbstractMasterTimer getMasterTimer() {
      return MasterTimer.getInstance();
   }

   @Override
   public int getMaximumCursorColors() {
      throw new UnsupportedOperationException("Not supported yet.");
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
   public long getMultiClickTime() {
      return 0L;
   }

   @Override
   public TKClipboard getNamedClipboard(String name) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public boolean isNestedLoopRunning() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public PerformanceTracker getPerformanceTracker() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public KeyCode getPlatformShortcutKey() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public Object getPrimaryScreen() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public int getRefreshRate() {
      return 60;
   }

   @Override
public ScreenConfigurationAccessor getScreenConfigurationAccessor() {
      throw new UnsupportedOperationException(
          "Not supported yet.");  // To change body of generated methods, choose Tools | Templates.
   }

   //~--- set methods ---------------------------------------------------------

   @Override
   public ScreenConfigurationAccessor setScreenConfigurationListener(TKScreenConfigurationListener listener) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public List<?> getScreens() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public com.sun.javafx.tk.TKClipboard getSystemClipboard() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public TKSystemMenu getSystemMenu() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   @Override
   public TextLayoutFactory getTextLayoutFactory() {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}

