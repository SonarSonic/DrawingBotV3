package drawingbot.pfm.wip;

import com.jhlabs.image.EdgeFilter;
import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.QuantizeFilter;
import drawingbot.api.IPlottingTask;
import drawingbot.geom.basic.GLine;
import drawingbot.image.filters.SplitEdgeFilter;
import drawingbot.pfm.AbstractPFM;
import drawingbot.utils.Utils;
import org.joml.Vector2d;

import java.awt.image.BufferedImage;

public class PFMSobelLines extends AbstractPFM {

    public float output_width = 40.0F;
    public int quantificationlevels = 4;
    //scale_factor = round(img.shape[0]/output_width)


    public float randomness_vertex = 0.01F; // 0 is no randomness, 0.01 is suitable.
    public float randomness_position = 1F;
    //public float min_pixel_size = 0.1F;
    //public float max_pixel_size = 0.9F;
    public float randomness_length = 0.1F;

    public BufferedImage imageSobelH;
    public BufferedImage imageSobelV;
    public static final float[] IDENTITY_MATRIX = new float[]{0,0,0,  0,1,0,  0,0,0 };


    //img = resize(img,(round(img.shape[0]/scale_factor), round(img.shape[1]/scale_factor)),anti_aliasing=True)
    //img_orig = img

    @Override
    public BufferedImage preFilter(BufferedImage image) {
        image = new GrayscaleFilter().filter(image, null);
        //img_dy = skimage.filters.sobel_h(img, mask=img < 255)
        //img_dx = skimage.filters.sobel_v(img, mask=img < 255)

        SplitEdgeFilter sobelFilter = new SplitEdgeFilter();
        sobelFilter.setEdgeMatrix(EdgeFilter.SOBEL_H);
        imageSobelH = sobelFilter.filter(image, null);
        sobelFilter.setEdgeMatrix(EdgeFilter.SOBEL_V);
        imageSobelV = sobelFilter.filter(image, null);

        //img = 1-img
        image = new InvertFilter().filter(image, null);

        ///img = np.round(np.multiply(img,quantificationlevels - 1)) + 1
        QuantizeFilter quantizeFilter = new QuantizeFilter();
        quantizeFilter.setNumColors(quantificationlevels);
        image = quantizeFilter.filter(image, null);

        return image;
    }

    @Override
    public void doProcess(IPlottingTask task) {
        for(int x = 0; x < task.getPixelData().getWidth(); x++){
            for(int y = 0; y < task.getPixelData().getHeight(); y++){
                int argb = Utils.mapInt((task.getPixelData().getARGB(x, y) >> 16) & 0xff, 0, 255 ,1, quantificationlevels);
                double hSobel = ((imageSobelH.getRGB(x, y)>>16)&0xff);
                double vSobel = ((imageSobelV.getRGB(x, y)>>16)&0xff);
                double alpha = Math.atan2(hSobel, vSobel);
                double grad_mag = (hSobel) + (vSobel);
                pixPlot(task, x, y, argb, alpha, grad_mag);
            }
        }
        task.finishProcess();
    }

    public void pixPlot(IPlottingTask task, int x, int y, int argb, double alpha, double grad_mag){

        for(int n = 0; n < argb; n++){
            Vector2d vertX = new Vector2d(-0.5, 0.5);
            Vector2d vertY = new Vector2d(0, 0);


            vertX.mul(Math.max(grad_mag*0.1, 1));
            vertX.mul(randomSeed(1-randomness_length, 1+randomness_length));


            vertY.add(randomSeed(-randomness_vertex, randomness_vertex), randomSeed(-randomness_vertex,randomness_vertex));


            // rotate line by gradient direction so that line is perpendicular to gradients
            Vector2d p1 = rotate_origin(vertX, alpha);
            Vector2d p2 = rotate_origin(vertY, alpha);


            vertX = new Vector2d(p1.x(), p2.x()).add(x, x).add(randomSeed(-randomness_position,+randomness_position), randomSeed(-randomness_position,+randomness_position));

            vertY = new Vector2d(p1.y(), p2.y()).add(y, y).add(randomSeed(-randomness_position,+randomness_position), randomSeed(-randomness_position,+randomness_position));

            task.addGeometry(new GLine((float)vertX.x(), (float)vertY.x(), (float)vertX.y(), (float)vertY.y()));

        }


    }



    public Vector2d rotate_origin(Vector2d xy, double radians){
        double rotateX = (xy.x() * Math.cos(radians)) + (xy.y() * Math.sin(radians));
        double rotateY = (-xy.x() * Math.sin(radians)) + (xy.y() * Math.cos(radians));
        return new Vector2d(rotateX, rotateY);
    }
}
/*

import numpy as np
from matplotlib import pyplot as plt
import skimage
from skimage import filters, data, io
from skimage.transform import resize
import random

#img = io.imread('file.jpg',as_gray=True)



def pixplot():
    # pixel_sizes = np.multiply(np.linspace(min_pixel_size,max_pixel_size,quantificationlevels),np.random.uniform(1-randomness_position,1+randomness_position,quantificationlevels))
    # pixel_sizes = pixel_sizes[0:val]
    # for n in pixel_sizes:

    # number of lines by the intensiy after quantization
    for n in range(val): #range(np.int(np.maximum(grad_mag*10,1)))

    # scale length by gradint magnitude
        vert_x = np.multiply(np.array([-0.5, 0.5]),np.maximum(grad_mag*0.1,1))
        vert_x = np.multiply(vert_x,np.random.uniform(1-randomness_length,1+randomness_length,1))
        vert_y = np.array([0, 0])

        vert_y = vert_y + np.random.uniform(-randomness_vertex,randomness_vertex,2)
        vert_x = vert_x + np.random.uniform(-randomness_vertex,randomness_vertex,2)
        # rotate line by gradient direction so that line is perpendicular to gradients
        p1 = rotate_origin([vert_x[0],vert_y[0]],alpha)
        p2 = rotate_origin([vert_x[1],vert_y[1]],alpha)


        vert_x = np.array([p1[0],p2[0]]) + c + np.random.uniform(-randomness_position,+randomness_position)
        vert_y = np.array([p1[1],p2[1]]) + r + np.random.uniform(-randomness_position,+randomness_position)


        plt.plot(vert_x,vert_y,'k-')


plt.imshow(img_orig,alpha = 0, cmap = 'gray')

height, width = img.shape
for c in range(width):
    for r in range(height):
        val = int(img[r,c])
        grad_direction = np.array([img_dx[r,c],img_dy[r,c]])
        alpha = np.arctan2(grad_direction[0],grad_direction[1])
        grad_mag = grad_direction[0]*grad_direction[0] + grad_direction[1]*grad_direction[1]
        pixplot()
plt.axis('off')
plt.box(False)
plt.savefig("file.svg", bbox_inches ="tight")
plt.show()
print("done")




 */
