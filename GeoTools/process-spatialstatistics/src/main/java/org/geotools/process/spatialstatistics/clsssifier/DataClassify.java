/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2014, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.process.spatialstatistics.clsssifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.process.spatialstatistics.core.FeatureTypes;
import org.geotools.process.spatialstatistics.core.SSUtils;
import org.geotools.process.spatialstatistics.core.StringHelper;
import org.geotools.process.spatialstatistics.gridcoverage.RasterHelper;
import org.geotools.util.factory.GeoTools;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;

/**
 * Classify objects apply one of several methods to statistically subdivide a set of numeric values into classes.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public abstract class DataClassify {
    protected static final Logger LOGGER = Logging.getLogger(DataClassify.class);

    protected Double[] classBreaks;

    public Double[] getClassBreaks() {
        return this.classBreaks;
    }

    /**
     * The name of the classification method
     * 
     * @return
     */
    public abstract String getMethodName();

    public abstract Double[] classify(double[] doubleValues, int[] longFrequencies, int binCount);

    public abstract Double[] classify(SimpleFeatureCollection inputFeatures, String fieldName,
            int binCount);

    public abstract Double[] classify(GridCoverage2D inputGc, int bandIndex, int binCount);

    protected void initializeClassBreaks(int binCount) {
        classBreaks = new Double[binCount + 1];
        for (int index = 0; index < classBreaks.length; index++) {
            classBreaks[index] = Double.valueOf(0.0);
        }
    }

    protected List<Double> retriveValue(GridCoverage2D inputGc, int bandIndex) {
        List<Double> sortedValueList = new ArrayList<Double>();

        double noDataValue = RasterHelper.getNoDataValue(inputGc);

        // 1. Iteration
        PlanarImage inputImage = (PlanarImage) inputGc.getRenderedImage();
        RectIter readIter = RectIterFactory.create(inputImage, inputImage.getBounds());

        readIter.startLines();
        while (!readIter.finishedLines()) {
            readIter.startPixels();
            while (!readIter.finishedPixels()) {
                double value = readIter.getSampleDouble(bandIndex);
                if (!SSUtils.compareDouble(noDataValue, value)) {
                    sortedValueList.add(Double.valueOf(value));
                }
                readIter.nextPixel();
            }
            readIter.nextLine();
        }

        Collections.sort(sortedValueList);

        return sortedValueList;
    }

    protected List<Double> retriveValue(SimpleFeatureCollection inputFeatures,
            String propertyName) {
        List<Double> sortedValueList = new ArrayList<Double>();

        if (!StringHelper.isNullOrEmpty(propertyName)) {
            propertyName = FeatureTypes.validateProperty(inputFeatures.getSchema(), propertyName);
        }

        if (inputFeatures.getSchema().indexOf(propertyName) == -1) {
            LOGGER.warning(propertyName + " does not exist!");
            return sortedValueList;
        }

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
        final Expression attrExpr = ff.property(propertyName);

        SimpleFeatureIterator featureIter = null;
        try {
            featureIter = inputFeatures.features();
            while (featureIter.hasNext()) {
                SimpleFeature feature = featureIter.next();

                Double value = attrExpr.evaluate(feature, Double.class);
                if (value != null) {
                    double doubleVal = value.doubleValue();
                    if (Double.isNaN(doubleVal) || Double.isInfinite(doubleVal)) {
                        continue;
                    }
                    sortedValueList.add(value);
                }
            }
        } finally {
            featureIter.close();
        }

        Collections.sort(sortedValueList);

        return sortedValueList;
    }

}
