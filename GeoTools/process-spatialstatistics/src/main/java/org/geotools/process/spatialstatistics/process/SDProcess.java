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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.ProcessFactory;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.process.spatialstatistics.distribution.StandardDistanceOperation;
import org.geotools.util.logging.Logging;
import org.opengis.util.ProgressListener;

/**
 * Measures the degree to which features are concentrated or dispersed around the geometric mean center.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class SDProcess extends AbstractStatisticsProcess {
    protected static final Logger LOGGER = Logging.getLogger(SDProcess.class);

    public SDProcess(ProcessFactory factory) {
        super(factory);
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public static SimpleFeatureCollection process(SimpleFeatureCollection inputFeatures,
            String circleSize, String weightField, String caseField, ProgressListener monitor) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SDProcessFactory.inputFeatures.key, inputFeatures);
        map.put(SDProcessFactory.circleSize.key, circleSize);
        map.put(SDProcessFactory.weightField.key, weightField);
        map.put(SDProcessFactory.caseField.key, caseField);

        Process process = new SDProcess(null);
        Map<String, Object> resultMap;
        try {
            resultMap = process.execute(map, monitor);
            return (SimpleFeatureCollection) resultMap.get(SDProcessFactory.RESULT.key);
        } catch (ProcessException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input, ProgressListener monitor)
            throws ProcessException {
        SimpleFeatureCollection inputFeatures = (SimpleFeatureCollection) Params.getValue(input,
                SDProcessFactory.inputFeatures, null);
        if (inputFeatures == null) {
            throw new NullPointerException("inputFeatures parameters required");
        }

        String weightField = (String) Params.getValue(input, SDProcessFactory.weightField, null);
        String caseField = (String) Params.getValue(input, SDProcessFactory.caseField, null);
        String circleSize = (String) Params.getValue(input, SDProcessFactory.circleSize,
                SDProcessFactory.circleSize.sample);

        // start process
        // 1_STANDARD_DEVIATION
        double stdDeviation = 1.0;
        if (circleSize.contains("2")) {
            stdDeviation = 2.0;
        } else if (circleSize.contains("3")) {
            stdDeviation = 3.0;
        }

        SimpleFeatureCollection resultFc = null;
        try {
            StandardDistanceOperation process = new StandardDistanceOperation();
            process.setStdDeviation(stdDeviation);
            resultFc = process.execute(inputFeatures, weightField, caseField);
        } catch (IOException e) {
            throw new ProcessException(e);
        }
        // end process

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(SDProcessFactory.RESULT.key, resultFc);
        return resultMap;
    }
}
