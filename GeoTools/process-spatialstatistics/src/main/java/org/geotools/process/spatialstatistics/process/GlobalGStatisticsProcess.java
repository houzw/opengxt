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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.process.Process;
import org.geotools.process.ProcessException;
import org.geotools.process.ProcessFactory;
import org.geotools.process.spatialstatistics.autocorrelation.GlobalGStatisticOperation;
import org.geotools.process.spatialstatistics.autocorrelation.GlobalGStatisticOperation.GeneralG;
import org.geotools.process.spatialstatistics.core.FeatureTypes;
import org.geotools.process.spatialstatistics.core.FormatUtils;
import org.geotools.process.spatialstatistics.core.Params;
import org.geotools.process.spatialstatistics.enumeration.DistanceMethod;
import org.geotools.process.spatialstatistics.enumeration.SpatialConcept;
import org.geotools.process.spatialstatistics.enumeration.StandardizationMethod;
import org.geotools.util.logging.Logging;
import org.opengis.util.ProgressListener;

/**
 * Measures the degree of clustering for either high values or low values using the Getis-Ord General G statistic.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class GlobalGStatisticsProcess extends AbstractStatisticsProcess {
    protected static final Logger LOGGER = Logging.getLogger(GlobalGStatisticsProcess.class);

    public GlobalGStatisticsProcess(ProcessFactory factory) {
        super(factory);
    }

    public ProcessFactory getFactory() {
        return factory;
    }

    public static GStatisticsProcessResult process(SimpleFeatureCollection inputFeatures,
            String inputField, SpatialConcept spatialConcept, DistanceMethod distanceMethod,
            StandardizationMethod standardization, Double searchDistance, Boolean selfNeighbors,
            ProgressListener monitor) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(GlobalGStatisticsProcessFactory.inputFeatures.key, inputFeatures);
        map.put(GlobalGStatisticsProcessFactory.inputField.key, inputField);
        map.put(GlobalGStatisticsProcessFactory.spatialConcept.key, spatialConcept);
        map.put(GlobalGStatisticsProcessFactory.distanceMethod.key, distanceMethod);
        map.put(GlobalGStatisticsProcessFactory.standardization.key, standardization);
        map.put(GlobalGStatisticsProcessFactory.searchDistance.key, searchDistance);
        map.put(GlobalGStatisticsProcessFactory.selfNeighbors.key, selfNeighbors);

        Process process = new GlobalGStatisticsProcess(null);
        Map<String, Object> resultMap;
        try {
            resultMap = process.execute(map, monitor);
            return (GStatisticsProcessResult) resultMap
                    .get(GlobalGStatisticsProcessFactory.RESULT.key);
        } catch (ProcessException e) {
            LOGGER.log(Level.FINER, e.getMessage(), e);
        }

        return new GStatisticsProcessResult(inputFeatures.getSchema().getTypeName(), inputField,
                new GeneralG());
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> input, ProgressListener monitor)
            throws ProcessException {
        SimpleFeatureCollection inputFeatures = (SimpleFeatureCollection) Params.getValue(input,
                GlobalGStatisticsProcessFactory.inputFeatures, null);
        String inputField = (String) Params.getValue(input,
                GlobalGStatisticsProcessFactory.inputField, null);
        if (inputFeatures == null || inputField == null) {
            throw new NullPointerException("inputFeatures and inputField parameters required");
        }

        inputField = FeatureTypes.validateProperty(inputFeatures.getSchema(), inputField);
        if (inputFeatures.getSchema().indexOf(inputField) == -1) {
            throw new NullPointerException(inputField + " field does not exist!");
        }

        SpatialConcept spatialConcept = (SpatialConcept) Params.getValue(input,
                GlobalGStatisticsProcessFactory.spatialConcept,
                GlobalGStatisticsProcessFactory.spatialConcept.sample);

        DistanceMethod distanceMethod = (DistanceMethod) Params.getValue(input,
                GlobalGStatisticsProcessFactory.distanceMethod,
                GlobalGStatisticsProcessFactory.distanceMethod.sample);

        StandardizationMethod standardization = (StandardizationMethod) Params.getValue(input,
                GlobalGStatisticsProcessFactory.standardization,
                GlobalGStatisticsProcessFactory.standardization.sample);

        Double searchDistance = (Double) Params.getValue(input,
                GlobalGStatisticsProcessFactory.searchDistance,
                GlobalGStatisticsProcessFactory.searchDistance.sample);

        Boolean selfNeighbors = (Boolean) Params.getValue(input,
                GlobalGStatisticsProcessFactory.selfNeighbors,
                GlobalGStatisticsProcessFactory.selfNeighbors.sample);

        // start process
        String typeName = inputFeatures.getSchema().getTypeName();

        GlobalGStatisticOperation process = new GlobalGStatisticOperation();
        process.setSpatialConceptType(spatialConcept);
        process.setDistanceType(distanceMethod);
        process.setStandardizationType(standardization);
        process.setSelfNeighbors(selfNeighbors);

        // searchDistance
        if (searchDistance > 0 && !Double.isNaN(searchDistance)) {
            process.setDistanceBand(searchDistance);
        }

        GeneralG ret = process.execute(inputFeatures, inputField);
        GStatisticsProcessResult processResult = new GStatisticsProcessResult(typeName, inputField,
                ret);
        // end process

        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(GlobalGStatisticsProcessFactory.RESULT.key, processResult);
        return resultMap;
    }

    public static class GStatisticsProcessResult {

        String typeName;

        String propertyName;

        String observed_Index;

        String expected_Index;

        String variance;

        String z_Score;

        String p_Value;

        String conceptualization;

        String distanceMethod;

        String rowStandardization;

        String distanceThreshold;

        public GStatisticsProcessResult(String typeName, String propertyName, GeneralG ret) {
            this.typeName = typeName;
            this.propertyName = propertyName;

            this.observed_Index = FormatUtils.format(ret.getObservedIndex());
            this.expected_Index = FormatUtils.format(ret.getExpectedIndex());
            this.variance = FormatUtils.format(ret.getZVariance());
            this.z_Score = FormatUtils.format(ret.getZScore());
            this.p_Value = FormatUtils.format(ret.getPValue());
            this.conceptualization = ret.getConceptualization().toString();
            this.distanceMethod = ret.getDistanceMethod().toString();
            this.rowStandardization = ret.getRowStandardization().toString();
            this.distanceThreshold = FormatUtils.format(ret.getDistanceThreshold());
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getObserved_Index() {
            return observed_Index;
        }

        public void setObserved_Index(String observed_Index) {
            this.observed_Index = observed_Index;
        }

        public String getExpected_Index() {
            return expected_Index;
        }

        public void setExpected_Index(String expected_Index) {
            this.expected_Index = expected_Index;
        }

        public String getVariance() {
            return variance;
        }

        public void setVariance(String variance) {
            this.variance = variance;
        }

        public String getZ_Score() {
            return z_Score;
        }

        public void setZ_Score(String z_Score) {
            this.z_Score = z_Score;
        }

        public String getP_Value() {
            return p_Value;
        }

        public void setP_Value(String p_Value) {
            this.p_Value = p_Value;
        }

        public String getConceptualization() {
            return conceptualization;
        }

        public void setConceptualization(String conceptualization) {
            this.conceptualization = conceptualization;
        }

        public String getDistanceMethod() {
            return distanceMethod;
        }

        public void setDistanceMethod(String distanceMethod) {
            this.distanceMethod = distanceMethod;
        }

        public String getRowStandardization() {
            return rowStandardization;
        }

        public void setRowStandardization(String rowStandardization) {
            this.rowStandardization = rowStandardization;
        }

        public String getDistanceThreshold() {
            return distanceThreshold;
        }

        public void setDistanceThreshold(String distanceThreshold) {
            this.distanceThreshold = distanceThreshold;
        }

        @Override
        public String toString() {
            final String separator = System.getProperty("line.separator");

            StringBuffer sb = new StringBuffer();
            sb.append("Getis-Ord General G").append(separator);
            sb.append("TypeName: ").append(typeName).append(separator);
            sb.append("PropertyName: ").append(propertyName).append(separator);
            sb.append("Observed Index: ").append(observed_Index).append(separator);
            sb.append("Expected Index: ").append(expected_Index).append(separator);
            sb.append("Variance: ").append(variance).append(separator);
            sb.append("z Score: ").append(z_Score).append(separator);
            sb.append("p Value: ").append(p_Value).append(separator);
            sb.append("Conceptualization: ").append(conceptualization).append(separator);
            sb.append("DistanceMethod: ").append(distanceMethod).append(separator);
            sb.append("RowStandardization: ").append(rowStandardization).append(separator);
            sb.append("DistanceThreshold: ").append(distanceThreshold).append(separator);

            return sb.toString();
        }
    }

}
