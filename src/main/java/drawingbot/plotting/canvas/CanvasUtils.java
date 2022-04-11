package drawingbot.plotting.canvas;

import drawingbot.api.ICanvas;
import drawingbot.utils.UnitsLength;
import java.awt.geom.AffineTransform;

public class CanvasUtils {

    public static AffineTransform createCanvasScaleTransform(ICanvas canvas){
        AffineTransform transform = new AffineTransform();
        transform.scale(canvas.getPlottingScale(), canvas.getPlottingScale());
        return transform;
    }

    public static ICanvas retargetCanvas(ICanvas canvas, UnitsLength units){
        if(canvas.getUnits() == units){
            return new SimpleCanvas(canvas);
        }
        return new SimpleCanvas(units, canvas.getScalingMode(), canvas.optimiseForPrint(), canvas.useOriginalSizing(), canvas.getPlottingScale(), UnitsLength.convert(canvas.getWidth(), canvas.getUnits(), units), UnitsLength.convert(canvas.getHeight(), canvas.getUnits(), units), UnitsLength.convert(canvas.getDrawingWidth(), canvas.getUnits(), units), UnitsLength.convert(canvas.getDrawingHeight(), canvas.getUnits(), units), UnitsLength.convert(canvas.getDrawingOffsetX(), canvas.getUnits(), units), UnitsLength.convert(canvas.getDrawingOffsetY(), canvas.getUnits(), units), canvas.getCanvasScale());
    }

    public static SimpleCanvas rescaleCanvas(ICanvas canvas, float rescale){
        return new SimpleCanvas(canvas.getUnits(), canvas.getScalingMode(), canvas.optimiseForPrint(), canvas.useOriginalSizing(), 1F, canvas.getWidth()*rescale, canvas.getHeight()*rescale, canvas.getDrawingWidth()*rescale, canvas.getDrawingHeight()*rescale, canvas.getDrawingOffsetX()*rescale, canvas.getDrawingOffsetY()*rescale, rescale);
    }

    public static SimpleCanvas normalisedCanvas(ICanvas canvas){
        return new SimpleCanvas(UnitsLength.PIXELS, canvas.getScalingMode(), canvas.optimiseForPrint(), canvas.useOriginalSizing(), 1F, canvas.getScaledWidth(), canvas.getScaledHeight(), canvas.getScaledDrawingWidth(), canvas.getScaledDrawingHeight(), canvas.getScaledDrawingOffsetX(), canvas.getScaledDrawingOffsetY(), canvas.getCanvasScale());
    }

    /**
     * @return [cropped width, cropped height, cropped x, cropped y]
     */
    public static int[] getCroppedImageSize(ICanvas canvas, int sourceWidth, int sourceHeight){
        if(canvas.getDrawingWidth() == 0 || canvas.getDrawingHeight() == 0){
            return new int[]{sourceWidth, sourceHeight, 0, 0, sourceWidth, sourceHeight};
        }

        double currentRatio = (float) sourceWidth / sourceHeight;
        double targetRatio = canvas.getDrawingWidth() / canvas.getDrawingHeight();

        int imageCropWidth = sourceWidth;
        int imageCropHeight = sourceHeight;

        int imageCropX = 0;
        int imageCropY = 0;

        if(targetRatio != currentRatio){
            int targetWidth = (int)(sourceHeight * targetRatio);
            int targetHeight = (int)(sourceWidth / targetRatio);

            if (currentRatio < targetRatio) {
                imageCropY = (sourceHeight - targetHeight) / 2;
                imageCropHeight = targetHeight;
            }else{
                imageCropX = (sourceWidth - targetWidth) / 2;
                imageCropWidth = targetWidth;
            }
        }

        int finalWidth = (int)(canvas.getDrawingWidth(UnitsLength.PIXELS) * canvas.getPlottingScale());
        int finalHeight = (int)(canvas.getDrawingHeight(UnitsLength.PIXELS) * canvas.getPlottingScale());

        return new int[]{imageCropWidth, imageCropHeight, imageCropX, imageCropY, finalWidth, finalHeight};
    }

    public static float getExportWidth(ICanvas canvas, float DPI){
        float exportWidth;
        if(canvas.getUnits() == UnitsLength.PIXELS){
            exportWidth = canvas.getWidth(UnitsLength.PIXELS) * canvas.getPlottingScale();
        }else{
            exportWidth = canvas.getWidth(UnitsLength.INCHES) * DPI;
        }
        return exportWidth;
    }

    public static float getExportHeight(ICanvas canvas, float DPI){
        float exportHeight;
        if(canvas.getUnits() == UnitsLength.PIXELS){
            exportHeight = canvas.getHeight(UnitsLength.PIXELS) * canvas.getPlottingScale();
        }else{
            exportHeight = canvas.getHeight(UnitsLength.INCHES) * DPI;
        }
        return exportHeight;
    }

    public static int getRasterExportWidth(ICanvas canvas, float DPI, boolean isVideo){
        return getRasterExportWidth((int) getExportWidth(canvas, DPI), isVideo);
    }

    public static int getRasterExportWidth(int canvasWidth, boolean isVideo){
        if(isVideo && canvasWidth % 2 == 1){
            canvasWidth-=1;
        }
        return canvasWidth;
    }

    public static int getRasterExportHeight(ICanvas canvas, float DPI, boolean isVideo){
        return getRasterExportHeight((int) getExportHeight(canvas, DPI), isVideo);
    }

    public static int getRasterExportHeight(int canvasHeight, boolean isVideo){
        if(isVideo && canvasHeight % 2 == 1){
            canvasHeight-=1;
        }
        return canvasHeight;
    }

}