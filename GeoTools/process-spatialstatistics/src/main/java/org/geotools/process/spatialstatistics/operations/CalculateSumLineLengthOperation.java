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
package org.geotools.process.spatialstatistics.operations;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.process.spatialstatistics.core.DataUtils;
import org.geotools.process.spatialstatistics.core.FeatureTypes;
import org.geotools.process.spatialstatistics.process.SumLineLengthProcessFactory;
import org.geotools.process.spatialstatistics.storage.IFeatureInserter;
import org.geotools.process.spatialstatistics.transformation.ReprojectFeatureCollection;
import org.geotools.referencing.CRS;
import org.geotools.util.Converters;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calculate the total sum of line lengths for each feature of a polygon features.
 * 
 * @author Minpa Lee, MangoSystem
 * 
 * @source $URL$
 */
public class CalculateSumLineLengthOperation extends GeneralOperation {
    protected static final Logger LOGGER = Logging.getLogger(CalculateSumLineLengthOperation.class);

    static final String LENGTH = (String) SumLineLengthProcessFactory.lengthField.sample;

    static final String COUNT = (String) SumLineLengthProcessFactory.countField.sample;

    public SimpleFeatureCollection execute(SimpleFeatureCollection polygons, String lengthField,
            String countField, SimpleFeatureCollection lines) throws IOException {
        Class<?> binding = polygons.getSchema().getGeometryDescriptor().getType().getBinding();
        if (!binding.isAssignableFrom(Polygon.class)
                && !binding.isAssignableFrom(MultiPolygon.class)) {
            throw new InvalidParameterValueException("Invalid parameters", "polygonFeatures",
                    polygons);
        }

        binding = lines.getSchema().getGeometryDescriptor().getType().getBinding();
        if (!binding.isAssignableFrom(LineString.class)
                && !binding.isAssignableFrom(MultiLineString.class)) {
            throw new InvalidParameterValueException("Invalid parameters", "lineFeatures", lines);
        }

        // prepare feature type
        if (lengthField == null || lengthField.length() == 0) {
            lengthField = LENGTH;
        }
        if (countField == null || countField.length() == 0) {
            countField = COUNT;
        }

        String typeName = polygons.getSchema().getTypeName();
        SimpleFeatureType featureType = FeatureTypes.build(polygons, typeName);
        featureType = FeatureTypes.add(featureType, lengthField, Double.class, 38);
        featureType = FeatureTypes.add(featureType, countField, Integer.class, 5);

        // number, string, int, long, float, double....
        AttributeDescriptor lenDsc = featureType.getDescriptor(lengthField);
        Class<?> lengthBinding = lenDsc.getType().getBinding();

        AttributeDescriptor cntDsc = featureType.getDescriptor(lengthField);
        Class<?> countBinding = cntDsc.getType().getBinding();

        String the_geom = lines.getSchema().getGeometryDescriptor().getLocalName();

        // check coordinate reference system
        CoordinateReferenceSystem crsT = polygons.getSchema().getCoordinateReferenceSystem();
        CoordinateReferenceSystem crsS = lines.getSchema().getCoordinateReferenceSystem();
        if (crsT != null && crsS != null && !CRS.equalsIgnoreMetadata(crsT, crsS)) {
            lines = new ReprojectFeatureCollection(lines, crsS, crsT, true);
            LOGGER.log(Level.WARNING, "reprojecting features");
        }

        // using SpatialIndexFeatureCollection
        SimpleFeatureCollection indexed = DataUtils.toSpatialIndexFeatureCollection(lines);

        // prepare transactional feature store
        IFeatureInserter featureWriter = getFeatureWriter(featureType);
        SimpleFeatureIterator featureIter = polygons.features();
        try {
            while (featureIter.hasNext()) {
                SimpleFeature feature = featureIter.next();
                Geometry clipGeometry = (Geometry) feature.getDefaultGeometry();
                if (clipGeometry == null || clipGeometry.isEmpty()) {
                    continue;
                }

                Filter filter = getIntersectsFilter(the_geom, clipGeometry);

                double sumLength = 0d;
                int lineCount = 0;
                SimpleFeatureIterator lineIter = indexed.subCollection(filter).features();
                try {
                    while (lineIter.hasNext()) {
                        SimpleFeature lineFeature = lineIter.next();
                        Geometry lineStrings = (Geometry) lineFeature.getDefaultGeometry();
                        Geometry clipedGeometry = lineStrings.intersection(clipGeometry);
                        if (clipedGeometry != null) {
                            sumLength += clipedGeometry.getLength();
                            lineCount++;
                        }
                    }
                } finally {
                    lineIter.close();
                }

                SimpleFeature newFeature = featureWriter.buildFeature();
                featureWriter.copyAttributes(feature, newFeature, true);
                newFeature.setAttribute(lengthField, Converters.convert(sumLength, lengthBinding));
                newFeature.setAttribute(countField, Converters.convert(lineCount, countBinding));
                featureWriter.write(newFeature);
            }
        } catch (Exception e) {
            featureWriter.rollback(e);
        } finally {
            featureWriter.close(featureIter);
        }

        return featureWriter.getFeatureCollection();
    }
}
