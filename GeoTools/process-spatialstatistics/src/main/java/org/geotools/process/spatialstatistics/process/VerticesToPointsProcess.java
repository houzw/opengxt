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

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.ProcessFactory;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.process.spatialstatistics.enumeration.PointLocationType;
import org.geotools.process.spatialstatistics.transformation.VerticesToPointsFeatureCollection;
import org.geotools.util.logging.Logging;
import org.opengis.util.ProgressListener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a feature class containing points generated from specified vertices or locations of the input features.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class VerticesToPointsProcess extends AbstractStatisticsProcess {

    protected static final Logger LOGGER = Logging.getLogger(VerticesToPointsProcess.class);

    public VerticesToPointsProcess(ProcessFactory factory) {
        super(factory);
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public static Integer process(SimpleFeatureCollection inputFeatures,
            PointLocationType location, ProgressListener monitor) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(VerticesToPointsProcessFactory.inputFeatures.key, inputFeatures);
        map.put(VerticesToPointsProcessFactory.location.key, location);

        Process process = new VerticesToPointsProcess(null);
        Map<String, Object> resultMap;
        try {
            resultMap = process.execute(map, monitor);
            return (Integer) resultMap.get(VerticesToPointsProcessFactory.RESULT.key);
        } catch (ProcessException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }

        return -1;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input, ProgressListener monitor)
            throws ProcessException {
        SimpleFeatureCollection inputFeatures = (SimpleFeatureCollection) Params.getValue(input,
                VerticesToPointsProcessFactory.inputFeatures, null);
        PointLocationType location = (PointLocationType) Params.getValue(input,
                VerticesToPointsProcessFactory.location,
                VerticesToPointsProcessFactory.location.sample);
        if (inputFeatures == null) {
            throw new NullPointerException("inputFeatures parameters required");
        }

        // start process
        SimpleFeatureCollection resultFc = DataUtilities
                .simple(new VerticesToPointsFeatureCollection(inputFeatures, location));
        // end process

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(VerticesToPointsProcessFactory.RESULT.key, resultFc);
        return resultMap;
    }

}
