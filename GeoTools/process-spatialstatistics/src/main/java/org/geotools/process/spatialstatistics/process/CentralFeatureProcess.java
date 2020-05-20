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
import org.geotools.process.spatialstatistics.distribution.CentralFeatureOperation;
import org.geotools.process.spatialstatistics.enumeration.DistanceMethod;
import org.geotools.util.logging.Logging;
import org.opengis.util.ProgressListener;

/**
 * Identifies the most centrally located feature in a point, line, or polygon feature class.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class CentralFeatureProcess extends AbstractStatisticsProcess {
    protected static final Logger LOGGER = Logging.getLogger(CentralFeatureProcess.class);

    public CentralFeatureProcess(ProcessFactory factory) {
        super(factory);
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public static SimpleFeatureCollection process(SimpleFeatureCollection inputFeatures,
            DistanceMethod distanceMethod, String weightField, String selfPotentialWeightField,
            String caseField, ProgressListener monitor) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CentralFeatureProcessFactory.inputFeatures.key, inputFeatures);
        map.put(CentralFeatureProcessFactory.distanceMethod.key, distanceMethod);
        map.put(CentralFeatureProcessFactory.weightField.key, weightField);
        map.put(CentralFeatureProcessFactory.selfPotentialWeightField.key, selfPotentialWeightField);
        map.put(CentralFeatureProcessFactory.caseField.key, caseField);

        Process process = new CentralFeatureProcess(null);
        Map<String, Object> resultMap;
        try {
            resultMap = process.execute(map, monitor);
            return (SimpleFeatureCollection) resultMap.get(CentralFeatureProcessFactory.RESULT.key);
        } catch (ProcessException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input, ProgressListener monitor)
            throws ProcessException {
        SimpleFeatureCollection inputFeatures = (SimpleFeatureCollection) Params.getValue(input,
                CentralFeatureProcessFactory.inputFeatures, null);
        if (inputFeatures == null) {
            throw new NullPointerException("inputFeatures parameter required");
        }

        DistanceMethod distanceMethod = (DistanceMethod) Params.getValue(input,
                CentralFeatureProcessFactory.distanceMethod,
                CentralFeatureProcessFactory.distanceMethod.sample);
        String weightField = (String) Params.getValue(input,
                CentralFeatureProcessFactory.weightField, null);
        String selfPotentialWeightField = (String) Params.getValue(input,
                CentralFeatureProcessFactory.selfPotentialWeightField, null);
        String caseField = (String) Params.getValue(input, CentralFeatureProcessFactory.caseField,
                null);

        // start process
        SimpleFeatureCollection resultFc = null;
        try {
            CentralFeatureOperation process = new CentralFeatureOperation();
            process.setDistanceMethod(distanceMethod);
            resultFc = process.execute(inputFeatures, weightField, selfPotentialWeightField,
                    caseField);
        } catch (IOException e) {
            throw new ProcessException(e);
        }
        // end process

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(CentralFeatureProcessFactory.RESULT.key, resultFc);
        return resultMap;
    }
}
