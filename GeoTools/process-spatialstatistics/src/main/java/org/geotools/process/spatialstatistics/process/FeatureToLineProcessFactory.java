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

import org.geotools.data.Parameter;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.process.Process;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.util.KVP;
import org.geotools.util.logging.Logging;
import org.opengis.util.InternationalString;

import java.util.*;
import java.util.logging.Logger;

/**
 * FeatureToLineProcessFactory
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class FeatureToLineProcessFactory extends SpatialStatisticsProcessFactory {
    protected static final Logger LOGGER = Logging.getLogger(FeatureToLineProcessFactory.class);

    private static final String PROCESS_NAME = "FeatureToLine";

    /*
     * FeatureToLine(SimpleFeatureCollection inputFeatures, Boolean preserveAttributes): SimpleFeatureCollection
     */

    public FeatureToLineProcessFactory() {
        super(new NameImpl(NAMESPACE, PROCESS_NAME));
    }

    @Override
    public Process create() {
        return new FeatureToLineProcess(this);
    }

    @Override
    public InternationalString getTitle() {
        return getResource("FeatureToLine.title");
    }

    @Override
    public InternationalString getDescription() {
        return getResource("FeatureToLine.description");
    }

    /** inputFeatures */
    public static final Parameter<SimpleFeatureCollection> inputFeatures = new Parameter<SimpleFeatureCollection>(
            "inputFeatures", SimpleFeatureCollection.class,
            getResource("FeatureToLine.inputFeatures.title"),
            getResource("FeatureToLine.inputFeatures.description"), true, 1, 1, null, new KVP(
                    Params.FEATURES, Params.Polyline));

    /** preserveAttributes */
    public static final Parameter<Boolean> preserveAttributes = new Parameter<Boolean>(
            "preserveAttributes", Boolean.class,
            getResource("FeatureToLine.preserveAttributes.title"),
            getResource("FeatureToLine.preserveAttributes.description"), false, 0, 1, Boolean.TRUE,
            null);

    @Override
    protected Map<String, Parameter<?>> getParameterInfo() {
        HashMap<String, Parameter<?>> parameterInfo = new LinkedHashMap<String, Parameter<?>>();
        parameterInfo.put(inputFeatures.key, inputFeatures);
        parameterInfo.put(preserveAttributes.key, preserveAttributes);
        return parameterInfo;
    }

    /** result */
    public static final Parameter<SimpleFeatureCollection> RESULT = new Parameter<SimpleFeatureCollection>(
            "result", SimpleFeatureCollection.class, getResource("FeatureToLine.result.title"),
            getResource("FeatureToLine.result.description"));

    static final Map<String, Parameter<?>> resultInfo = new TreeMap<String, Parameter<?>>();
    static {
        resultInfo.put(RESULT.key, RESULT);
    }

    @Override
    protected Map<String, Parameter<?>> getResultInfo(Map<String, Object> parameters)
            throws IllegalArgumentException {
        return Collections.unmodifiableMap(resultInfo);
    }

}
