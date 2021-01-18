package drawingbot.helpers;

import drawingbot.DrawingBotV3;
import drawingbot.tasks.PlottingTask;
import static processing.core.PApplet.*;

public class DrawingTools {


    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void adjustDistribution(PlottingTask task, int pen, double value){
        if(DrawingBotV3.pen_count > pen){
            task.pen_distribution[pen] *= value;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void set_even_distribution(PlottingTask task) {
        println("set_even_distribution");
        for (int p = 0; p< DrawingBotV3.pen_count; p++) {
            task.pen_distribution[p] = task.display_line_count / DrawingBotV3.pen_count;
            //println("pen_distribution[" + p + "] = " + pen_distribution[p]);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void set_black_distribution(PlottingTask task) {
        println("set_black_distribution");
        for (int p=0; p<DrawingBotV3.pen_count; p++) {
            task.pen_distribution[p] = 0;
            //println("pen_distribution[" + p + "] = " + pen_distribution[p]);
        }
        task.pen_distribution[0] = task.display_line_count;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void normalize_distribution(PlottingTask task) {
        float total = 0;

        println();
        //println("normalize_distribution");

        for (int p=0; p<DrawingBotV3.pen_count; p++) {
            total = total + task.pen_distribution[p];
        }

        for (int p = 0; p<DrawingBotV3.pen_count; p++) {
            task.pen_distribution[p] = task.display_line_count * task.pen_distribution[p] / total;
            print("Pen " + p + ", ");
            System.out.printf("%-4s", CopicPenHelper.copic_sets[DrawingBotV3.INSTANCE.current_copic_set][p]);
            System.out.printf("%8.0f  ", task.pen_distribution[p]);

            // Display approximately one star for every percent of total
            for (int s = 0; s<(int)(task.pen_distribution[p]/total*100); s++) {
                print("*");
            }
            println();
        }
    }

}
