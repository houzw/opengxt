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
 * UpdateProcessFactory
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class UpdateProcessFactory extends SpatialStatisticsProcessFactory {
    protected static final Logger LOGGER = Logging.getLogger(UpdateProcessFactory.class);

    private static final String PROCESS_NAME = "Update";

    /*
     * Update(SimpleFeatureCollection inputFeatures, SimpleFeatureCollection updateFeatures): SimpleFeatureCollection
     */

    public UpdateProcessFactory() {
        super(new NameImpl(NAMESPACE, PROCESS_NAME));
    }

    @Override
    public Process create() {
        return new UpdateProcess(this);
    }

    @Override
    public InternationalString getTitle() {
        return getResource("Update.title");
    }

    @Override
    public InternationalString getDescription() {
        return getResource("Update.description");
    }

    /** inputFeatures */
    public static final Parameter<SimpleFeatureCollection> inputFeatures = new Parameter<SimpleFeatureCollection>(
            "inputFeatures", SimpleFeatureCollection.class,
            getResource("Update.inputFeatures.title"),
            getResource("Update.inputFeatures.description"), true, 1, 1, null, new KVP(
                    Params.FEATURES, Params.Polygon));

    /** updateFeatures */
    public static final Parameter<SimpleFeatureCollection> updateFeatures = new Parameter<SimpleFeatureCollection>(
            "updateFeatures", SimpleFeatureCollection.class,
            getResource("Update.updateFeatures.title"),
            getResource("Update.updateFeatures.description"), true, 1, 1, null, new KVP(
                    Params.FEATURES, Params.Polygon));

    @Override
    protected Map<String, Parameter<?>> getParameterInfo() {
        HashMap<String, Parameter<?>> parameterInfo = new LinkedHashMap<String, Parameter<?>>();
        parameterInfo.put(inputFeatures.key, inputFeatures);
        parameterInfo.put(updateFeatures.key, updateFeatures);
        return parameterInfo;
    }

    /** result */
    public static final Parameter<SimpleFeatureCollection> RESULT = new Parameter<SimpleFeatureCollection>(
            "result", SimpleFeatureCollection.class, getResource("Update.result.title"),
            getResource("Update.result.description"), true, 1, 1, null, null);

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
