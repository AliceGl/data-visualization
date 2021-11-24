# Basics of programming course: data visualization, charts

### Usage

The name of the file with the data for building the chart
is passed to the program as the argument.

The data are presented in the form of a table (see examples).

When you start the program, two windows open - the first one will offer to
select the chart format. To select it, click on the button with the chart
name. After that the chart parameters will appear, and
in the second window the required chart will be drawn.

You can change the chart parameters by clicking on the 
corresponding buttons.

To save the chart and stop the program, you can click the "Save and quit" 
button. The chart will be saved in file "output.png".

### Examples

File "data.txt" contains:

<pre>
Months january february march april may june july august september october november december
Company1 15 26 42 59 27 16 12 31 34 30 35 49
Company2 50 38 20 47 30 25 16 42 34 41 17 18
Company3 13 30 20 56 34 27 84 62 83 49 31 43
</pre>

When you run the program with argument "data.txt", a window will open:

![image](./pictures/1.png)

When you click on the "Histogram" button in the second window
a chart will appear:

![image](./pictures/2.png)

Changing parameters:

![image](./pictures/3.png)

![image](./pictures/4.png)

When you click on the "Save and quit" button, the program ends
and saves the chart to a file.