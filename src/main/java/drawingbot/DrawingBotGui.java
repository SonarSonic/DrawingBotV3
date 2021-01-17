package drawingbot;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DrawingBotGui extends JPanel {

    public ButtonGroup rbgPanel0;
    public JComboBox cmbDisplayMode;
    public JComboBox cmbPathFinding;
    public JProgressBar pbCurrentProgress;
    public JToggleButton tbtEnableGrid;

    public DrawingBotGui() {
        rbgPanel0 = new ButtonGroup();
        GridBagLayout gbPanel0 = new GridBagLayout();
        GridBagConstraints gbcPanel0 = new GridBagConstraints();
        setLayout( gbPanel0 );

        String []dataDisplayMode = { "Drawing", "Original", "Reference", "Lightened",
                "Pen" };
        cmbDisplayMode = new JComboBox( dataDisplayMode );
        gbcPanel0.gridx = 8;
        gbcPanel0.gridy = 4;
        gbcPanel0.gridwidth = 4;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 0;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( cmbDisplayMode, gbcPanel0 );
        add( cmbDisplayMode );

        String []dataPathFinding = { "Original", "Squares", "Spiral" };
        cmbPathFinding = new JComboBox( dataPathFinding );
        gbcPanel0.gridx = 2;
        gbcPanel0.gridy = 4;
        gbcPanel0.gridwidth = 1;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 0;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( cmbPathFinding, gbcPanel0 );
        add( cmbPathFinding );

        pbCurrentProgress = new JProgressBar( );
        gbcPanel0.gridx = 2;
        gbcPanel0.gridy = 28;
        gbcPanel0.gridwidth = 29;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 0;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( pbCurrentProgress, gbcPanel0 );
        add( pbCurrentProgress );

        tbtEnableGrid = new JToggleButton( "Grid"  );
        rbgPanel0.add( tbtEnableGrid );
        gbcPanel0.gridx = 3;
        gbcPanel0.gridy = 4;
        gbcPanel0.gridwidth = 5;
        gbcPanel0.gridheight = 1;
        gbcPanel0.fill = GridBagConstraints.BOTH;
        gbcPanel0.weightx = 1;
        gbcPanel0.weighty = 0;
        gbcPanel0.anchor = GridBagConstraints.NORTH;
        gbPanel0.setConstraints( tbtEnableGrid, gbcPanel0 );
        add( tbtEnableGrid );
    }

    //-Comment out main by GuiGenee, we set up ourselves:

  /*    public static void main (String[] args) {
   JFrame frame = new JFrame ("MyPanel");
   frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
   frame.getContentPane().add (new MyPanel());
   frame.pack();
   frame.setVisible (true);
   }
   */


    //**************************************************************
    //  This gets called when button is clicked
    //**************************************************************

    public class Button1Click implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            JButton b = (JButton)e.getSource();
            b.setLabel("Thanks!");

            DrawingBotV3.INSTANCE.timer=DrawingBotV3.INSTANCE.millis()+1000;

            DrawingBotV3.INSTANCE.println("Click");
            DrawingBotV3.INSTANCE.paper = DrawingBotV3.INSTANCE.color(DrawingBotV3.INSTANCE.random(255), DrawingBotV3.INSTANCE.random(255), DrawingBotV3.INSTANCE.random(255));


        }
    }

    //**************************************************************
    //  This gets called when slider is changed
    //**************************************************************

    public class HSlider1Change implements ChangeListener
    {
        public void stateChanged(ChangeEvent e)
        {
            JSlider source = (JSlider)e.getSource();
            DrawingBotV3.INSTANCE.ballYPos = (int)source.getValue();
        }
    }

    public class HSlider2Change implements ChangeListener{
        public void stateChanged(ChangeEvent e)
        {
            JSlider source = (JSlider)e.getSource();
            DrawingBotV3.INSTANCE.ballWidth = (int)source.getValue();

        }
    }

    public class HSlider3Change implements ChangeListener
    {
        public void stateChanged(ChangeEvent e)
        {
            JSlider source = (JSlider)e.getSource();
            DrawingBotV3.INSTANCE.ballHeight = (int)source.getValue();

        }
    }

}