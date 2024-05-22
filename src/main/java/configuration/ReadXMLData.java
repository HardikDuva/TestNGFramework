package configuration;

import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ReadXMLData {
	// Objects to fetch data from XML
	File file;
	DocumentBuilderFactory dbf;
	DocumentBuilder db;
	Document doc;
	AutoLogger logger = new AutoLogger(ReadXMLData.class);

	/**
	 * Overloaded Constructor
	 * 
	 * @param filePath
	 */
	public ReadXMLData(String filePath) {
		
		try {
			readXML(filePath);
		} catch (Exception e) {
			logger.e(e);
		}
	}

	/**
	 * This readXML1 function will read the XML element from the specified XML
	 * file
	 * 
	 * @param filenm
	 *            File path to read
	 * @throws Exception
	 */
	public void readXML(String filenm) throws Exception {
		file = new File(filenm);
		dbf = DocumentBuilderFactory.newInstance();
		db = dbf.newDocumentBuilder();
		doc = db.parse(file);
		doc.getDocumentElement().normalize();
	}

	/**
	 * This readXMLElement function will read the first level XML elements from
	 * XML file first level XML element
	 * 
	 * @param parNm
	 *            Target parameter name
	 * @param eleNm
	 *            Target element name
	 * @param i
	 *            Target number of nodelist item
	 * @return Element value
	 */
	public String readXMLElement(String parNm, String eleNm, int i) {
		NodeList nodeLst = doc.getElementsByTagName(parNm);
		Node node1 = nodeLst.item(i);
		if (node1.getNodeType() == Node.ELEMENT_NODE) {
			Element elem1 = (Element) node1;
			NodeList nodeLst1 = elem1.getElementsByTagName(eleNm);
			Element elem2 = (Element) nodeLst1.item(0);
			NodeList nodeLst2 = elem2.getChildNodes();
			return (nodeLst2.item(0)).getNodeValue();
		}
		return "";
	}

	/**
	 * Get the value of specified node/element
	 * 
	 * @param nodes
	 *            Node in hierarchy
	 * @return Value of element
	 */
	public String get(String... nodes) {

		String value = "";

		try {
			Node node = doc.getDocumentElement();
			for (String s : nodes) {
				NodeList childList = node.getChildNodes();
				boolean childFound = false;
				for (int j = 0; j < childList.getLength(); j++) {
					if (s.equals(childList.item(j).getNodeName())) {
						childFound = true;
						node = childList.item(j);
						break;
					}
				}
				Assert.assertTrue(childFound, "Node '" + s
						+ "' not found in XML!");
			}

			if (node == null) {
				throw new Exception("Error while finding node!");
			}
			value = node.getFirstChild().getNodeValue();

		} catch (Exception e) {
			logger.e(e);
		}

		return value;
	}
}