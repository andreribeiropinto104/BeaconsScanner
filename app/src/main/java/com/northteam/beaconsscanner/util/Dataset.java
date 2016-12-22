package com.northteam.beaconsscanner.util;

import java.util.ArrayList;

/**
 *
 * @author andrepinto
 */
public class Dataset {
    private ArrayList<Double> x;
    private ArrayList<Double> y;

    public Dataset(ArrayList<Double> x, ArrayList<Double> y) {
        this.x = x;
        this.y = y;
    }

    
    public double getXValue(int i) {
        return this.x.get(i);
    }
    
    
    public double getYValue(int i) {
        return this.y.get(i);
    }
    
    public int getItemCount() {
        return this.x.size();
    }
    
    /**
     * @return the x
     */
    public ArrayList<Double> getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(ArrayList<Double> x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public ArrayList<Double> getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(ArrayList<Double> y) {
        this.y = y;
    }
}

