package com.mns.uima.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.uima.UIMAFramework;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.annotator.AnnotatorInitializationException;
import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.ConstraintFactory;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FSTypeConstraint;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;

/** 
 * Utilities for working with and extracting information from a UIMA CAS
 * 
 * @author      Martin Saunders <martin.saunders@uk.ibm.com>
 * @version     2.1.0 $Revision: 168 $            
 */
public final class CASUtils {
	private static final String SENTENCETYPE = "uima.tt.SentenceAnnotation";
	private static final String PARAGRAPHTYPE = "uima.tt.ParagraphAnnotation";
	private static final String LEMMATYPE = "uima.tt.Lemma";
	private static final String LEMMAKEY = "key";
	
	private static final Logger logger = UIMAFramework.getLogger(CASUtils.class);
	/**
	 * Get a type object corresponding to a name.
	 * <p>
	 *
	 * @param  ts Type system the type should exist in
	 * @param  typeName Full type name as a string
	 * @return Type object
	 * @throws AnnotatorInitializationException
	 */
	public static final Type initType(TypeSystem ts, String typeName)
			throws AnnotatorInitializationException {
		Type type = ts.getType(typeName);
		if (type == null) {
			throw new AnnotatorInitializationException(
					AnnotatorInitializationException.TYPE_NOT_FOUND,
					new Object[] { CASUtils.class.getName(), typeName });
		}
		return type;
	}

	/**
	 * Get a Feature object from a Type corresponding to a name.
	 * <p>
	 *
	 * @param  type Type that the feature belongs to
	 * @param  featName Base name of the feature as a string.
	 * @return Feature object
	 * @throws AnnotatorInitializationException
	 */	
	public static final Feature initFeature(Type type, String featName)
			throws AnnotatorInitializationException {
		Feature feat = type.getFeatureByBaseName(featName);
		if (feat == null) {
			throw new AnnotatorInitializationException(
					AnnotatorInitializationException.FEATURE_NOT_FOUND,
					new Object[] { CASUtils.class.getName(), featName });
		}
		return feat;
	}

	/**
	 * Extract a list of annotation feature structures for a given type name.
	 * <p>
	 *
	 * @param  jcas 
	 * @param  typeName Full type name to extract
	 * @return List of AnnotationFS
	 */	
	public static final ArrayList<AnnotationFS> extractAFSList(JCas jcas, String typeName) {
		ArrayList<AnnotationFS> annotations = new ArrayList<AnnotationFS>();
		try {
			TypeSystem typeSystem = jcas.getTypeSystem();
			Type type = typeSystem.getType(typeName);

			if (type!=null) {

				JFSIndexRepository indexRepository = jcas.getJFSIndexRepository();
				AnnotationIndex<Annotation> index = indexRepository.getAnnotationIndex();

				ConstraintFactory cf = jcas.getConstraintFactory();
				FSTypeConstraint filter = cf.createTypeConstraint();

				filter.add(type);

				FSIterator<Annotation> list = jcas.createFilteredIterator(index.iterator(), filter);
				while (list.hasNext()) {
					AnnotationFS afs = (AnnotationFS)list.next();
					annotations.add(afs);
				}
			} else {
				logger.log(Level.INFO, "Type " + typeName + " not found in typesystem");
			}
		}
		catch (CASRuntimeException e) {
			logger.log(Level.WARNING,e.toString(),e);
		}
		return annotations;
	}

