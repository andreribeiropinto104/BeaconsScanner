package com.northteam.beaconsscanner.util;

import com.northteam.beaconsscanner.util.Dataset;

import java.util.ArrayList;

/**
 *
 * @author andrepinto
 */
public class PowerRegression {

        /**
     * Returns the parameters 'a' and 'b' for an equation y = ax^b, fitted to 
     * the data using a power regression equation.  The result is returned as 
     * an array, where double[0] --> a, and double[1] --> b.
     *
     * @param data  the data.
     * @return The parameters.
     */
    public static double[] getPowerRegression(Dataset data) {

        int n = data.getItemCount();
        if (n < 2) {
            throw new IllegalArgumentException("Not enough data.");
        }

        double sumX = 0;
        double sumY = 0;
        double sumXX = 0;
        double sumXY = 0;
        for (int i = 0; i < n; i++) {
            double x = Math.log(data.getXValue(i));
            double y = Math.log(data.getYValue(i));
            sumX += x;
            sumY += y;
            double xx = x * x;
            sumXX += xx;
            double xy = x * y;
            sumXY += xy;
        }
        double sxx = sumXX - (sumX * sumX) / n;
        double sxy = sumXY - (sumX * sumY) / n;
        double xbar = sumX / n;
        double ybar = sumY / n;

        double[] result = new double[2];
        result[1] = sxy / sxx;
        result[0] = Math.pow(Math.exp(1.0), ybar - result[1] * xbar);

        return result;

    }
    
}
