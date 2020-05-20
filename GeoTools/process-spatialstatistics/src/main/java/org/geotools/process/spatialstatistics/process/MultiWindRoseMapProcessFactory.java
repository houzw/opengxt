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
package org.geotools.process.spatialstatistics.process;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.geotools.data.Parameter;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.process.Process;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.opengis.util.InternationalString;

/**
 * MultiWindRoseMapProcessFactory
 * 
 * @author jyajya, MangoSystem
 * 
 * @source $URL$
 */
public class MultiWindRoseMapProcessFactory extends SpatialStatisticsProcessFactory {
    protected static final Logger LOGGER = Logging.getLogger(MultiWindRoseMapProcessFactory.class);

    private static final String PROCESS_NAME = "MultiWindRoseMap";

    // VA_Fishnet(extent BoundingBoxData, columns Integer, rows Integer, width Double, height
    // Double) : GML
    // VA_Fishnet(boundary GML, boundaryInside Boolean, columns Integer, rows Integer, width Double,
    // height Double) : GML

    public MultiWindRoseMapProcessFactory() {
        super(new NameImpl(NAMESPACE, PROCESS_NAME));
    }
//  SimpleFeatureCollection inputFeatures, String weightField,
//  SimpleFeatureCollection centerFeatures, double searchRadius, String valueField, int roseCnt

    /** inputFeatures */
	public static final Parameter<SimpleFeatureCollection> inputFeatures = new Parameter<SimpleFeatureCollection>(
			"inputFeatures", SimpleFeatureCollection.class, 
			getResource("MultiWindRoseMap.inputFeatures.title"),
			getResource("MultiWindRoseMap.inputFeatures.description"), true, 1, Integer.MAX_VALUE, null, null);

	/** weightField */
	public static final Parameter<String> weightFields = new Parameter<String>(
			"weightField", String.class,
			getResource("MultiWindRoseMap.weightFields.title"),
			getResource("MultiWindRoseMap.weightFields.description"), false, 0, 1, null, null);
    
//    /** inputFeatures */
//    public static final Parameter<SimpleFeatureCollection> inputFeatures2 = new Parameter<SimpleFeatureCollection>(
//            "inputFeatures2", SimpleFeatureCollection.class, Text.text("Input Point Layer 2"),
//            Text.text("Input Vector Layer"), false, 1, 1, null, null);
//
//    /** weightField */
//    public static final Parameter<String> weightField2 = new Parameter<String>(
//            "weightField2", String.class, Text.text("Field to apply the weight(Point Layer 2)"),
//            Text.text("Field to apply the weight"), false, 0, 1, null, null);
//    
//    /** inputFeatures */
//    public static final Parameter<SimpleFeatureCollection> inputFeatures3 = new Parameter<SimpleFeatureCollection>(
//            "inputFeatures3", SimpleFeatureCollection.class, Text.text("Input Point Layer 3"),
//            Text.text("Input Vector Layer"), false, 1, 1, null, null);
//
//    /** weightField */
//    public static final Parameter<String> weightField3 = new Parameter<String>(
//            "weightField3", String.class, Text.text("Field to apply the weight(Point Layer 3)"),
//            Text.text("Field to apply the weight"), false, 0, 1, null, null);
    
    /** centerFeatures */
    public static final Parameter<SimpleFeatureCollection> centerFeatures = new Parameter<SimpleFeatureCollection>(
            "centerFeatures", SimpleFeatureCollection.class, 
            getResource("MultiWindRoseMap.centerFeatures.title"),
            getResource("MultiWindRoseMap.centerFeatures.description"), false, 0, 1, null, null);
    public static final Parameter<Geometry> centerPoint = new Parameter<Geometry>(
            "centerPoint", Geometry.class, 
            getResource("MultiWindRoseMap.centerPoint.title"),
            getResource("MultiWindRoseMap.centerPoint.description"), false, 0, 1, null, null);

    /** searchRadius */
    public static final Parameter<Double> searchRadius = new Parameter<Double>(
            "searchRadius", Double.class, 
            getResource("MultiWindRoseMap.searchRadius.title"),
            getResource("MultiWindRoseMap.searchRadius.description"), false, 0, 1, null, null);
    
    /** roseCount */
    public static final Parameter<Integer> roseCount = new Parameter<Integer>("roseCount", Integer.class,
    		getResource("MultiWindRoseMap.roseCount.title"), 
    		getResource("MultiWindRoseMap.roseCount.description"), true, 0, 1, 16, null);

    @Override
    protected Map<String, Parameter<?>> getParameterInfo() {
        HashMap<String, Parameter<?>> parameterInfo = new LinkedHashMap<String, Parameter<?>>();
        parameterInfo.put(inputFeatures.key, inputFeatures);
        parameterInfo.put(weightFields.key, weightFields);
//        parameterInfo.put(inputFeatures2.key, inputFeatures2);
//        parameterInfo.put(weightField2.key, weightField2);
//        parameterInfo.put(inputFeatures3.key, inputFeatures3);
//        parameterInfo.put(weightField3.key, weightField3);
        parameterInfo.put(centerFeatures.key, centerFeatures);
        parameterInfo.put(centerPoint.key, centerPoint);
        parameterInfo.put(searchRadius.key, searchRadius);
        parameterInfo.put(roseCount.key, roseCount);
        return parameterInfo;
    }

    public static final Parameter<SimpleFeatureCollection> result = new Parameter<SimpleFeatureCollection>(
            "multi_wind_rose", SimpleFeatureCollection.class, 
            getResource("MultiWindRoseMap.result.title"),
            getResource("MultiWindRoseMap.result.description"));
    public static final Parameter<SimpleFeatureCollection> anchor = new Parameter<SimpleFeatureCollection>(
            "multi_wind_rose_anchor", SimpleFeatureCollection.class, 
            getResource("MultiWindRoseMap.anchor.title"),
            getResource("MultiWindRoseMap.anchor.description"));

    static final Map<String, Parameter<?>> resultInfo = new TreeMap<String, Parameter<?>>();
    static {
        resultInfo.put(result.key, result);
        resultInfo.put(anchor.key, anchor);
    }

    @Override
    protected Map<String, Parameter<?>> getResultInfo(Map<String, Object> parameters)
            throws IllegalArgumentException {
        return Collections.unmodifiableMap(resultInfo);
    }

    @Override
    protected Process create() {
        return new MultiWindRoseMapProcess(this);
    }

    @Override
    public InternationalString getTitle() {
        return getResource("MultiWindRoseMap.title");
    }

    @Override
    protected InternationalString getDescription() {
        return getResource("MultiWindRoseMap.description");
    }

}
