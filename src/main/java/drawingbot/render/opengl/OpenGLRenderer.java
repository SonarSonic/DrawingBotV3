package drawingbot.render.opengl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import drawingbot.DrawingBotV3;
import drawingbot.render.AbstractRenderer;
import javafx.beans.binding.Bindings;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.jfree.fx.FXGraphics2D;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class OpenGLRenderer extends AbstractRenderer implements GLEventListener {

    int width = 500;
    int height = 500;

    public ByteBuffer byteBuffer;

    public Image writableImage;
    public PixelBuffer<ByteBuffer> pixelBuffer;

    public GLOffscreenAutoDrawable drawable;
    public BufferedImage img = null;
    public Rectangle2D rectangle = new Rectangle2D(0, 0, width, height);


    public Canvas canvas;
    public GraphicsContext graphicsFX;
    public FXGraphics2D graphicsAWT;

    @Override
    public void init() {

        byteBuffer = Buffers.newDirectByteBuffer((width * height) * 4);

        PixelFormat<ByteBuffer> pixelFormat = PixelFormat.getByteBgraPreInstance();
        pixelBuffer = new PixelBuffer<>(width, height, byteBuffer, pixelFormat);

        writableImage = new WritableImage(pixelBuffer);

        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        caps.setHardwareAccelerated(true);
        caps.setOnscreen(false);

        // create the offscreen drawable
        GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);
        drawable = factory.createOffscreenAutoDrawable(null, caps,null, width, height);
        drawable.addGLEventListener(this);

        canvas = new Canvas(500, 500);
        graphicsFX = canvas.getGraphicsContext2D();
        graphicsAWT = new FXGraphics2D(canvas.getGraphicsContext2D());


        DrawingBotV3.INSTANCE.controller.viewportStackPane.getChildren().add(canvas);

        DrawingBotV3.INSTANCE.controller.viewportStackPane.minWidthProperty().bind(Bindings.createDoubleBinding(() -> canvas.getWidth() * Math.max(1, canvas.getScaleX()), canvas.widthProperty(), canvas.scaleXProperty()));
        DrawingBotV3.INSTANCE.controller.viewportStackPane.minHeightProperty().bind(Bindings.createDoubleBinding(() -> canvas.getHeight() * Math.max(1, canvas.getScaleY()), canvas.heightProperty(), canvas.scaleYProperty()));


    }

    @Override
    public void draw() {
        drawable.display();
        drawable.getContext().makeCurrent();

        pixelBuffer.updateBuffer(this::updateBuffer);
        graphicsFX.clearRect(0, 0, width, height);
        graphicsFX.drawImage(writableImage, 0, 0);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        DrawingBotV3.logger.info("OpenGL - Init");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        DrawingBotV3.logger.info("OpenGL - Dispose");

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        DrawingBotV3.logger.info("OpenGL - Display");

        GL2 gl = drawable.getGL().getGL2();

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glViewport(0, 0, width, height);

        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_BLEND );

        // use pixel coordinates
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();

        gl.glOrtho(0d, width, 0, height, -1d, 1d);
        gl.glColor4f(0f, 0f, 0f, 0f);
        gl.glRectd(0, 0, width, height);


        gl.glPointSize(4f);
        gl.glColor4f(0f, 1f, 0f, 1F);
        gl.glRectd(5,5,100,100);


        gl.glColor4f(0f, 0f, 1f, 0.5F);
        gl.glRectd(5,5,50,50);
    }


    public Rectangle2D updateBuffer(PixelBuffer<ByteBuffer> param) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glReadPixels(0, 0, width, height, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, byteBuffer);
        return rectangle;
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        DrawingBotV3.logger.info("OpenGL - Reshape");
    }
}