	/**
	 * Extract a list of primitive annotation feature structures for a given type name.
	 * <p>
	 * The features from the underlying AnnotationFS to be included in the primitiveAFS are specified. 
	 * <p>
	 * Note: 
	 * PrimitiveAFS is not a UIMA class. It is defined and used as a simple class 
	 * that is easier to handle than AnnotationFS with the restriction that it can
	 * only hold features that are UIMA primitive types. If other types are 
	 * requested then the return value depends on the type.
	 * <ul>
	 * <li>Sentence or Paragraph - the text of the sentence or pragraph</li>
	 * <li>A FSArray - the covered text of the first element in the array if it exists</li>
	 * <li>lemma - the string value of the lemma key</li>
	 * <li>Anything else - set to an empty string value in the returned object</li>
	 * <ul>
	 * <br>
	 * <p>	 * @param  jcas 
	 * @param  typeName Full type name to extract
	 * @param  featureNames List of feature base names to be extracted.
	 * @return List of PrimitiveAFS
	 */	
	public static final ArrayList<PrimitiveAFS> extractPrimitiveAFSList(JCas jcas, String typeName, ArrayList<String> featureNames) {
		ArrayList<PrimitiveAFS> annotations = new ArrayList<PrimitiveAFS>();
		try {
			TypeSystem typeSystem = jcas.getTypeSystem();
			Type type = typeSystem.getType(typeName);

			if (type!=null ) {

				JFSIndexRepository indexRepository = jcas.getJFSIndexRepository();
				AnnotationIndex<Annotation> index = indexRepository.getAnnotationIndex();

				ConstraintFactory cf = jcas.getConstraintFactory();
				FSTypeConstraint filter = cf.createTypeConstraint();

				filter.add(type);

				FSIterator<Annotation> list = jcas.createFilteredIterator(index.iterator(), filter);
				while (list.hasNext()) {
					AnnotationFS afs = (AnnotationFS)list.next();
					PrimitiveAFS pafs = new PrimitiveAFS();
					pafs.setBegin(afs.getBegin());
					pafs.setEnd(afs.getEnd());
					pafs.setCoveredText(afs.getCoveredText());
					pafs.setTypeStr(typeName);
					HashMap<String, String> features = new HashMap<String, String>();
					for (String ftName : featureNames) {
						try {
							Feature ft = afs.getType().getFeatureByBaseName(ftName);
							if (ft.getRange().isPrimitive()) {
								String str = afs.getFeatureValueAsString(ft);
								features.put(ftName, str);
							} else if (ft.getRange().isArray()) { // try the covered text on the first element 
								FeatureStructure fs = ((ArrayFS) afs.getFeatureValue(ft)).get(0);
								if (fs != null) {
									if (fs.getType().getFeatureByBaseName("begin") != null) // it's an annotation
										features.put(ftName, ((AnnotationFS) fs).getCoveredText());
									else
										features.put(ftName, "");
								}		
							} else {
								FeatureStructure fs = afs.getFeatureValue(ft);
								String name = fs.getType().getName();
								if (name.equals(SENTENCETYPE) || name.equals(PARAGRAPHTYPE))
									features.put(ftName, ((AnnotationFS) fs).getCoveredText());
								else if ( name.equals(LEMMATYPE)) {
									Type lemmaFSType = fs.getType();
									Feature lemmaKey = lemmaFSType.getFeatureByBaseName(LEMMAKEY);
									if (lemmaKey != null) features.put(ftName, fs.getStringValue(lemmaKey));
								}
							}
						}
						catch (Exception e) {
							features.put(ftName, "");
						}
					}
					pafs.setFeatures(features);
					annotations.add(pafs);
				}
			} else {
				logger.log(Level.INFO, "Type " + typeName + " not found in typesystem");
			}
	}
	catch (CASRuntimeException e) {
		logger.log(Level.WARNING,e.toString(),e);
	}
	return annotations;
}

/**
 * Extract a list of primitive annotation feature structures for a given type name.
 * <p>
 * This extraction method optionally returns all or none of any features 
 * according to the value of the getFeatures parameter. (begin, end and covered text 
 * are always returned).
 * <p>
 * Note: 
 * PrimitiveAFS is not a UIMA class. It is defined and used as a simple class 
 * that is easier to handle than AnnotationFS with the restriction that it can
 * only hold features that are UIMA primitive types. If other types are 
 * requested then the return value depends on the type.
 * <ul>
 * <li>Sentence or Paragraph - the text of the sentence or pragraph</li>
 * <li>A FSArray - the covered text of the first element in the array if it exists</li>
 * <li>lemma - the string value of the lemma key</li>
 * <li>Anything else - set to an empty string value in the returned object</li>
 * <ul>
 * <br>
 * <p>
 * @param  jcas 
 * @param  typeName Full type name to extract
 * @param  getFeatures If true all features are extracted, if false only the mandatory.
 * @return List of PrimitiveAFS
 */	
public static final ArrayList<PrimitiveAFS> extractPrimitiveAFSList(JCas jcas, String typeName, boolean getFeatures) {
	ArrayList<PrimitiveAFS> annotations = new ArrayList<PrimitiveAFS>();
	TypeSystem typeSystem = jcas.getTypeSystem();
	Type type = typeSystem.getType(typeName);

	if (type!=null) {

		ArrayList<String> featureNames = new ArrayList<String>();

		if (getFeatures) {
			List<Feature> feats = type.getFeatures();
			for (Feature ft : feats){
				featureNames.add(ft.getShortName());
			}
		} 

		annotations = extractPrimitiveAFSList(jcas, typeName, featureNames);
	} else {
		logger.log(Level.INFO, "Type " + typeName + " not found in typesystem");
	}
	return annotations;
}

/**
 * Extracts a primitive feature value as a string from a feature structure.
 * <p>
 * If the feature does not exist a null is returned. If it is not a primitive
 * but a primitive can be inferred then that is returned. See following cases:
 * <ul>
 * <li>Sentence or Paragraph - the text of the sentence or pragraph</li>
 * <li>lemma - the string value of the lemma key</li>
 * <ul>
 * <br>
 * <p>
 * @param fs	feature structure to extract feature from
 * @param feature  short name of feature to extract.
 * @return  string value of feature or null.
 */
public static final String extractPrimitiveFeatureAsString(AnnotationFS afs, String feature){
	Feature ft = null;
	String ret = null;
	Type type = afs.getType();

	try {
		ft = initFeature(type, feature);
		if (ft.getRange().isPrimitive()) {
			ret = afs.getFeatureValueAsString(ft);
		} else if (ft.getRange().isArray()) { // try the covered text on the first element 
			FeatureStructure fs = ((ArrayFS) afs.getFeatureValue(ft)).get(0);
			if (fs != null) {
				if (fs.getType().getFeatureByBaseName("begin") != null) // it's an annotation
					ret = ((AnnotationFS) fs).getCoveredText();
			}		
		} else {
			FeatureStructure fs = afs.getFeatureValue(ft);
			String name = fs.getType().getName();
			if (name.equals(SENTENCETYPE) || name.equals(PARAGRAPHTYPE))
				ret = ((AnnotationFS) fs).getCoveredText();
			else if ( name.equals(LEMMATYPE)) {
				Type lemmaFSType = fs.getType();
				Feature lemmaKey = lemmaFSType.getFeatureByBaseName(LEMMAKEY);
				if (lemmaKey != null) ret = fs.getStringValue(lemmaKey);
			}
		}
		
		
		
		
	} catch (AnnotatorInitializationException e) {
		logger.log(Level.INFO,"Failed to find feature for extract. " + e.toString(),e);
	}
	catch (CASRuntimeException e) {
		logger.log(Level.INFO,"Failed to get feature value for feature: " + ft.getName() + " " + e.toString(),e);
	}
	return ret;
}


/**
 * Sets a primitive string feature value in a feature structure.
 * <p>
 * If the feature is not a primitive or does not exist a null is returned.
 * <p>
 * @param fs	feature structure to extract feature from
 * @param feature  short name of feature to extract.
 * @param  value to set.
 * @return  boolean indicating success or failure
 */
public static final boolean setPrimitiveFeature(AnnotationFS fs, String feature, String value){
	Feature ft = null;
	Type type = fs.getType();
	boolean ret=false;

	try {
		ft = initFeature(type, feature);
		Type ftType = ft.getRange();

		if (ftType.getName().equals(CAS.TYPE_NAME_STRING)) {
			fs.setStringValue(ft, value);
			ret = true;
		} 
	} catch (AnnotatorInitializationException e) {
		logger.log(Level.INFO,"Failed to find feature for extract. " + e.toString(),e);
	}
	catch (CASRuntimeException e) {
		logger.log(Level.INFO,"Failed to set feature value for feature: " + ft.getName() + " " + e.toString(),e);
	}
	return ret;
}

/**
 * Sets a primitive integer feature value in a feature structure.
 * <p>
 * If the feature is not a primitive or does not exist a null is returned.
 * <p>
 * @param fs	feature structure to extract feature from
 * @param feature  short name of feature to extract.
 * @param value	to set
 * @return  boolean indicating success or failure
 */
public static final boolean setPrimitiveFeature(AnnotationFS fs, String feature, int value){
	Feature ft = null;
	Type type = fs.getType();
	boolean ret = false;

	try {
		ft = initFeature(type, feature);
		Type ftType = ft.getRange();
		if (ftType.getName().equals(CAS.TYPE_NAME_INTEGER)) {
			fs.setIntValue(ft, value);	
			ret = true;
		}
	} catch (AnnotatorInitializationException e) {
		logger.log(Level.INFO,"Failed to find feature for extract. " + e.toString(),e);
	}
	catch (CASRuntimeException e) {
		logger.log(Level.INFO,"Failed to set feature value for feature: " + ft.getName() + " " + e.toString(),e);
	}
	return ret;
}

/**
 * Sets a primitive boolean feature value in a feature structure.
 * <p>
 * If the feature is not a primitive or does not exist a null is returned.
 * <p>
 * @param fs	feature structure to extract feature from
 * @param feature  short name of feature to extract.
 * @param value	to set
 * @return  boolean indicating success or failure
 */
public static final boolean setPrimitiveFeature(AnnotationFS fs, String feature, boolean value){
	Feature ft = null;
	Type type = fs.getType();
	boolean ret = false;

	try {
		ft = initFeature(type, feature);
		Type ftType = ft.getRange();
		if (ftType.getName().equals(CAS.TYPE_NAME_BOOLEAN)) {
			fs.setBooleanValue(ft, value);	
			ret = true;
		}
	} catch (AnnotatorInitializationException e) {
		logger.log(Level.INFO,"Failed to find feature for extract. " + e.toString(),e);
	}
	catch (CASRuntimeException e) {
		logger.log(Level.INFO,"Failed to set feature value for feature: " + ft.getName() + " " + e.toString(),e);
	}
	return ret;
}

/**
 * Extract ICA's MetaFields from the CAS.
 * <p>
 * MetaFields are name value pairs typically added by crawlers or crawler plugins. 
 * <p>
 * @param  cas 
 * @return properties representing name value pairs
 */	
public static final Properties extractICAMetaFields(CAS cas) {
	Properties metadata = new Properties();
	TypeSystem ts = cas.getTypeSystem();
	Feature feature = ts.getFeatureByFullName("uima.tcas.DocumentAnnotation:id");
	if (feature != null) {
		Iterator<CAS> itr = cas.getViewIterator();
		while (itr.hasNext()) {
			CAS c = (CAS)itr.next();
			String sofaId = c.getSofa().getSofaID();

			if (sofaId.equals("_InitialView")) {

				Type metadataType = ts.getType("com.ibm.es.oze.MetaField");
				if (metadataType != null) {
					try {
						//FSIterator metaIterator = cas.getIndexRepository().get
						FSIterator<TOP> metaIterator = cas.getJCas().getJFSIndexRepository().getAllIndexedFS(metadataType);
						Feature nameFeature = metadataType.getFeatureByBaseName("name");
						Feature valueFeature = metadataType.getFeatureByBaseName("value");
						if (nameFeature != null && valueFeature != null) {
							while (metaIterator.hasNext())
							{
								FeatureStructure fs = (FeatureStructure)metaIterator.next();
								String name = fs.getFeatureValueAsString(nameFeature);
								String value = fs.getFeatureValueAsString(valueFeature);
								if (name != null && !name.isEmpty())
									if (value != null && !value.isEmpty())
										metadata.put(name, value);
							}
						}
					}
					catch (CASException e) {
						logger.log(Level.WARNING,e.toString(),e);
					}
				}
			}
		}
	}
	return metadata;
}

/**
 * Extract Content Classification MetaFields from the CAS.
 * <p>
 * ICMMetaFields are name value pairs added by Content Classification. 
 * <p>
 * @param  cas 
 * @return properties representing name value pairs
 */	
public static final Properties extractICMMetaFields(CAS cas) {
	Properties metadata = new Properties();
	TypeSystem ts = cas.getTypeSystem();
	Feature feature = ts.getFeatureByFullName("uima.tcas.DocumentAnnotation:id");
	if (feature != null) {
		Iterator<CAS> itr = cas.getViewIterator();
		while (itr.hasNext()) {
			CAS c = (CAS)itr.next();
			String sofaId = c.getSofa().getSofaID();

			if (sofaId.equals("_InitialView")) {

				Type metadataType = ts.getType("com.ibm.es.oze.ICMMetaField");
				if (metadataType != null) {
					try {
						//FSIterator metaIterator = cas.getIndexRepository().get
						FSIterator<TOP> metaIterator = cas.getJCas().getJFSIndexRepository().getAllIndexedFS(metadataType);
						Feature nameFeature = metadataType.getFeatureByBaseName("name");
						Feature valueFeature = metadataType.getFeatureByBaseName("value");
						if (nameFeature != null && valueFeature != null) {
							while (metaIterator.hasNext())
							{
								FeatureStructure fs = (FeatureStructure)metaIterator.next();
								String name = fs.getFeatureValueAsString(nameFeature);
								String value = fs.getFeatureValueAsString(valueFeature);
								if (name != null && !name.isEmpty())
									if (value != null && !value.isEmpty())
										metadata.put(name, value);
							}
						}
					}
					catch (CASException e) {
						logger.log(Level.WARNING,e.toString(),e);
					}
				}
			}
		}
	}
	return metadata;
}

/**
 * Returns the value of a given AE Configuration parameter.
 * <p>
 * @param context   uima context
 * @param groupName Parameter group name or null if no group.
 * @param parameter Parameter name.
 * @return          Parameter value.
 * @throws ResourceInitializationException 
 */
public static final String getConfigurationStringValue(UimaContext context, String groupName, String parameter) throws ResourceInitializationException{
	String ret = null;
	if (groupName == null)
		ret = (String) context.getConfigParameterValue(parameter);
	else
		ret = (String) context.getConfigParameterValue(groupName, parameter);
	if (ret == null) {
		throw new ResourceInitializationException (
				ResourceInitializationException.CONFIG_SETTING_ABSENT, new Object[] {parameter});
	}
	return ret;
}

/**
 * Returns the value of a given AE Configuration parameter.
 * <p>
 * @param context   uima context
 * @param groupName Parameter group name or null if no group.
 * @param parameter Parameter name.
 * @return          Parameter value.
 * @throws ResourceInitializationException 
 */
public static final Integer getConfigurationIntegerValue(UimaContext context, String groupName, String parameter) throws ResourceInitializationException{
	Integer ret = null;
	if (groupName == null)
		ret = (Integer) context.getConfigParameterValue(parameter);
	else
		ret = (Integer) context.getConfigParameterValue(groupName, parameter);
	if (ret == null) {
		throw new ResourceInitializationException (
				ResourceInitializationException.CONFIG_SETTING_ABSENT, new Object[] {parameter});
	}
	return ret;
}

/**
 * Returns the value of a given AE Configuration parameter.
 * <p>
 * @param context   uima context
 * @param groupName Parameter group name or null if no group.
 * @param parameter Parameter name.
 * @return          Parameter value.
 * @throws ResourceInitializationException 
 */
public static final Boolean getConfigurationBooleanValue(UimaContext context, String groupName, String parameter) throws ResourceInitializationException{
	Boolean ret = null;
	if (groupName == null)
		ret = (Boolean) context.getConfigParameterValue(parameter);
	else
		ret = (Boolean) context.getConfigParameterValue(groupName, parameter);
	if (ret == null) {
		throw new ResourceInitializationException (
				ResourceInitializationException.CONFIG_SETTING_ABSENT, new Object[] {parameter});
	}
	return ret;
}

}
