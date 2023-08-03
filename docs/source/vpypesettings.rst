.. _vpype-settings:

============================
vpype Integration / Export
============================

DrawingBotV3 allows you to export directly to `vpype <https://vpype.readthedocs.io/en/stable/>`_ which gives you quick access to advanced layout tools, pre-visualisation, SVG optimisation, HPGL output and more.

You can even store *vpype* commands as :ref:`presets` to speed up your workflow.

Setup / Usage
^^^^^^^^^^^^^^^^

To export a drawing to *vpype* go to **File / Export to vpype**, you'll see a new window, from here you can select an inbuilt preset or enter your own command and then trigger use the **Send Command** button to send it to vpype.
Note: You must specify the :ref:`Path to vpype Executable <vpype-executable>` before you can send a command.

**Command:**

Any *vpype* command can be used, note however that there is an implicit read command added automatically so you don't need to use ``vpype read <FILE>``. So the **command** entered will be used in this format.

        vpype read "temp_vpype.svg" **command**

**Output Wildcard:**

You can replace the output file name in your command with the wildcard **%OUTPUT_FILE%**.

If this wildcard is added, when you press "Send Command" you'll be prompted to choose the name / directory to save the file. For example this would be a valid command, which would show you a save prompt.

        linesort write "**%OUTPUT_FILE%**.svg" show

**Bypass "Path Optimisation":**

Enabling this will bypass DrawingBotV3's :ref:`path-optimisation` and therefore use *vpype* commands exclusively to optimise your outputted drawing.
This option is command specific and is saved alongside a command preset.

.. _vpype-executable:

Finding the "Path to vpype Executable"
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

1) Open a terminal window and activate your virtual environment if you use one
2) Type ``where vpype`` (Windows) or ``which vpype`` (macOS and Linux)
3) Copy and paste the resulting path into the text field

You'll only need to do this once but if the vpype executable moves you will need to repeat this step.
