package com.mns.uima.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Iterator;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.Level;
/** 
 * Document details from ICA pipeline. Static only cannot be instantiated.
 * <p>
 * @author      Martin Saunders <martin.saunders@uk.ibm.com>
 * @version     1.1 $Revision: 125 $            
 */

/*   Example minimum CAS DocumentAnnotation
 * <tcas:DocumentAnnotation 
 * 		xmi:id="8" 
 * 		sofa="1" 
 * 		begin="0" 
 * 		end="50" 
 * 		language="en"/>
 * 
 * Example CAS DocumentAnnotation from ICA
 * <tcas:DocumentAnnotation 
 * 		xmi:id="1" 
 * 		sofa="15" 
 * 		begin="0" 
 * 		end="50" 
 * 		language="en" 
 * 		id="file:///c:/data/tmp/tmp2/test.txt" 
 * 		fallbackLanguage="en" 
 * 		useNgramForCJK="18" 
 * 		indexAllLemmas="0" 
 * 		documentPart="0" 
 * 		esDocumentMetaData="22"/>
 * 
 * Example CAS additional DocumentMetaData from ICA
 * 	<tt:DocumentMetaData 
 * 		xmi:id="22" 
 * 		crawlspaceId="t1341171863373" 
 * 		crawlerId="col_17167.WIN_52670" 
 * 		dataSource="winfs" 
 * 		dataSourceName="Windows file system crawler 1" 
 * 		docType="text/plain" 
 * 		date="1341170706000" 
 * 		url="file:///c:/data/tmp/tmp2/test.txt" 
 * 		httpcode="2000" 
 * 		documentId="2" 
 * 		title="test.txt" 
 * 		deleteDocument="false"/>
 */
public class DocumentDetails {
	public static String id;			// from UIMA DocumentAnnotation ICA version
	public static String language;		// from UIMA DocumentAnnotation
	public static String url; 			// from ICA DocumentMetaData
	public static String title;			// from ICA DocumentMetaData
	public static String dataSource;	// from ICA DocumentMetaData
	public static String dataSourceName;// from ICA DocumentMetaData
	public static String docType;		// from ICA DocumentMetaData
	public static Date   docDate;

	private static final String SOURCE_DOCUMENT_INFORMATION_TYPE = "org.apache.uima.examples.SourceDocumentInformation";

	/**
	 * Suppress default constructor so it can't be instantiated.
	 * <p>
	 * @throws AssertionError
	 */	
	private DocumentDetails(){
		throw new AssertionError();
	}

	/**
	 * Initialise object with metadata about a document from UIMA and ICA document details.
	 * <p>
	 *
	 */
	public static void extractDocumentDetails(JCas jcas) {
		DocumentDetails.id="unknown";	
		DocumentDetails.language="unknown";	
		DocumentDetails.url="unknown"; 	
		DocumentDetails.title="unknown";
		DocumentDetails.dataSource="unknown";
		DocumentDetails.dataSourceName="unknown";
		DocumentDetails.docType="unknown";	
		DocumentDetails.docDate=new Date(0); // initialise to 01-01-1970


		// Find the CAS that has the document metadata set by ICA - it has the additional esDocumentMetaData feature
		CAS cas = jcas.getCas();
		getDocumentLanguage(cas);
		
		final Feature feature = cas.getTypeSystem().getFeatureByFullName(CAS.TYPE_NAME_DOCUMENT_ANNOTATION + TypeSystem.FEATURE_SEPARATOR + "esDocumentMetaData");
		//final Feature feature = jcas.getTypeSystem().getFeatureByFullName(CAS.TYPE_NAME_DOCUMENT_ANNOTATION + TypeSystem.FEATURE_SEPARATOR + "esDocumentMetaData");

		if (feature != null) { // ICA id present in type system

			final Iterator<?> itr = cas.getViewIterator();
			while (itr.hasNext()) {
				final CAS c = (CAS) itr.next();
				final String sofaId = c.getSofa().getSofaID();

				if (sofaId.equals("_InitialView")) {
					getICADocumentDetails(c);
				}			
			}
		} else {  // not ICA so try SDK Examples CollectionReader
			TypeSystem typeSystem = jcas.getTypeSystem();
			Type sdiType = typeSystem.getType(SOURCE_DOCUMENT_INFORMATION_TYPE);

			if (sdiType != null) {
				AnnotationIndex<Annotation> sdiIdx = jcas.getAnnotationIndex(SourceDocumentInformation.type);
				FSIterator<Annotation> sdiIt = sdiIdx.iterator();
				for (sdiIt.moveToFirst(); sdiIt.isValid(); sdiIt.moveToNext()) {
					SourceDocumentInformation srcDocInfo = (SourceDocumentInformation)  sdiIt.get();
					String srcDocUri = srcDocInfo.getUri();
					DocumentDetails.url = srcDocUri;
					try {
						File inFile = new File(new URL(srcDocUri).getPath());
						String fileName = inFile.getName();
						if (fileName!=null)
							if (fileName.length() != 0)
								DocumentDetails.title=fileName;
					} catch (MalformedURLException e1) {
						UIMAFramework.getLogger().log(Level.WARNING,e1.toString(),e1);
					}
				}
			}
		}

	}

