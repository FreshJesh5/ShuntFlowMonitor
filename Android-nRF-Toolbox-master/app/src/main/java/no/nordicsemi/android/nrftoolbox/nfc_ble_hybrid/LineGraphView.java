/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.nrftoolbox.nfc_ble_hybrid;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * This class uses external library AChartEngine to show dynamic real time line graph for HR values
 */
public class LineGraphView {
	//TimeSeries will hold the data in x,y format for single chart
//	private TimeSeries mSeries = new TimeSeries(" ");
	private TimeSeries axisxSeries = new TimeSeries("x");
	private TimeSeries axisySeries = new TimeSeries("y");
	private TimeSeries axiszSeries = new TimeSeries("z");
	private TimeSeries axisaSeries = new TimeSeries("a");
	//XYMultipleSeriesDataset will contain all the TimeSeries
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	//XYMultipleSeriesRenderer will contain all XYSeriesRenderer and it can be used to set the properties of whole Graph
	private XYMultipleSeriesRenderer mMultiRenderer = new XYMultipleSeriesRenderer();
	private LineGraphView mInstance = null;
	private double x_init;
	public double getx_init(){return x_init;}
	private double y_init;
	public double gety_init(){return y_init;}
	private double z_init;
	public double getz_init(){return z_init;}
	private double a_init;
	public double geta_init(){return a_init;}
	/**
	 * singleton implementation of LineGraphView class
	 */
	public LineGraphView getLineGraphView() {
		if (mInstance == null) {
			mInstance = new LineGraphView();
		}
		return mInstance;
	}

	/**
	 * This constructor will set some properties of single chart and some properties of whole graph
	 */
	public LineGraphView() {
		//add single line chart mSeries
//		mDataset.addSeries(mSeries);
		mDataset.addSeries(axisxSeries);
		mDataset.addSeries(axisySeries);
		mDataset.addSeries(axiszSeries);
		mDataset.addSeries(axisaSeries);

		//XYSeriesRenderer is used to set the properties like chart color, style of each point, etc. of single chart
//		final XYSeriesRenderer EMGData = new XYSeriesRenderer();
//		//set line chart color to Black
//		EMGData.setColor(0xFF009933);
//		//set line chart style to square points
//		EMGData.setPointStyle(PointStyle.CIRCLE);
//		EMGData.setFillPoints(true);
//		EMGData.setLineWidth(3);


		final XYSeriesRenderer xdata = new XYSeriesRenderer();
		//set line chart color to Black
		xdata.setColor(Color.rgb(0,200,200));
		//set line chart style to square points
		xdata.setPointStyle(PointStyle.CIRCLE);
		xdata.setFillPoints(true);
		xdata.setLineWidth(5);

		final XYSeriesRenderer ydata = new XYSeriesRenderer();
		//set line chart color to Black
		ydata.setColor(0xFF00FF00);
		//set line chart style to square points
		ydata.setPointStyle(PointStyle.CIRCLE);
		ydata.setLineWidth(5);

		final XYSeriesRenderer zdata = new XYSeriesRenderer();
		//set line chart color to Black
		zdata.setColor(0xFFFF0000);
		//set line chart style to square points
		zdata.setPointStyle(PointStyle.CIRCLE);
		zdata.setFillPoints(true);
		zdata.setLineWidth(5);

		final XYSeriesRenderer adata = new XYSeriesRenderer();
		//set line chart color to Black
		adata.setColor(0xFF0000FF);
		//set line chart style to square points
		adata.setPointStyle(PointStyle.CIRCLE);
		adata.setFillPoints(true);
		adata.setLineWidth(5);

		final XYMultipleSeriesRenderer renderer = mMultiRenderer;
		//set whole graph background color to transparent color
		renderer.setBackgroundColor(Color.TRANSPARENT);
		renderer.setMargins(new int[]{50, 105, 80, 5}); // top, left, bottom, right
		renderer.setMarginsColor(Color.argb(0x00, 0x05, 0x05, 0x05));
		renderer.setAxesColor(0xFFFFFFFF);
		renderer.setAxisTitleTextSize(5);
		renderer.setShowGrid(true);
		//renderer.setGridColor(Color.GREEN);
		renderer.setLabelsColor(Color.rgb(255,255,255));
		//renderer.setYLabelsColor(0, 0xFFFFFFF);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setYLabelsPadding(30);
		renderer.setXLabelsColor(0xFFFFFFFF);
		renderer.setLabelsTextSize(30);
		renderer.setYAxisMax(10);
		renderer.setYAxisMin(0);
		renderer.setXAxisMax(NFC_BLE_HYBRID_Activity.GRAPH_WINDOW);
		renderer.setXAxisMin(0);
		renderer.setXLabels(8);
		renderer.setYLabels(5);
		renderer.setLegendTextSize(25);
		renderer.setShowGrid(true);
		renderer.setXTitle("  ");
		renderer.setYTitle("                   ");
		renderer.setXLabels(0);
		//renderer.setYLabels(0);
		//renderer.setShowLabels(false);


		//Disable zoom
		renderer.setPanEnabled(false, true);
		renderer.setZoomEnabled(false, true);
		//set title to x-axis and y-axis
		//renderer.addSeriesRenderer(EMGData);

		renderer.addSeriesRenderer(xdata);
		renderer.addSeriesRenderer(ydata);
		renderer.addSeriesRenderer(zdata);
		renderer.addSeriesRenderer(adata);

	}


	//this is the second function




	/**
	 * return graph view to activity
	 */
	public GraphicalView getView(Context context) {
		final GraphicalView graphView = ChartFactory.getLineChartView(context, mDataset, mMultiRenderer);
		return graphView;
	}

	/**
	 * add new x,y value to chart
	 */
	public void LineGraphViewUp(int xmin, int xmax) {
		final XYMultipleSeriesRenderer rendererupdate = mMultiRenderer;
		rendererupdate.setXAxisMin(xmin);
		rendererupdate.setXAxisMax(xmax);

	}

//	public void addValue( int x, int y) {
//		mSeries.add(x,y);
//		//mMultiRenderer.addXTextLabel((p.x*1.0), "");
//		//renderer.addXTextLabel(x, "label");
//	}


	public void removeold(int p) {

		axisxSeries.remove(p);
		axisySeries.remove(p);
		axiszSeries.remove(p);
		axisaSeries.remove(p);
	}



	public void addValue_x(int x, double y) {
		if (axisxSeries.getItemCount()==0) {
			x_init = y;
			axisxSeries.add(x, 0);
		}
		else axisxSeries.add(x, y-x_init);
	}

	public void addValue_y(int x, double y) {
		if (axisySeries.getItemCount()==0) {
			y_init = y;
			axisySeries.add(x, 0);
		}
		else axisySeries.add(x, y-y_init);
	}

	public void addValue_z(int x, double y) {
		if (axiszSeries.getItemCount()==0) {
			z_init = y;
			axiszSeries.add(x, 0);
		}
		else axiszSeries.add(x, y-z_init);
	}

	public void addValue_a(int x, double y) {
		if (axisaSeries.getItemCount()==0) {
			a_init = y;
			axisaSeries.add(x, 0);
		}
		else axisaSeries.add(x, y-a_init);
	}

	/**
	 * clear all previous values of chart
	 */
	public void clearGraph() {

		axisxSeries.clear();
		axisySeries.clear();
		axiszSeries.clear();
		axisaSeries.clear();
	}

}