	private static void getDocumentLanguage(CAS cas) {
		AnnotationFS doc = cas.getDocumentAnnotation();
		Type documentAnnotType = doc.getType();

		// get language features
		Feature languageFeature	= documentAnnotType.getFeatureByBaseName("language");

		if ( languageFeature != null ) {
			String language = doc.getFeatureValueAsString(languageFeature);
			if ( language != null )  DocumentDetails.language = language;
		}

	}

	private static void getICADocumentDetails(CAS cas) {

		// get DocumentAnnotation type to retrieve the id and esDocumentMetaData features

		AnnotationFS doc = cas.getDocumentAnnotation();
		Type documentAnnotType = doc.getType();

		// get id and language features
		Feature idFeature 		= documentAnnotType.getFeatureByBaseName("id");

		if ( idFeature != null ) {
			String id = doc.getFeatureValueAsString(idFeature);
			if ( id != null )  {
				try {
					DocumentDetails.id = URLDecoder.decode(id, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					DocumentDetails.id = "unknown";
				}
			}
		}

		// get esDocumentMetaData feature
		Feature esDocumentMetaDataFeature = documentAnnotType.getFeatureByBaseName("esDocumentMetaData");

		if ( esDocumentMetaDataFeature != null ) {
			// get document metadata from esDocumentMetaData feature
			FeatureStructure esDocumentMetaData = doc.getFeatureValue(esDocumentMetaDataFeature);

			if (esDocumentMetaData != null) {
				Type esDocumentMetaDataType = esDocumentMetaData.getType();

				Feature urlFeature				= esDocumentMetaDataType.getFeatureByBaseName("url");
				Feature docTypeFeature			= esDocumentMetaDataType.getFeatureByBaseName("docType");
				Feature titleFeature			= esDocumentMetaDataType.getFeatureByBaseName("title");
				Feature dataSourceFeature		= esDocumentMetaDataType.getFeatureByBaseName("dataSource");
				Feature dataSourceNameFeature	= esDocumentMetaDataType.getFeatureByBaseName("dataSourceName");
				Feature docDateFeature	= esDocumentMetaDataType.getFeatureByBaseName("date");

				// Note the Features are defined on the type but not all values are present, 
				// depends on the crawler used so check each one.
				if (urlFeature != null)	{
					String str = esDocumentMetaData.getStringValue(urlFeature);  
					if (str != null) {
						try {
							DocumentDetails.url = URLDecoder.decode(str, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							DocumentDetails.url = "unknown";
						}
					}
				}
				if(dataSourceFeature != null) {
					String str = esDocumentMetaData.getStringValue(dataSourceFeature);
					if (str != null) DocumentDetails.dataSource = str;							
				}
				if(dataSourceNameFeature != null) {
					String str = esDocumentMetaData.getStringValue(dataSourceNameFeature);
					if (str != null) DocumentDetails.dataSourceName = str;
				}
				if(titleFeature != null) {
					String str = esDocumentMetaData.getStringValue(titleFeature);
					if (str != null) 
						DocumentDetails.title = str;
					else {
						if (!DocumentDetails.url.equals("unknown")) {
							try {
								String uriStr = URLDecoder.decode(DocumentDetails.url, "UTF-8");
								URI uri = new URI(uriStr);
								String fileName = new File(uri.getPath()).getName();
								if (fileName!=null)
									if (fileName.length() != 0)
										DocumentDetails.title=fileName;
							} catch (UnsupportedEncodingException e) {
								UIMAFramework.getLogger().log(Level.WARNING,e.toString(),e);
							} catch (URISyntaxException e) {
								UIMAFramework.getLogger().log(Level.WARNING,e.toString(),e);
							}
						}
					}
				}
				if(docTypeFeature != null) {
					String str = esDocumentMetaData.getStringValue(docTypeFeature);				
					if (str != null) DocumentDetails.docType = str;
				}
				if(docDateFeature != null) {
					String str = esDocumentMetaData.getStringValue(docDateFeature);	
					try {
						long ts = Long.parseLong(str)*1000L;
						DocumentDetails.docDate = new java.util.Date(ts);
					} catch (NumberFormatException e) {}
				}
			}
		}
	}
}
